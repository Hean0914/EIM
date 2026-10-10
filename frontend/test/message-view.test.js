import test from 'node:test'
import assert from 'node:assert/strict'
import { filterMessages, summarizeMessages } from '../src/utils/message-view.js'

const messages = [
  { id: 1, messageType: 'APPROVAL', priority: 3, readStatus: 0, createTime: '2026-09-16T10:00:00' },
  { id: 2, messageType: 'NOTICE', priority: 1, readStatus: 1, createTime: '2026-09-16T11:00:00' },
  { id: 3, messageType: 'PLAN', priority: 2, readStatus: 0, createTime: '2026-09-16T09:00:00' }
]

test('filters messages by category and read state', () => {
  assert.deepEqual(filterMessages(messages, { type: 'APPROVAL' }).map((item) => item.id), [1])
  assert.deepEqual(filterMessages(messages, { readStatus: 0 }).map((item) => item.id), [1, 3])
})

test('summarizes unread and high priority counts', () => {
  assert.deepEqual(summarizeMessages(messages), {
    total: 3,
    unread: 2,
    highPriority: 1,
    categories: 3
  })
})
