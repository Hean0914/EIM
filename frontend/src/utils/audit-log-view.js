const MODULE_LABELS = Object.freeze({
  NOTICE: '公告',
  APPROVAL: '审批',
  PLAN: '计划',
  MEETING: '会议',
  ATTENDANCE: '考勤',
  AUTH: '账号',
  SYSTEM: '系统'
})

export function sortAuditLogs(logs = []) {
  return (Array.isArray(logs) ? logs : []).slice().sort((left, right) => String(right.createTime || '').localeCompare(String(left.createTime || '')))
}

export function auditStatusMeta(status) {
  return Number(status) === 1
    ? { label: '成功', type: 'success' }
    : Number(status) === 0
      ? { label: '失败', type: 'danger' }
      : { label: '未知', type: 'info' }
}

export function auditModuleLabel(module) {
  return MODULE_LABELS[module] || module || '系统'
}

export function formatAuditDetail(detail) {
  return String(detail || '')
    .replace(/(password|token|secret|authorization)\s*=\s*[^\s,;]+/gi, '$1=[REDACTED]')
}
