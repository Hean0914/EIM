import request from '@/utils/request'

export function listRoles() {
  return request({
    url: '/roles',
    method: 'get'
  })
}

export function listPositionRoleBindings() {
  return request({
    url: '/roles/bindings',
    method: 'get'
  })
}
