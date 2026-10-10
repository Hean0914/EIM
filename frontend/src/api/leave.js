import request from '@/utils/request'

export function listLeavePage(params) {
  return request({
    url: '/leaves/page',
    method: 'get',
    params
  })
}

export function createLeave(data) {
  return request({
    url: '/leaves',
    method: 'post',
    data
  })
}

export function approveLeave(id, data) {
  return request({
    url: `/leaves/${id}/approve`,
    method: 'post',
    data
  })
}

export function cancelLeave(id) {
  return request({
    url: `/leaves/${id}/cancel`,
    method: 'post'
  })
}

export function deleteLeave(id) {
  return request({
    url: `/leaves/${id}`,
    method: 'delete'
  })
}
