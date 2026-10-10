import test from 'node:test'
import assert from 'node:assert/strict'
import {
  buildMonthGrid,
  buildWeekDays,
  calculateOccupancyRate,
  groupBookingsByRoomAndDay,
  meetingStatusLabel
} from '../src/utils/meeting-calendar-view.js'

test('builds stable Monday-first month and week ranges', () => {
  const month = buildMonthGrid('2026-09')
  const week = buildWeekDays('2026-09-16')

  assert.equal(month.length, 42)
  assert.equal(month[0].date, '2026-08-31')
  assert.equal(month[1].date, '2026-09-01')
  assert.deepEqual(week.map((item) => item.date), [
    '2026-09-14', '2026-09-15', '2026-09-16', '2026-09-17',
    '2026-09-18', '2026-09-19', '2026-09-20'
  ])
})

test('groups bookings by room and day for the resource week view', () => {
  const grouped = groupBookingsByRoomAndDay([
    { roomId: 1, startTime: '2026-09-16T10:00:00', meetingTitle: 'A' },
    { roomId: 1, startTime: '2026-09-16T14:00:00', meetingTitle: 'B' },
    { roomId: 2, startTime: '2026-09-16T10:00:00', meetingTitle: 'C' }
  ])

  assert.deepEqual(grouped['1@2026-09-16'].map((item) => item.meetingTitle), ['A', 'B'])
  assert.equal(grouped['2@2026-09-16'].length, 1)
})

test('calculates an occupancy percentage from booked minutes', () => {
  const rate = calculateOccupancyRate([
    { startTime: '2026-09-16T10:00:00', endTime: '2026-09-16T12:00:00' },
    { startTime: '2026-09-16T13:00:00', endTime: '2026-09-16T14:00:00' }
  ], 1, '2026-09-16T09:00:00', '2026-09-16T17:00:00')

  assert.equal(rate, 38)
})

test('maps meeting statuses to clear labels', () => {
  assert.equal(meetingStatusLabel(0), '待开始')
  assert.equal(meetingStatusLabel(1), '进行中')
  assert.equal(meetingStatusLabel(3), '已取消')
  assert.equal(meetingStatusLabel(99), '未知状态')
})
