import test from 'node:test'
import assert from 'node:assert/strict'
import { effectScope } from 'vue'
import { useLoginDemo } from '../src/utils/loginDemo.js'

test('completing an example task moves it once and updates progress; reset restores it', () => {
  const scope = effectScope()
  const demo = scope.run(useLoginDemo)
  assert.equal(demo.tasks.value.filter((task) => task.status === 'done').length, 1)
  demo.completeTask('summary')
  demo.completeTask('summary')
  assert.equal(demo.tasks.value.filter((task) => task.status === 'done').length, 2)
  assert.equal(demo.tasks.value.find((task) => task.id === 'summary').progress, 100)
  demo.completeTask('unknown')
  assert.equal(demo.tasks.value.length, 3)
  demo.resetTasks()
  assert.equal(demo.tasks.value.find((task) => task.id === 'summary').status, 'active')
  assert.equal(demo.tasks.value.find((task) => task.id === 'summary').progress, 65)
  scope.stop()
})

test('approval advances in order, ignores repeated starts and can replay', (t) => {
  t.mock.timers.enable({ apis: ['setInterval'] })
  const scope = effectScope()
  const demo = scope.run(useLoginDemo)
  demo.startApproval()
  t.mock.timers.tick(1000)
  assert.equal(demo.approvalStep.value, 1)
  demo.startApproval()
  assert.equal(demo.approvalStep.value, 1)
  t.mock.timers.tick(2000)
  assert.equal(demo.approvalStep.value, 3)
  assert.equal(demo.approvalRunning.value, false)
  demo.startApproval()
  assert.equal(demo.approvalStep.value, 0)
  assert.equal(demo.approvalRunning.value, true)
  scope.stop()
  t.mock.timers.tick(5000)
  assert.equal(demo.approvalStep.value, 0)
})

test('leaving approval cancels its timer and date selection changes only example meetings', (t) => {
  t.mock.timers.enable({ apis: ['setInterval'] })
  const scope = effectScope()
  const demo = scope.run(useLoginDemo)
  demo.activePreview.value = 'approval'
  demo.startApproval()
  demo.activePreview.value = 'meeting'
  t.mock.timers.tick(5000)
  assert.equal(demo.approvalRunning.value, false)
  assert.equal(demo.approvalStep.value, 0)
  const firstDay = demo.meetings.value.map((meeting) => meeting.id)
  demo.selectedDay.value = 'tuesday'
  assert.notDeepEqual(demo.meetings.value.map((meeting) => meeting.id), firstDay)
  assert.ok(demo.meetings.value.every((meeting) => meeting.attendees && meeting.location))
  demo.selectedDay.value = 'wednesday'
  assert.equal(demo.meetings.value.length, 0)
  scope.stop()
})
