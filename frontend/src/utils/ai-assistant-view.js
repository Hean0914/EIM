export function createWelcomeMessage(name) {
  const displayName = String(name || '').trim()
  return {
    role: 'assistant',
    content: displayName
      ? `你好，${displayName}。我可以根据你当前有权限查看的工作台数据，帮你查询待办、计划、会议和审批。`
      : '你好，我是企业办公助手。我可以根据你当前有权限查看的工作台数据，帮你查询待办、计划、会议和审批。',
    kind: 'welcome'
  }
}

export function canSendAssistantMessage(message, loading) {
  return !loading && Boolean(String(message || '').trim())
}

export function createAssistantErrorMessage(error) {
  const timedOut = error?.code === 'ECONNABORTED' || error?.code === 'ETIMEDOUT'
  return {
    role: 'assistant',
    content: timedOut
      ? '本地 AI 正在生成回答，等待时间较长，请稍后重试。'
      : '本地 AI 暂时不可用，请确认 Ollama 已启动且 qwen2.5:1.5b 模型可用。',
    kind: 'error'
  }
}
