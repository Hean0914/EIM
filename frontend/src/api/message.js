import request from '@/utils/request'

export function getMessageSummary() {
  return request({ url: '/messages/summary', method: 'get' })
}

export function listMessagePage(params) {
  return request({ url: '/messages/page', method: 'get', params })
}

export function markMessageRead(id) {
  return request({ url: `/messages/${id}/read`, method: 'post' })
}

export function markAllMessagesRead() {
  return request({ url: '/messages/read-all', method: 'post' })
}
