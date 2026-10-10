const TYPE_META = Object.freeze({
  NOTICE: { label: '公告', type: 'notice' },
  APPROVAL: { label: '审批', type: 'approval' },
  ATTENDANCE: { label: '考勤', type: 'attendance' },
  PLAN: { label: '计划', type: 'plan' },
  MEETING: { label: '会议', type: 'meeting' },
  SYSTEM: { label: '系统', type: 'system' }
})

export function filterMessages(messages = [], filters = {}) {
  return (Array.isArray(messages) ? messages : [])
    .filter((message) => !filters.type || message.messageType === filters.type)
    .filter((message) => filters.readStatus === null || typeof filters.readStatus === 'undefined' || Number(message.readStatus) === Number(filters.readStatus))
    .filter((message) => !filters.priority || Number(message.priority) === Number(filters.priority))
    .sort(compareMessages)
}

export function summarizeMessages(messages = []) {
  const records = Array.isArray(messages) ? messages : []
  return {
    total: records.length,
    unread: records.filter((message) => Number(message.readStatus) === 0).length,
    highPriority: records.filter((message) => Number(message.priority) === 3).length,
    categories: new Set(records.map((message) => message.messageType).filter(Boolean)).size
  }
}

export function messageTypeMeta(type) {
  return TYPE_META[type] || TYPE_META.SYSTEM
}

export function priorityMeta(priority) {
  return {
    3: { label: '高优先级', type: 'danger' },
    2: { label: '重要提醒', type: 'warning' },
    1: { label: '普通提醒', type: 'info' }
  }[Number(priority)] || { label: '普通提醒', type: 'info' }
}

export function formatMessageTime(value, now = new Date()) {
  if (!value) return '时间待定'
  const time = new Date(String(value).replace(' ', 'T'))
  if (Number.isNaN(time.getTime())) return String(value)
  const diffMinutes = Math.floor((now.getTime() - time.getTime()) / 60000)
  if (diffMinutes >= 0 && diffMinutes < 60) return `${Math.max(diffMinutes, 1)} 分钟前`
  if (diffMinutes >= 60 && diffMinutes < 24 * 60) return `${Math.floor(diffMinutes / 60)} 小时前`
  return String(value).replace('T', ' ').slice(0, 16)
}

function compareMessages(left, right) {
  const priorityDelta = Number(right.priority || 0) - Number(left.priority || 0)
  if (priorityDelta !== 0) return priorityDelta
  return String(right.createTime || '').localeCompare(String(left.createTime || ''))
}
