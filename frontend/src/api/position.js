import request from '@/utils/request'

export function listPositionPage(params) {
  return request({
    url: '/positions/page',
    method: 'get',
    params
  })
}

export function listPositions(params) {
  return request({
    url: '/positions',
    method: 'get',
    params
  })
}

export function getPosition(id) {
  return request({
    url: `/positions/${id}`,
    method: 'get'
  })
}

export function createPosition(data) {
  return request({
    url: '/positions',
    method: 'post',
    data
  })
}

export function updatePosition(data) {
  return request({
    url: '/positions',
    method: 'put',
    data
  })
}

export function deletePosition(id) {
  return request({
    url: `/positions/${id}`,
    method: 'delete'
  })
}
