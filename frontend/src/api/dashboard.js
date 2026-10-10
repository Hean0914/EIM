import request from '@/utils/request'

export function getAdminDashboard(params = {}) {
  return request({
    url: '/dashboard/admin',
    method: 'get',
    params
  })
}

export function getHrDashboard() {
  return request({
    url: '/dashboard/hr',
    method: 'get'
  })
}

export function getPersonalDashboard() {
  return request({
    url: '/dashboard/personal',
    method: 'get'
  })
}
