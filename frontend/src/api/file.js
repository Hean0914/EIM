import request from '@/utils/request'

// 不要手动设置 Content-Type，axios 需要自动补上 multipart 的 boundary
export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/files/upload',
    method: 'post',
    data: formData,
    timeout: 30000
  })
}
