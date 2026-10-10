const TYPE_META = Object.freeze({
  1: { label: '通知', tag: 'primary' },
  2: { label: '制度', tag: 'warning' },
  3: { label: '活动', tag: 'success' },
  4: { label: '其他', tag: 'info' }
})

const STATUS_META = Object.freeze({
  0: { label: '草稿', tag: 'info' },
  1: { label: '已发布', tag: 'success' },
  2: { label: '已下线', tag: 'warning' }
})

export function getNoticeTypeMeta(type) {
  return TYPE_META[Number(type)] || { label: '其他', tag: 'info' }
}

export function getNoticeStatusMeta(status) {
  return STATUS_META[Number(status)] || { label: '未知状态', tag: 'info' }
}

export function buildNoticeFeed(records = [], readIds = [], options = {}) {
  const summaryLength = Number(options.summaryLength) > 0 ? Number(options.summaryLength) : 120
  const readSet = new Set(Array.isArray(readIds) ? readIds.map(String) : [])

  return (Array.isArray(records) ? records : [])
    .filter(Boolean)
    .map((notice) => {
      const typeMeta = getNoticeTypeMeta(notice.noticeType)
      const statusMeta = getNoticeStatusMeta(notice.publishStatus)
      return {
        ...notice,
        typeLabel: typeMeta.label,
        typeTag: typeMeta.tag,
        statusLabel: statusMeta.label,
        statusTag: statusMeta.tag,
        summary: createSummary(notice.content, summaryLength),
        isPinned: Number(notice.topFlag) === 1,
        isRead: notice.readFlag === true || readSet.has(String(notice.id))
      }
    })
    .sort(compareNotices)
}

export function summarizeNoticeFeed(feed = []) {
  const records = Array.isArray(feed) ? feed : []
  return {
    total: records.length,
    unread: records.filter((notice) => !notice?.isRead).length,
    pinned: records.filter((notice) => notice?.isPinned || Number(notice?.topFlag) === 1).length
  }
}

function createSummary(content, limit) {
  const normalized = String(content || '').replace(/\s+/g, ' ').trim()
  if ([...normalized].length <= limit) {
    return normalized || '暂无正文摘要'
  }
  return `${[...normalized].slice(0, limit).join('')}…`
}

function compareNotices(left, right) {
  const pinnedDelta = Number(right.isPinned) - Number(left.isPinned)
  if (pinnedDelta !== 0) {
    return pinnedDelta
  }
  const rightTime = right.publishTime || right.createTime || ''
  const leftTime = left.publishTime || left.createTime || ''
  return String(rightTime).localeCompare(String(leftTime))
}
