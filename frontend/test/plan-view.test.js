import test from 'node:test'
import assert from 'node:assert/strict'
import {
  buildMonthDays,
  getDeadlineState,
  getGanttSegment,
  groupPlansByStatus,
  summarizePlans
} from '../src/utils/plan-view.js'

test('groups plans into all five status columns', () => {
  const grouped = groupPlansByStatus([
    { id: 1, status: 0 },
    { id: 2, status: 1 },
    { id: 3, status: 1 }
  ])

  assert.deepEqual(grouped[0].map((item) => item.id), [1])
  assert.deepEqual(grouped[1].map((item) => item.id), [2, 3])
  assert.deepEqual(grouped[2], [])
  assert.deepEqual(grouped[3], [])
  assert.deepEqual(grouped[4], [])
})

test('summarizes active, due soon, overdue, and average progress', () => {
  const summary = summarizePlans([
    { status: 1, progress: 40, endDate: '2026-09-17' },
    { status: 3, progress: 20, endDate: '2026-09-10' },
    { status: 2, progress: 100, endDate: '2026-09-01' }
  ], '2026-09-14')

  assert.deepEqual(summary, {
    total: 3,
    inProgress: 1,
    dueSoon: 1,
    overdue: 1,
    averageProgress: 53
  })
})

test('returns clear deadline labels for due-soon and overdue plans', () => {
  assert.deepEqual(
    getDeadlineState({ status: 1, endDate: '2026-09-17' }, '2026-09-14'),
    { kind: 'due-soon', days: 3, label: '3 天后到期' }
  )
  assert.deepEqual(
    getDeadlineState({ status: 3, endDate: '2026-09-10' }, '2026-09-14'),
    { kind: 'overdue', days: 4, label: '已逾期 4 天' }
  )
})

test('does not report completed or cancelled plans as overdue', () => {
  assert.equal(getDeadlineState({ status: 2, endDate: '2026-09-01' }, '2026-09-14').kind, 'closed')
  assert.equal(getDeadlineState({ status: 4, endDate: '2026-09-01' }, '2026-09-14').kind, 'closed')
})

test('builds every day for September 2026', () => {
  const days = buildMonthDays('2026-09-01')

  assert.equal(days.length, 30)
  assert.equal(days[0].key, '2026-09-01')
  assert.equal(days[29].key, '2026-09-30')
})

test('builds leap-year February without timezone shifts', () => {
  const days = buildMonthDays('2028-02-01')

  assert.equal(days.length, 29)
  assert.equal(days[28].key, '2028-02-29')
})

test('clips a cross-month plan to the visible month', () => {
  const segment = getGanttSegment({
    startDate: '2026-08-28',
    endDate: '2026-09-05',
    progress: 50,
    status: 1
  }, '2026-09-01', '2026-09-14')

  assert.deepEqual(segment, {
    startColumn: 1,
    span: 5,
    progressPercent: 50,
    overdue: true
  })
})

test('gives a one-day plan one grid cell', () => {
  const segment = getGanttSegment({
    startDate: '2026-09-14',
    endDate: '2026-09-14',
    progress: 0,
    status: 0
  }, '2026-09-01', '2026-09-14')

  assert.equal(segment.startColumn, 14)
  assert.equal(segment.span, 1)
})

test('omits plans outside the visible month', () => {
  assert.equal(getGanttSegment({
    startDate: '2026-10-01',
    endDate: '2026-10-03',
    progress: 0,
    status: 0
  }, '2026-09-01', '2026-09-14'), null)
})

test('clamps invalid progress values for gantt rendering', () => {
  assert.equal(getGanttSegment({
    startDate: '2026-09-01',
    endDate: '2026-09-03',
    progress: 140,
    status: 1
  }, '2026-09-01', '2026-09-14').progressPercent, 100)
})
