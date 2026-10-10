import request from '@/utils/request'

export function chatWithAssistant(message) {
  return request({
    url: '/ai/assistant/chat',
    method: 'post',
    // Local model inference can take longer than the 10-second default API timeout.
    timeout: 120000,
    data: { message }
  })
}
