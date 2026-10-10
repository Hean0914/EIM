import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'

const currentDir = dirname(fileURLToPath(import.meta.url))
const layoutView = readFileSync(join(currentDir, '../src/layout/Layout.vue'), 'utf8')

test('layout groups navigation by enterprise work areas', () => {
  assert.match(layoutView, /工作空间/)
  assert.match(layoutView, /组织与流程/)
  assert.match(layoutView, /办公协作/)
  assert.match(layoutView, /系统管理/)
  assert.match(layoutView, /class="menu-section-label"/)
})

test('layout supports desktop collapse and mobile drawer controls', () => {
  assert.match(layoutView, /asideCollapsed/)
  assert.match(layoutView, /mobileMenuOpen/)
  assert.match(layoutView, /class="sidebar-toggle"/)
  assert.match(layoutView, /class="mobile-menu-backdrop"/)
  assert.match(layoutView, /:collapse="asideCollapsed"/)
})

test('layout keeps existing permission guards and message entry', () => {
  assert.match(layoutView, /canManageOrganization/)
  assert.match(layoutView, /canManageEmployees/)
  assert.match(layoutView, /canManageMeetingRooms/)
  assert.match(layoutView, /canViewAuditLogs/)
  assert.match(layoutView, /getMessageSummary/)
  assert.match(layoutView, /router\.push\(.*messages/)
})
