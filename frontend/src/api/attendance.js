import request from '@/utils/request'

export function checkIn(data) {
  return request({
    url: '/attendance/check-in',
    method: 'post',
    data
  })
}

export function checkOut(data) {
  return request({
    url: '/attendance/check-out',
    method: 'post',
    data
  })
}

export function listAttendanceRecords(params) {
  return request({
    url: '/attendance/records/page',
    method: 'get',
    params
  })
}

export function listAttendanceStats(params) {
  return request({
    url: '/attendance/stats',
    method: 'get',
    params
  })
}

export function getAttendanceCalendar(params) {
  return request({
    url: '/attendance/calendar',
    method: 'get',
    params
  })
}

export function getAttendanceAnalysis(params) {
  return request({
    url: '/attendance/analysis',
    method: 'get',
    params
  })
}

export function getAttendanceRule() {
  return request({
    url: '/attendance/rule',
    method: 'get'
  })
}

export function updateAttendanceRule(data) {
  return request({
    url: '/attendance/rule',
    method: 'put',
    data
  })
}

export function getAttendanceLocation() {
  return request({
    url: '/attendance/location',
    method: 'get'
  })
}

export function updateAttendanceLocation(data) {
  return request({
    url: '/attendance/location',
    method: 'put',
    data
  })
}
