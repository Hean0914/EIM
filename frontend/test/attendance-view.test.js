import test from 'node:test'
import assert from 'node:assert/strict'
import {
  buildAnomalyCards,
  buildAttendanceCalendarGrid,
  groupCalendarItemsByDate,
  statusLabel
} from '../src/utils/attendance-view.js'

test('builds a Monday-first six-week grid without timezone shifts', () => {
  const grid = buildAttendanceCalendarGrid('2026-09', [])

  assert.equal(grid.length, 42)
  assert.equal(grid[0].date, '2026-08-31')
  assert.equal(grid[0].inMonth, false)
  assert.equal(grid[1].date, '2026-09-01')
  assert.equal(grid[1].inMonth, true)
  assert.equal(grid[41].date, '2026-10-11')
})

test('groups all-employee calendar items by day for a compact overview', () => {
  const grouped = groupCalendarItemsByDate([
    { date: '2026-09-01', employeeId: 1, status: 'NORMAL' },
    { date: '2026-09-01', employeeId: 2, status: 'ABSENT' },
    { date: '2026-09-02', employeeId: 1, status: 'LEAVE' }
  ])

  assert.deepEqual(grouped['2026-09-01'], {
    total: 2,
    abnormal: 1,
    leave: 0,
    out: 0,
    items: [
      { date: '2026-09-01', employeeId: 1, status: 'NORMAL' },
      { date: '2026-09-01', employeeId: 2, status: 'ABSENT' }
    ]
  })
})

test('builds analysis cards with stable labels and values', () => {
  assert.deepEqual(buildAnomalyCards({
    attendanceRate: 88,
    lateCount: 2,
    earlyLeaveCount: 1,
    missingCheckInCount: 3,
    missingCheckOutCount: 0,
    absenceCount: 4,
    gpsAbnormalCount: 1
  }), [
    { key: 'attendanceRate', label: '出勤率', value: '88%', tone: 'primary' },
    { key: 'lateCount', label: '迟到', value: 2, tone: 'warning' },
    { key: 'earlyLeaveCount', label: '早退', value: 1, tone: 'warning' },
    { key: 'missingCheckInCount', label: '上班缺卡', value: 3, tone: 'danger' },
    { key: 'missingCheckOutCount', label: '下班缺卡', value: 0, tone: 'danger' },
    { key: 'absenceCount', label: '缺勤', value: 4, tone: 'danger' },
    { key: 'gpsAbnormalCount', label: 'GPS 异常', value: 1, tone: 'info' }
  ])
})

test('maps calendar status codes to user-facing labels', () => {
  assert.equal(statusLabel('NORMAL'), '正常')
  assert.equal(statusLabel('MISSING_CHECK_OUT'), '下班缺卡')
  assert.equal(statusLabel('UNKNOWN'), '未知状态')
})
