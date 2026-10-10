import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'
import { parse } from '@vue/compiler-sfc'
import postcss from 'postcss'

const currentDir = dirname(fileURLToPath(import.meta.url))
const loginView = readFileSync(join(currentDir, '../src/views/Login.vue'), 'utf8')

test('brand footer keeps copyright and status in normal flow', () => {
  const { descriptor } = parse(loginView)
  // Check the template hierarchy separately from CSS so moving an item outside is caught.
  const footerContent = descriptor.template.content.match(/<footer class="brand-footer">([\s\S]*?)<\/footer>/)?.[1]
  assert.ok(footerContent, 'brand footer must contain the supporting information')
  for (const name of ['brand-copyright', 'brand-status']) {
    assert.ok(footerContent.includes(`class="${name}"`), `${name} must be in the footer`)
  }
  const css = postcss.parse(descriptor.styles[0].content)
  for (const selector of ['.brand-module-list', '.brand-status']) {
    css.walkRules(selector, (rule) => {
      rule.walkDecls((decl) => {
        assert.ok(!['top', 'right', 'bottom', 'left'].includes(decl.prop), `${selector} must not use offsets`)
        if (decl.prop === 'position') assert.ok(!['absolute', 'fixed'].includes(decl.value))
      })
    })
  }
  css.walkRules('.brand-copyright', (rule) => {
    rule.walkDecls('display', (decl) => assert.notEqual(decl.value, 'none', 'copyright remains visible on mobile'))
  })
})

test('login view exposes the system module navigation visual', () => {
  assert.match(loginView, /class="brand-module-list"/)
  assert.match(loginView, /工作台/)
  assert.match(loginView, /计划管理/)
  assert.match(loginView, /审批流程/)
  assert.match(loginView, /考勤管理/)
  assert.match(loginView, /日常办公/)
  assert.match(loginView, /统一消息/)
})

test('login view links form state to the module navigation', () => {
  assert.match(loginView, /activeModule = 'dashboard'/)
  assert.match(loginView, /activeModule = 'security'/)
  assert.match(loginView, /class="brand-status"/)
  assert.match(loginView, /正在验证登录信息/)
})

test('login view keeps the login contract and accessible states', () => {
  assert.match(loginView, /data-testid="login-username"/)
  assert.match(loginView, /data-testid="login-password"/)
  assert.match(loginView, /data-testid="login-submit"/)
  assert.match(loginView, /prefers-reduced-motion/)
  assert.match(loginView, /aria-live="polite"/)
})

test('login previews are explicitly labeled examples with keyboard-accessible selection', () => {
  assert.match(loginView, /功能示例/)
  assert.match(loginView, /<button[\s\S]*?:aria-pressed="activePreview === preview.key"/)
  assert.match(loginView, /@click="activePreview = preview.key"/)
  for (const key of ['plan', 'approval', 'meeting']) {
    assert.ok(loginView.includes(`activePreview === '${key}'`))
  }
  assert.match(loginView, /<Transition name="preview" mode="out-in">/)
  assert.match(loginView, /autocomplete="username"/)
  assert.match(loginView, /autocomplete="current-password"/)
})

test('login view wires local demo actions without changing the login request', () => {
  assert.match(loginView, /@click="completeTask\(task.id\)"/)
  assert.match(loginView, /@click="resetTasks"/)
  assert.match(loginView, /@click="startApproval"/)
  assert.match(loginView, /@click="selectedDay = day.key"/)
  assert.match(loginView, /<details v-for="meeting in meetings"/)
  assert.match(loginView, /:aria-valuenow="task.progress"/)
  assert.match(loginView, /useLoginDemo\(\)/)
  assert.match(loginView, /await login\(form\)/)
})

test('login form uses a dark shared-theme surface and separates identity from account support', () => {
  const { descriptor } = parse(loginView)
  const formSide = descriptor.template.content.match(/<section class="login-form-side"[\s\S]*?<\/section>/)?.[0]
  assert.match(formSide, /class="login-identity"/)
  assert.match(formSide, /id="login-title">账号登录/)
  assert.ok(formSide.indexOf('class="login-account-note"') > formSide.indexOf('</el-form>'))
  const css = postcss.parse(descriptor.styles[0].content)
  const declarations = (selector) => {
    const result = {}
    css.walkRules(selector, (rule) => rule.walkDecls((decl) => { result[decl.prop] = decl.value }))
    return result
  }
  const panel = declarations('.login-form-side')
  assert.match(panel.background, /var\(--login-surface\)/)
  assert.equal(panel.color, '#f1f5f9')
  const input = declarations('.login-field :deep(.el-input__wrapper)')
  assert.match(input.background, /var\(--color-navigation-bg\)/)
  assert.equal(input['min-height'], '54px')
  assert.equal(declarations('.login-field :deep(.el-input__inner)').color, 'inherit')
})

test('login form motion is restrained and disabled for reduced-motion preference', () => {
  const { descriptor } = parse(loginView)
  const css = postcss.parse(descriptor.styles[0].content)
  let reducedMotionDisablesEntry = false
  css.walkAtRules('media', (rule) => {
    if (!rule.params.includes('prefers-reduced-motion')) return
    rule.walkRules('.login-form-side', (entry) => {
      entry.walkDecls('animation', (decl) => { reducedMotionDisablesEntry = decl.value === 'none' })
    })
  })
  assert.ok(reducedMotionDisablesEntry)
  assert.match(loginView, /@keyframes login-reveal/)
})
