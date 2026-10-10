const STATUS_META = {
  0: { label: '待审批', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已驳回', type: 'danger' },
  3: { label: '已撤销', type: 'info' }
}

const LEAVE_TYPES = {
  1: '事假',
  2: '病假',
  3: '年假',
  4: '调休',
  5: '其他'
}

const OUT_TYPES = {
  1: '公出',
  2: '客户拜访',
  3: '其他'
}

const TIMELINE_META = {
  1: { label: '审批通过', type: 'success' },
  2: { label: '已驳回', type: 'danger' },
  3: { label: '已撤销', type: 'info' }
}

export function mergeApprovalApplications(leaves = [], outs = []) {
  return [
    ...normalizeApplications(leaves, 'leave'),
    ...normalizeApplications(outs, 'out')
  ]
}

export function summarizeApprovals(records = []) {
  const summary = {
    total: 0,
    pending: 0,
    approved: 0,
    rejected: 0,
    cancelled: 0,
    leaveCount: 0,
    outCount: 0
  }

  for (const record of Array.isArray(records) ? records : []) {
    summary.total += 1
    if (record.kind === 'leave') {
      summary.leaveCount += 1
    } else if (record.kind === 'out') {
      summary.outCount += 1
    }

    if (record.status === 0) summary.pending += 1
    if (record.status === 1) summary.approved += 1
    if (record.status === 2) summary.rejected += 1
    if (record.status === 3) summary.cancelled += 1
  }

  return summary
}

export function buildApprovalTimeline(application, records = []) {
  if (!application) {
    return []
  }

  const history = (Array.isArray(records) ? records : [])
    .map((record) => {
      const meta = TIMELINE_META[Number(record?.approvalResult)] || { label: '审批更新', type: 'info' }
      return {
        key: `record-${record.id}`,
        label: meta.label,
        type: meta.type,
        time: record.approvalTime || record.createTime || '',
        comment: record.approvalComment || '',
        actor: record.approverName || (record.approverId ? `用户 ${record.approverId}` : '系统')
      }
    })
    .sort((left, right) => String(right.time).localeCompare(String(left.time)))

  history.push({
    key: 'submitted',
    label: '已提交',
    type: 'primary',
    time: application.createTime || '',
    comment: application.reason || '',
    actor: application.employeeName || (application.employeeId ? `员工 ${application.employeeId}` : '申请人')
  })

  return history.sort((left, right) => String(right.time).localeCompare(String(left.time)))
}

function normalizeApplications(applications, kind) {
  const source = Array.isArray(applications) ? applications : []
  return source.map((application) => {
    const status = Number(application?.status)
    const statusMeta = STATUS_META[status] || { label: '未知状态', type: 'info' }
    const typeValue = kind === 'leave' ? application?.leaveType : application?.outType
    return {
      ...application,
      kind,
      kindLabel: kind === 'leave' ? '请假' : '外出',
      typeLabel: (kind === 'leave' ? LEAVE_TYPES : OUT_TYPES)[typeValue] || '其他',
      status,
      statusLabel: statusMeta.label,
      statusType: statusMeta.type,
      title: `${kind === 'leave' ? '请假' : '外出'}申请 · ${(kind === 'leave' ? LEAVE_TYPES : OUT_TYPES)[typeValue] || '其他'}`
    }
  })
}
