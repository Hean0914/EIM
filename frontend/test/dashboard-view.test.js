import test from 'node:test'
import assert from 'node:assert/strict'
import { filterDashboardTodos, summarizeDashboardTodos } from '../src/utils/dashboard-view.js'

const todos = [
  { key: 'attendance', category: 'attendance', title: '今日考勤异常', count: 2, priority: 'high' },
  { key: 'plan', category: 'plan', title: '逾期计划', count: 1, priority: 'medium' },
  { key: 'meeting', category: 'meeting', title: '近期会议', count: 3, priority: 'low' }
]

test('filters dashboard todos by category and keeps all items for all mode', () => {
  assert.equal(filterDashboardTodos(todos, 'all').length, 3)
  assert.deepEqual(filterDashboardTodos(todos, 'attendance').map((item) => item.key), ['attendance'])
})

test('summarizes todo count and high priority count', () => {
  assert.deepEqual(summarizeDashboardTodos(todos), { total: 6, highPriority: 2, categories: 3 })
})
