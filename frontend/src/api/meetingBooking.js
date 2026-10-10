import request from '@/utils/request'

export function listMeetingBookingPage(params) {
  return request({
    url: '/meeting-bookings/page',
    method: 'get',
    params
  })
}

export function listMeetingBookingCalendar(params) {
  return request({
    url: '/meeting-bookings/calendar',
    method: 'get',
    params
  })
}

export function createMeetingBooking(data) {
  return request({
    url: '/meeting-bookings',
    method: 'post',
    data
  })
}

export function cancelMeetingBooking(id) {
  return request({
    url: `/meeting-bookings/${id}/cancel`,
    method: 'post'
  })
}

export function deleteMeetingBooking(id) {
  return request({
    url: `/meeting-bookings/${id}`,
    method: 'delete'
  })
}
