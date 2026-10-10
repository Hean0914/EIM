const CALENDAR_DOMAIN = 'enterprise-information-management'
const SHANGHAI_OFFSET_MINUTES = 8 * 60

const planStatusMap = {
  0: '未开始',
  1: '进行中',
  2: '已完成',
  3: '已延期',
  4: '已取消'
}

const priorityMap = { 1: '低', 2: '中', 3: '高' }

export function escapeIcsText(value) {
  return String(value ?? '')
    .replaceAll('\\', '\\\\')
    .replaceAll('\r\n', '\n')
    .replaceAll('\r', '\n')
    .replaceAll('\n', '\\n')
    .replaceAll(';', '\\;')
    .replaceAll(',', '\\,')
}

export function buildMeetingEvent(booking = {}) {
  const uid = booking.bookingNo || booking.id
  const summary = booking.meetingTitle || '未命名会议'
  const location = [booking.roomName, booking.location].filter(Boolean).join('，')
  const description = [
    booking.remark,
    booking.participantCount == null ? '' : `参会人数：${booking.participantCount}`
  ].filter(Boolean).join('\n')
  const lines = [
    'BEGIN:VEVENT',
    `UID:${escapeIcsText(uid)}@${CALENDAR_DOMAIN}`,
    `DTSTAMP:${formatUtc(new Date())}`,
    `DTSTART:${formatShanghaiDateTime(booking.startTime)}`,
    `DTEND:${formatShanghaiDateTime(booking.endTime)}`,
    `SUMMARY:${escapeIcsText(summary)}`
  ]
  if (location) lines.push(`LOCATION:${escapeIcsText(location)}`)
  if (description) lines.push(`DESCRIPTION:${escapeIcsText(description)}`)
  if (Number(booking.status) === 3) lines.push('STATUS:CANCELLED')
  lines.push('END:VEVENT')
  return lines.join('\r\n')
}

export function buildPlanEvent(plan = {}) {
  const uid = plan.planNo || plan.id
  const endDate = addDays(plan.endDate, 1)
  const description = [
    plan.content,
    `状态：${planStatusMap[plan.status] || '未知'}`,
    `优先级：${priorityMap[plan.priority] || '未知'}`,
    `进度：${Number(plan.progress || 0)}%`
  ].join('\n')
  return [
    'BEGIN:VEVENT',
    `UID:${escapeIcsText(uid)}@${CALENDAR_DOMAIN}`,
    `DTSTAMP:${formatUtc(new Date())}`,
    `DTSTART;VALUE=DATE:${formatDateValue(plan.startDate)}`,
    `DTEND;VALUE=DATE:${formatDateValue(endDate)}`,
    `SUMMARY:${escapeIcsText(plan.title || '未命名计划')}`,
    `DESCRIPTION:${escapeIcsText(description)}`,
    'END:VEVENT'
  ].join('\r\n')
}

export function buildCalendarFile(name, events = []) {
  const validEvents = events.filter(Boolean)
  return [
    'BEGIN:VCALENDAR',
    'VERSION:2.0',
    'PRODID:-//Enterprise Information Management//Calendar Export//CN',
    'CALSCALE:GREGORIAN',
    `X-WR-CALNAME:${escapeIcsText(name || '日历')}`,
    ...validEvents.flatMap((event) => event.split('\r\n').flatMap(foldIcsLine)),
    'END:VCALENDAR',
    ''
  ].join('\r\n')
}

function foldIcsLine(line) {
  const chunks = []
  let current = ''
  let bytes = 0
  for (const character of line) {
    const size = new TextEncoder().encode(character).length
    if (current && bytes + size > 75) {
      chunks.push(current)
      current = ` ${character}`
      bytes = 1 + size
    } else {
      current += character
      bytes += size
    }
  }
  chunks.push(current)
  return chunks
}

export function downloadCalendarFile(filename, content) {
  const blob = new Blob([content], { type: 'text/calendar;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}

function formatShanghaiDateTime(value) {
  const date = parseShanghaiDate(value)
  return formatUtc(date)
}

function parseShanghaiDate(value) {
  const match = String(value || '').match(/^(\d{4})-(\d{2})-(\d{2})T?(\d{2})?:?(\d{2})?:?(\d{2})?/)
  if (!match) return new Date(NaN)
  const [, year, month, day, hour = '00', minute = '00', second = '00'] = match
  return new Date(Date.UTC(Number(year), Number(month) - 1, Number(day), Number(hour), Number(minute), Number(second)) - SHANGHAI_OFFSET_MINUTES * 60 * 1000)
}

function formatUtc(date) {
  if (Number.isNaN(date.getTime())) return ''
  return date.toISOString().replace(/[-:]/g, '').replace(/\.\d{3}Z$/, 'Z')
}

function formatDateValue(value) {
  return String(value || '').replaceAll('-', '')
}

function addDays(value, days) {
  const [year, month, day] = String(value || '').split('-').map(Number)
  const date = new Date(Date.UTC(year, month - 1, day + days))
  return `${date.getUTCFullYear()}-${String(date.getUTCMonth() + 1).padStart(2, '0')}-${String(date.getUTCDate()).padStart(2, '0')}`
}
