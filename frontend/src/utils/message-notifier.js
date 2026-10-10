export const MESSAGE_UPDATED_EVENT = 'enterprise-message-updated'
export const MESSAGE_NOTIFICATION_POLL_INTERVAL = 30_000

const STORAGE_KEY_PREFIX = 'enterprise_notified_messages_'
const MAX_SEEN_MESSAGE_IDS = 100

function defaultNotificationApi() {
  return typeof globalThis !== 'undefined' ? globalThis.Notification : undefined
}

function defaultWindowObject() {
  return typeof globalThis !== 'undefined' ? globalThis.window : undefined
}

function defaultStorage() {
  return typeof globalThis !== 'undefined' ? globalThis.localStorage : undefined
}

function defaultTimerApi() {
  return typeof globalThis !== 'undefined' ? globalThis : undefined
}

function messageId(message) {
  const id = message?.id ?? message?.sourceKey
  return id === null || typeof id === 'undefined' ? '' : String(id)
}

function isUnread(message) {
  return Number(message?.readStatus) === 0
}

function normalizePermission(permission) {
  return ['granted', 'denied', 'default'].includes(permission) ? permission : 'unsupported'
}

function readSeenIds(storage, storageKey) {
  if (!storage) {
    return new Set()
  }
  try {
    const value = storage.getItem(storageKey)
    const ids = value ? JSON.parse(value) : []
    return new Set(Array.isArray(ids) ? ids.map(String) : [])
  } catch {
    return new Set()
  }
}

function writeSeenIds(storage, storageKey, seenIds) {
  if (!storage) {
    return
  }
  const values = Array.from(seenIds).slice(-MAX_SEEN_MESSAGE_IDS)
  try {
    storage.setItem(storageKey, JSON.stringify(values))
  } catch {
    // 浏览器存储不可用时仍保留当前页面内的去重状态。
  }
}

export function getBrowserNotificationPermission(notificationApi = defaultNotificationApi()) {
  return normalizePermission(notificationApi?.permission)
}

export function findNewUnreadMessages(messages = [], seenIds = new Set()) {
  const seen = seenIds instanceof Set ? seenIds : new Set(seenIds)
  const currentIds = new Set()
  return (Array.isArray(messages) ? messages : []).filter((message) => {
    const id = messageId(message)
    if (!id || currentIds.has(id) || !isUnread(message) || seen.has(id)) {
      return false
    }
    currentIds.add(id)
    return true
  })
}

export function requestBrowserNotificationPermission(notificationApi = defaultNotificationApi()) {
  if (!notificationApi || typeof notificationApi.requestPermission !== 'function') {
    return Promise.resolve('unsupported')
  }

  const currentPermission = getBrowserNotificationPermission(notificationApi)
  if (currentPermission !== 'default') {
    return Promise.resolve(currentPermission)
  }

  return new Promise((resolve) => {
    let settled = false
    const finish = (permission) => {
      if (settled) {
        return
      }
      settled = true
      resolve(normalizePermission(permission))
    }

    try {
      const result = notificationApi.requestPermission(finish)
      if (result && typeof result.then === 'function') {
        result.then(finish).catch(() => finish('denied'))
      } else if (typeof result === 'string') {
        finish(result)
      }
    } catch {
      finish('denied')
    }
  })
}

export function showBrowserNotification(message, {
  notificationApi = defaultNotificationApi(),
  windowObject = defaultWindowObject(),
  onClick
} = {}) {
  if (getBrowserNotificationPermission(notificationApi) !== 'granted') {
    return null
  }

  const notification = new notificationApi(message?.title || '企业信息管理系统', {
    body: message?.content || '你有一条新的待处理消息',
    tag: message?.id ? `enterprise-message-${message.id}` : undefined,
    requireInteraction: Number(message?.priority) === 3
  })

  notification.onclick = () => {
    windowObject?.focus?.()
    onClick?.(message)
    notification.close?.()
  }
  return notification
}

export function createMessageNotifier({
  userId = 'current',
  storage = defaultStorage(),
  notificationApi = defaultNotificationApi(),
  windowObject = defaultWindowObject(),
  timerApi = defaultTimerApi(),
  intervalMs = MESSAGE_NOTIFICATION_POLL_INTERVAL,
  fetchSnapshot,
  showNotification = (message) => showBrowserNotification(message, { notificationApi, windowObject }),
  shouldNotify = () => true,
  onNewMessages = () => {},
  onSnapshot = () => {},
  onUnreadCountChange = () => {},
  onError = () => {}
} = {}) {
  if (typeof fetchSnapshot !== 'function') {
    throw new TypeError('fetchSnapshot 必须是函数')
  }

  const storageKey = `${STORAGE_KEY_PREFIX}${userId}`
  const seenIds = readSeenIds(storage, storageKey)
  let timerId
  let running = false
  let initialized = false
  let unreadCount = 0

  function persistSeenIds() {
    writeSeenIds(storage, storageKey, seenIds)
  }

  function currentUnreadCount(snapshot, messages) {
    const value = snapshot?.unreadCount ?? snapshot?.summary?.unread
    return Number.isFinite(Number(value))
      ? Number(value)
      : messages.filter(isUnread).length
  }

  async function refresh() {
    let snapshot
    try {
      snapshot = await fetchSnapshot()
    } catch (error) {
      onError(error)
      return null
    }

    const messages = Array.isArray(snapshot?.messages) ? snapshot.messages : []
    unreadCount = currentUnreadCount(snapshot, messages)

    if (!initialized) {
      messages.filter(isUnread).forEach((message) => {
        const id = messageId(message)
        if (id) {
          seenIds.add(id)
        }
      })
      initialized = true
    } else {
      const newMessages = findNewUnreadMessages(messages, seenIds)
      newMessages.forEach((message) => {
        const id = messageId(message)
        seenIds.add(id)
        try {
          if (shouldNotify(message, snapshot)) {
            showNotification(message)
          }
        } catch (error) {
          onError(error)
        }
      })
      if (newMessages.length) {
        onNewMessages(newMessages, { ...snapshot, messages, unreadCount })
      }
    }

    persistSeenIds()
    onUnreadCountChange(unreadCount)
    onSnapshot({ ...snapshot, messages, unreadCount })
    return snapshot
  }

  async function start() {
    if (running) {
      return
    }
    running = true
    await refresh()
    if (running && timerApi?.setInterval) {
      timerId = timerApi.setInterval(() => {
        void refresh()
      }, intervalMs)
    }
  }

  function stop() {
    running = false
    if (timerId !== undefined && timerApi?.clearInterval) {
      timerApi.clearInterval(timerId)
    }
    timerId = undefined
  }

  return {
    start,
    stop,
    refresh,
    getUnreadCount: () => unreadCount,
    isRunning: () => running
  }
}

export function dispatchMessageUpdated(detail = {}) {
  if (typeof globalThis === 'undefined' || typeof globalThis.dispatchEvent !== 'function'
    || typeof globalThis.CustomEvent !== 'function') {
    return
  }
  globalThis.dispatchEvent(new globalThis.CustomEvent(MESSAGE_UPDATED_EVENT, { detail }))
}
