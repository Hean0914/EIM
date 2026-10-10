import request from '@/utils/request'

export function getAuditLogSummary(params) {
  return request({ url: '/audit-logs/summary', method: 'get', params })
}

export function listAuditLogPage(params) {
  return request({ url: '/audit-logs/page', method: 'get', params })
}
