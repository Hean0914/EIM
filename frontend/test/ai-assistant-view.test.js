import test from 'node:test'
import assert from 'node:assert/strict'
import {
  canSendAssistantMessage,
  createAssistantErrorMessage,
  createWelcomeMessage
} from '../src/utils/ai-assistant-view.js'

test('creates a welcome message with the current user name', () => {
  assert.deepEqual(createWelcomeMessage('张三'), {
    role: 'assistant',
    content: '你好，张三。我可以根据你当前有权限查看的工作台数据，帮你查询待办、计划、会议和审批。',
    kind: 'welcome'
  })
})

test('falls back to a neutral welcome when the name is missing', () => {
  assert.equal(createWelcomeMessage('').content.startsWith('你好，我是企业办公助手。'), true)
})

test('only allows non-empty messages while idle', () => {
  assert.equal(canSendAssistantMessage('  ', false), false)
  assert.equal(canSendAssistantMessage('本周有哪些会议？', true), false)
  assert.equal(canSendAssistantMessage('本周有哪些会议？', false), true)
})

test('creates a recoverable assistant error message', () => {
  assert.deepEqual(createAssistantErrorMessage(), {
    role: 'assistant',
    content: '本地 AI 暂时不可用，请确认 Ollama 已启动且 qwen2.5:1.5b 模型可用。',
    kind: 'error'
  })
})

test('explains when local inference exceeds the request timeout', () => {
  assert.equal(
    createAssistantErrorMessage({ code: 'ECONNABORTED' }).content,
    '本地 AI 正在生成回答，等待时间较长，请稍后重试。'
  )
})
