const DAY_MS = 24 * 60 * 60 * 1000
const CLOSED_STATUSES = new Set([2, 4])

export function groupPlansByStatus(plans = []) {
  const groups = { 0: [], 1: [], 2: [], 3: [], 4: [] }
  for (const plan of plans) {
    const status = Number(plan?.status)
    if (Object.hasOwn(groups, status)) {
      groups[status].push(plan)
    }
  }
  return groups
}

export function summarizePlans(plans = [], today) {
  const safePlans = Array.isArray(plans) ? plans : []
  const totalProgress = safePlans.reduce((sum, plan) => sum + clampProgress(plan?.progress), 0)
  const deadlineStates = safePlans.map((plan) => getDeadlineState(plan, today))

  return {
    total: safePlans.length,
    inProgress: safePlans.filter((plan) => Number(plan?.status) === 1).length,
    dueSoon: deadlineStates.filter((state) => state.kind === 'due-soon').length,
    overdue: deadlineStates.filter((state) => state.kind === 'overdue').length,
    averageProgress: safePlans.length ? Math.round(totalProgress / safePlans.length) : 0
  }
}

export function getDeadlineState(plan, today) {
  const endDay = toDayNumber(plan?.endDate)
  const todayDay = toDayNumber(today)
  if (endDay === null || todayDay === null) {
    return { kind: 'unknown', days: null, label: '日期待确认' }
  }
  if (CLOSED_STATUSES.has(Number(plan?.status))) {
    return { kind: 'closed', days: Math.abs(endDay - todayDay), label: '已结束' }
  }

  const delta = endDay - todayDay
  if (delta < 0) {
    const days = Math.abs(delta)
    return { kind: 'overdue', days, label: `已逾期 ${days} 天` }
  }
  if (delta === 0) {
    return { kind: 'due-soon', days: 0, label: '今天到期' }
  }
  if (delta <= 7) {
    return { kind: 'due-soon', days: delta, label: `${delta} 天后到期` }
  }
  return { kind: 'normal', days: delta, label: `${delta} 天后到期` }
}

export function buildMonthDays(monthValue) {
  const month = parseDate(monthValue)
  if (!month) {
    return []
  }
  const year = month.year
  const monthIndex = month.month - 1
  const total = new Date(Date.UTC(year, monthIndex + 1, 0)).getUTCDate()

  return Array.from({ length: total }, (_, index) => {
    const day = index + 1
    const date = new Date(Date.UTC(year, monthIndex, day))
    const weekday = date.getUTCDay()
    return {
      key: formatUtcDate(date),
      day,
      weekday,
      weekend: weekday === 0 || weekday === 6
    }
  })
}

export function getGanttSegment(plan, monthValue, today) {
  const month = parseDate(monthValue)
  const planStart = toDayNumber(plan?.startDate)
  const planEnd = toDayNumber(plan?.endDate)
  if (!month || planStart === null || planEnd === null || planEnd < planStart) {
    return null
  }

  const monthStart = Date.UTC(month.year, month.month - 1, 1) / DAY_MS
  const monthEnd = Date.UTC(month.year, month.month, 0) / DAY_MS
  const visibleStart = Math.max(planStart, monthStart)
  const visibleEnd = Math.min(planEnd, monthEnd)
  if (visibleEnd < visibleStart) {
    return null
  }

  return {
    startColumn: visibleStart - monthStart + 1,
    span: visibleEnd - visibleStart + 1,
    progressPercent: clampProgress(plan?.progress),
    overdue: getDeadlineState(plan, today).kind === 'overdue'
  }
}

function clampProgress(value) {
  const progress = Number(value)
  if (!Number.isFinite(progress)) {
    return 0
  }
  return Math.min(100, Math.max(0, progress))
}

function toDayNumber(value) {
  const date = parseDate(value)
  return date ? Date.UTC(date.year, date.month - 1, date.day) / DAY_MS : null
}

function parseDate(value) {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(String(value || ''))
  if (!match) {
    return null
  }
  const year = Number(match[1])
  const month = Number(match[2])
  const day = Number(match[3])
  const date = new Date(Date.UTC(year, month - 1, day))
  if (date.getUTCFullYear() !== year || date.getUTCMonth() !== month - 1 || date.getUTCDate() !== day) {
    return null
  }
  return { year, month, day }
}

function formatUtcDate(date) {
  const year = date.getUTCFullYear()
  const month = String(date.getUTCMonth() + 1).padStart(2, '0')
  const day = String(date.getUTCDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
