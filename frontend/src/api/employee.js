import request from '@/utils/request'

export function listEmployeePage(params) {
  return request({
    url: '/employees/page',
    method: 'get',
    params
  })
}

export function getEmployee(id) {
  return request({
    url: `/employees/${id}`,
    method: 'get'
  })
}

export function getEmployeeAccountRole(id) {
  return request({
    url: `/employees/${id}/account-role`,
    method: 'get'
  })
}

export function getEmployeeProfile(id) {
  return request({
    url: `/employees/${id}/profile`,
    method: 'get'
  })
}

export function createEmployee(data) {
  return request({
    url: '/employees',
    method: 'post',
    data
  })
}

export function updateEmployee(data) {
  return request({
    url: '/employees',
    method: 'put',
    data
  })
}

export function deleteEmployee(id) {
  return request({
    url: `/employees/${id}`,
    method: 'delete'
  })
}

export function updateEmployeeStatus(id, employmentStatus) {
  return request({
    url: `/employees/${id}/status`,
    method: 'put',
    data: { employmentStatus }
  })
}

export function assignEmployeeAccount(id, data) {
  return request({
    url: `/employees/${id}/account`,
    method: 'post',
    data
  })
}

export function batchDeleteEmployees(ids) {
  return Promise.all(ids.map((id) => deleteEmployee(id)))
}
