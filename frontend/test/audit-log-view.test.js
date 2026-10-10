import test from 'node:test'
import assert from 'node:assert/strict'
import { auditStatusMeta, formatAuditDetail, sortAuditLogs } from '../src/utils/audit-log-view.js'

test('sorts audit logs newest first', () => {
  const sorted = sortAuditLogs([
    { id: 1, createTime: '2026-09-15T09:00:00' },
    { id: 2, createTime: '2026-09-16T09:00:00' }
  ])
  assert.deepEqual(sorted.map((item) => item.id), [2, 1])
})

test('formats audit status and redacts sensitive values', () => {
  assert.deepEqual(auditStatusMeta(1), { label: '成功', type: 'success' })
  assert.deepEqual(auditStatusMeta(0), { label: '失败', type: 'danger' })
  assert.equal(formatAuditDetail('token=abc password=123 planId=8'), 'token=[REDACTED] password=[REDACTED] planId=8')
})
