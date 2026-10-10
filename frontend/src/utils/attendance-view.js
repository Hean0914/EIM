const STATUS_LABELS = {
  NORMAL: '正常',
  REST: '休息日',
  LEAVE: '请假',
  OUT: '外出',
  ABSENT: '缺勤',
  ABNORMAL: '存在异常',
  MISSING_CHECK_IN: '上班缺卡',
  MISSING_CHECK_OUT: '下班缺卡'
}

const ANOMALY_STATUS = new Set(['ABNORMAL', 'ABSENT', 'MISSING_CHECK_IN', 'MISSING_CHECK_OUT'])

export function statusLabel(status) {
  return STATUS_LABELS[status] || '未知状态'
}

export function groupCalendarItemsByDate(items = []) {
  return items.reduce((result, item) => {
    const date = item?.date
    if (!date) {
      return result
    }
    const group = result[date] || { total: 0, abnormal: 0, leave: 0, out: 0, items: [] }
    group.total += 1
    group.abnormal += ANOMALY_STATUS.has(item.status) ? 1 : 0
    group.leave += item.status === 'LEAVE' ? 1 : 0
    group.out += item.status === 'OUT' ? 1 : 0
    group.items.push(item)
    result[date] = group
    return result
  }, {})
}

export function buildAttendanceCalendarGrid(month, items = []) {
  const [year, monthNumber] = String(month).split('-').map(Number)
  const firstDay = new Date(year, monthNumber - 1, 1)
  const mondayOffset = (firstDay.getDay() + 6) % 7
  const start = new Date(year, monthNumber - 1, 1 - mondayOffset)
  const grouped = groupCalendarItemsByDate(items)
  return Array.from({ length: 42 }, (_, index) => {
    const date = new Date(start.getFullYear(), start.getMonth(), start.getDate() + index)
    const dateKey = formatDate(date)
    return {
      date: dateKey,
      day: date.getDate(),
      inMonth: date.getFullYear() === year && date.getMonth() === monthNumber - 1,
      group: grouped[dateKey] || null
    }
  })
}

export function buildAnomalyCards(analysis = {}) {
  return [
    { key: 'attendanceRate', label: '出勤率', value: `${Number(analysis.attendanceRate || 0)}%`, tone: 'primary' },
    { key: 'lateCount', label: '迟到', value: Number(analysis.lateCount || 0), tone: 'warning' },
    { key: 'earlyLeaveCount', label: '早退', value: Number(analysis.earlyLeaveCount || 0), tone: 'warning' },
    { key: 'missingCheckInCount', label: '上班缺卡', value: Number(analysis.missingCheckInCount || 0), tone: 'danger' },
    { key: 'missingCheckOutCount', label: '下班缺卡', value: Number(analysis.missingCheckOutCount || 0), tone: 'danger' },
    { key: 'absenceCount', label: '缺勤', value: Number(analysis.absenceCount || 0), tone: 'danger' },
    { key: 'gpsAbnormalCount', label: 'GPS 异常', value: Number(analysis.gpsAbnormalCount || 0), tone: 'info' }
  ]
}

function formatDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
