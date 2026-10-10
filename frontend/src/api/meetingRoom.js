import request from '@/utils/request'

export function listMeetingRoomPage(params) {
  return request({
    url: '/meeting-rooms/page',
    method: 'get',
    params
  })
}

export function listAvailableMeetingRooms() {
  return request({
    url: '/meeting-rooms/available',
    method: 'get'
  })
}

export function createMeetingRoom(data) {
  return request({
    url: '/meeting-rooms',
    method: 'post',
    data
  })
}

export function updateMeetingRoom(data) {
  return request({
    url: '/meeting-rooms',
    method: 'put',
    data
  })
}

export function deleteMeetingRoom(id) {
  return request({
    url: `/meeting-rooms/${id}`,
    method: 'delete'
  })
}
