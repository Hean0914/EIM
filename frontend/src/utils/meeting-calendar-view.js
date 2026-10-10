const STATUS_LABELS = {
  0: '待开始',
  1: '进行中',
  2: '已结束',
  3: '已取消'
}

export function meetingStatusLabel(status) {
  return STATUS_LABELS[status] || '未知状态'
}

export function meetingStatusTone(status) {
  return {
    0: 'info',
    1: 'success',
    2: 'neutral',
    3: 'warning'
  }[status] || 'neutral'
}

export function buildWeekDays(anchorDate) {
  const anchor = parseDate(anchorDate)
  const mondayOffset = (anchor.getDay() + 6) % 7
  const start = new Date(anchor.getFullYear(), anchor.getMonth(), anchor.getDate() - mondayOffset)
  return Array.from({ length: 7 }, (_, index) => {
    const date = new Date(start.getFullYear(), start.getMonth(), start.getDate() + index)
    return { date: formatDate(date), day: date.getDate(), weekday: weekdayLabel(date.getDay()) }
  })
}

export function buildMonthGrid(month) {
  const [year, monthNumber] = String(month).split('-').map(Number)
  const firstDay = new Date(year, monthNumber - 1, 1)
  const mondayOffset = (firstDay.getDay() + 6) % 7
  const start = new Date(year, monthNumber - 1, 1 - mondayOffset)
  return Array.from({ length: 42 }, (_, index) => {
    const date = new Date(start.getFullYear(), start.getMonth(), start.getDate() + index)
    return {
      date: formatDate(date),
      day: date.getDate(),
      inMonth: date.getFullYear() === year && date.getMonth() === monthNumber - 1
    }
  })
}

export function groupBookingsByRoomAndDay(bookings = []) {
  return bookings.reduce((result, booking) => {
    const roomId = booking?.roomId
    const date = booking?.startTime?.slice(0, 10)
    if (roomId === null || typeof roomId === 'undefined' || !date) {
      return result
    }
    const key = `${roomId}@${date}`
    result[key] ||= []
    result[key].push(booking)
    result[key].sort((left, right) => String(left.startTime).localeCompare(String(right.startTime)))
    return result
  }, {})
}

export function calculateOccupancyRate(bookings = [], roomCount, startTime, endTime) {
  const totalMinutes = Math.max(0, minutesBetween(startTime, endTime) * Math.max(1, Number(roomCount) || 0))
  if (!totalMinutes) {
    return 0
  }
  const bookedMinutes = bookings.reduce((sum, booking) => {
    const start = laterTime(booking.startTime, startTime)
    const end = earlierTime(booking.endTime, endTime)
    return sum + Math.max(0, minutesBetween(start, end))
  }, 0)
  return Math.min(100, Math.round(bookedMinutes / totalMinutes * 100))
}

function parseDate(value) {
  const [year, month, day] = String(value).slice(0, 10).split('-').map(Number)
  return new Date(year, month - 1, day)
}

function formatDate(date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function weekdayLabel(day) {
  return ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][day]
}

function minutesBetween(start, end) {
  return Math.round((new Date(end).getTime() - new Date(start).getTime()) / 60000)
}

function laterTime(left, right) {
  return new Date(left).getTime() >= new Date(right).getTime() ? left : right
}

function earlierTime(left, right) {
  return new Date(left).getTime() <= new Date(right).getTime() ? left : right
}
