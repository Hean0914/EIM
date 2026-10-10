import request from '@/utils/request'

export function listNoticePage(params) {
  return request({
    url: '/notices/page',
    method: 'get',
    params
  })
}

export function getNotice(id) {
  return request({
    url: `/notices/${id}`,
    method: 'get'
  })
}

export function createNotice(data) {
  return request({
    url: '/notices',
    method: 'post',
    data
  })
}

export function updateNotice(data) {
  return request({
    url: '/notices',
    method: 'put',
    data
  })
}

export function publishNotice(id) {
  return request({
    url: `/notices/${id}/publish`,
    method: 'post'
  })
}

export function offlineNotice(id) {
  return request({
    url: `/notices/${id}/offline`,
    method: 'post'
  })
}

export function markNoticeRead(id) {
  return request({
    url: `/notices/${id}/read`,
    method: 'post'
  })
}

export function deleteNotice(id) {
  return request({
    url: `/notices/${id}`,
    method: 'delete'
  })
}
