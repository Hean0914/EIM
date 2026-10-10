import request from '@/utils/request'

export function listPlanPage(params) {
  return request({
    url: '/plans/page',
    method: 'get',
    params
  })
}

export function createPlan(data) {
  return request({
    url: '/plans',
    method: 'post',
    data
  })
}

export function updatePlan(data) {
  return request({
    url: '/plans',
    method: 'put',
    data
  })
}

export function updatePlanStatus(id, status) {
  return request({
    url: `/plans/${id}/status`,
    method: 'put',
    data: { status }
  })
}

export function submitPlanProgress(data) {
  return request({
    url: '/plans/progress',
    method: 'post',
    data
  })
}

export function listPlanProgress(id) {
  return request({
    url: `/plans/${id}/progress`,
    method: 'get'
  })
}

export function deletePlan(id) {
  return request({
    url: `/plans/${id}`,
    method: 'delete'
  })
}
