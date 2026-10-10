import test from 'node:test'
import assert from 'node:assert/strict'
import {
  createMessageNotifier,
  dispatchMessageUpdated,
  findNewUnreadMessages,
  requestBrowserNotificationPermission,
  showBrowserNotification
} from '../src/utils/message-notifier.js'

function createMemoryStorage() {
  const values = new Map()
  return {
    getItem(key) {
      return values.has(key) ? values.get(key) : null
    },
    setItem(key, value) {
      values.set(key, String(value))
    },
    removeItem(key) {
      values.delete(key)
    }
  }
}

test('findNewUnreadMessages ignores read and already seen messages', () => {
  const messages = [
    { id: 1, readStatus: 0 },
    { id: 2, readStatus: 1 },
    { id: 3, readStatus: 0 }
  ]

  assert.deepEqual(
    findNewUnreadMessages(messages, new Set(['1'])).map((message) => message.id),
    [3]
  )
})

test('requestBrowserNotificationPermission handles unsupported and granted browsers', async () => {
  assert.equal(await requestBrowserNotificationPermission(null), 'unsupported')

  let calls = 0
  const notificationApi = {
    permission: 'default',
    requestPermission: async () => {
      calls += 1
      return 'granted'
    }
  }

  assert.equal(await requestBrowserNotificationPermission(notificationApi), 'granted')
  assert.equal(calls, 1)
})

test('message notifier establishes a baseline and only emits each new message once', async () => {
  const storage = createMemoryStorage()
  const notifications = []
  let clearedTimerId
  let snapshot = {
    unreadCount: 1,
    messages: [{ id: 1, readStatus: 0, title: '旧消息' }]
  }

  const notifier = createMessageNotifier({
    userId: 7,
    storage,
    notificationApi: { permission: 'granted' },
    fetchSnapshot: async () => snapshot,
    showNotification: (message) => notifications.push(message),
    timerApi: {
      setInterval: () => 1,
      clearInterval: (timerId) => { clearedTimerId = timerId }
    }
  })

  await notifier.start()
  assert.equal(notifications.length, 0)

  snapshot = {
    unreadCount: 2,
    messages: [
      { id: 1, readStatus: 0, title: '旧消息' },
      { id: 2, readStatus: 0, title: '新消息' }
    ]
  }
  await notifier.refresh()
  await notifier.refresh()

  assert.deepEqual(notifications.map((message) => message.id), [2])
  assert.equal(notifier.getUnreadCount(), 2)
  notifier.stop()
  assert.equal(notifier.isRunning(), false)
  assert.equal(clearedTimerId, 1)
})

test('dispatchMessageUpdated emits the shared update event when the browser supports events', () => {
  const originalDispatchEvent = globalThis.dispatchEvent
  const originalCustomEvent = globalThis.CustomEvent
  const events = []
  globalThis.CustomEvent = class {
    constructor(type, init) {
      this.type = type
      this.detail = init?.detail
    }
  }
  globalThis.dispatchEvent = (event) => {
    events.push(event)
  }

  try {
    dispatchMessageUpdated({ source: 'read' })
    assert.equal(events.length, 1)
    assert.equal(events[0].type, 'enterprise-message-updated')
    assert.deepEqual(events[0].detail, { source: 'read' })
  } finally {
    if (originalDispatchEvent) {
      globalThis.dispatchEvent = originalDispatchEvent
    } else {
      delete globalThis.dispatchEvent
    }
    if (originalCustomEvent) {
      globalThis.CustomEvent = originalCustomEvent
    } else {
      delete globalThis.CustomEvent
    }
  }
})

test('showBrowserNotification focuses the window and invokes the click handler', () => {
  const notices = []
  let focused = false
  let clickedMessage
  class FakeNotification {
    static permission = 'granted'

    constructor(title, options) {
      this.title = title
      this.options = options
      notices.push(this)
    }
  }

  const message = { id: 9, title: '审批结果', content: '申请已通过' }
  const notice = showBrowserNotification(message, {
    notificationApi: FakeNotification,
    windowObject: { focus: () => { focused = true } },
    onClick: (value) => { clickedMessage = value }
  })

  notice.onclick()

  assert.equal(notices.length, 1)
  assert.equal(notices[0].title, '审批结果')
  assert.equal(notices[0].options.body, '申请已通过')
  assert.equal(focused, true)
  assert.equal(clickedMessage, message)
})
