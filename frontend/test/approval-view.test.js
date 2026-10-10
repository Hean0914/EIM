import test from 'node:test'
import assert from 'node:assert/strict'
import {
  buildApprovalTimeline,
  mergeApprovalApplications,
  summarizeApprovals
} from '../src/utils/approval-view.js'

test('normalizes leave and out applications into one approval feed', () => {
  const records = mergeApprovalApplications(
    [{ id: 1, applyNo: 'LA-001', leaveType: 2, status: 0, employeeId: 7, createTime: '2026-09-15T09:00:00' }],
    [{ id: 2, applyNo: 'OA-001', outType: 1, status: 1, employeeId: 8, createTime: '2026-09-15T10:00:00' }]
  )

  assert.deepEqual(records.map((item) => [item.kind, item.kindLabel, item.typeLabel, item.statusLabel]), [
    ['leave', '请假', '病假', '待审批'],
    ['out', '外出', '公出', '已通过']
  ])
})

test('summarizes all approval statuses and business types', () => {
  const records = mergeApprovalApplications(
    [
      { id: 1, status: 0 },
      { id: 2, status: 1 },
      { id: 3, status: 2 }
    ],
    [{ id: 4, status: 3 }]
  )

  assert.deepEqual(summarizeApprovals(records), {
    total: 4,
    pending: 1,
    approved: 1,
    rejected: 1,
    cancelled: 1,
    leaveCount: 3,
    outCount: 1
  })
})

test('builds a newest-first timeline with a submitted node', () => {
  const application = mergeApprovalApplications([
    {
      id: 9,
      applyNo: 'LA-009',
      leaveType: 1,
      status: 2,
      createTime: '2026-09-14T08:00:00',
      reason: '家庭事务'
    }
  ], [])[0]
  const timeline = buildApprovalTimeline(application, [
    { id: 2, approvalResult: 2, approvalComment: '时间冲突', approvalTime: '2026-09-15T09:00:00', approverId: 1 },
    { id: 1, approvalResult: 1, approvalComment: '已审核', approvalTime: '2026-09-14T10:00:00', approverId: 2 }
  ])

  assert.deepEqual(timeline.map((item) => item.label), ['已驳回', '审批通过', '已提交'])
  assert.equal(timeline[0].comment, '时间冲突')
  assert.equal(timeline[2].comment, '家庭事务')
})

test('returns an empty summary and submitted timeline for empty records', () => {
  assert.deepEqual(summarizeApprovals([]), {
    total: 0,
    pending: 0,
    approved: 0,
    rejected: 0,
    cancelled: 0,
    leaveCount: 0,
    outCount: 0
  })

  const application = mergeApprovalApplications([{ id: 3, status: 0, createTime: '2026-09-15T08:00:00' }], [])[0]
  assert.equal(buildApprovalTimeline(application, []).length, 1)
})
