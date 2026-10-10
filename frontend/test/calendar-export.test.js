import test from 'node:test'
import assert from 'node:assert/strict'
import {
  buildCalendarFile,
  buildMeetingEvent,
  buildPlanEvent,
  escapeIcsText
} from '../src/utils/calendar-export.js'

test('escapes iCalendar text delimiters and line breaks', () => {
  assert.equal(escapeIcsText('主题,备注;下一行\\结束'), '主题\\,备注\\;下一行\\\\结束')
  assert.equal(escapeIcsText('第一行\n第二行'), '第一行\\n第二行')
})

test('builds a timed meeting event in UTC with location and cancellation state', () => {
  const event = buildMeetingEvent({
    id: 12,
    bookingNo: 'MB-12',
    meetingTitle: '评审,会议',
    startTime: '2026-09-23T09:00:00',
    endTime: '2026-09-23T10:30:00',
    roomName: '一号会议室',
    location: '北京;A区',
    remark: '请准备\\方案',
    participantCount: 6,
    status: 3
  })

  assert.match(event, /UID:MB-12@enterprise-information-management/)
  assert.match(event, /DTSTART:20260923T010000Z/)
  assert.match(event, /DTEND:20260923T023000Z/)
  assert.match(event, /SUMMARY:评审\\,会议/)
  assert.match(event, /LOCATION:一号会议室，北京\\;A区/)
  assert.match(event, /DESCRIPTION:请准备\\\\方案\\n参会人数：6/)
  assert.match(event, /STATUS:CANCELLED/)
})

test('builds an all-day plan event with inclusive end date', () => {
  const event = buildPlanEvent({
    id: 7,
    planNo: 'PL-7',
    title: '上线准备',
    content: '完成部署;验收',
    startDate: '2026-09-23',
    endDate: '2026-09-25',
    priority: 3,
    progress: 40,
    status: 1
  })

  assert.match(event, /UID:PL-7@enterprise-information-management/)
  assert.match(event, /DTSTART;VALUE=DATE:20260923/)
  assert.match(event, /DTEND;VALUE=DATE:20260926/)
  assert.match(event, /SUMMARY:上线准备/)
  assert.match(event, /DESCRIPTION:完成部署\\;验收\\n状态：进行中\\n优先级：高\\n进度：40%/)
})

test('builds a calendar with CRLF lines and skips invalid records', () => {
  const ics = buildCalendarFile('会议安排', [
    buildMeetingEvent({
      id: 1,
      meetingTitle: '有效会议',
      startTime: '2026-09-23T09:00:00',
      endTime: '2026-09-23T10:00:00'
    }),
    ''
  ])

  assert.match(ics, /^BEGIN:VCALENDAR\r\n/)
  assert.match(ics, /X-WR-CALNAME:会议安排\r\n/)
  assert.equal((ics.match(/BEGIN:VEVENT/g) || []).length, 1)
  assert.match(ics, /\r\nEND:VCALENDAR\r\n$/)
})
