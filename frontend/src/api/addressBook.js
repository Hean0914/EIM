import request from '@/utils/request'

export function listAddressBookPage(params) {
  return request({
    url: '/address-books/page',
    method: 'get',
    params
  })
}

export function createAddressBook(data) {
  return request({
    url: '/address-books',
    method: 'post',
    data
  })
}

export function updateAddressBook(data) {
  return request({
    url: '/address-books',
    method: 'put',
    data
  })
}

export function deleteAddressBook(id) {
  return request({
    url: `/address-books/${id}`,
    method: 'delete'
  })
}
