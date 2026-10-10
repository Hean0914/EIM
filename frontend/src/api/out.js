import request from '@/utils/request'

export function listOutPage(params) {
  return request({
    url: '/outs/page',
    method: 'get',
    params
  })
}

export function createOut(data) {
  return request({
    url: '/outs',
    method: 'post',
    data
  })
}

export function approveOut(id, data) {
  return request({
    url: `/outs/${id}/approve`,
    method: 'post',
    data
  })
}

export function cancelOut(id) {
  return request({
    url: `/outs/${id}/cancel`,
    method: 'post'
  })
}

export function deleteOut(id) {
  return request({
    url: `/outs/${id}`,
    method: 'delete'
  })
}
