import request from '@/utils/request'

export function listApprovalRecords(kind, id) {
  const resource = kind === 'out' ? 'outs' : 'leaves'
  return request({
    url: `/${resource}/${id}/approval-records`,
    method: 'get'
  })
}
