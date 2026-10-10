import request from '@/utils/request'

export function listDepartments(params) {
  return request({
    url: '/departments',
    method: 'get',
    params
  })
}

export function listDepartmentTree(params) {
  return request({
    url: '/departments/tree',
    method: 'get',
    params
  })
}

export function getDepartment(id) {
  return request({
    url: `/departments/${id}`,
    method: 'get'
  })
}

export function createDepartment(data) {
  return request({
    url: '/departments',
    method: 'post',
    data
  })
}

export function updateDepartment(data) {
  return request({
    url: '/departments',
    method: 'put',
    data
  })
}

export function deleteDepartment(id) {
  return request({
    url: `/departments/${id}`,
    method: 'delete'
  })
}
