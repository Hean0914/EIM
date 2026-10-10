<template>
  <el-drawer
    :model-value="modelValue"
    title="员工画像"
    size="640px"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div v-loading="loading" class="profile-drawer">
      <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
      <template v-if="profile">
        <header class="profile-heading">
          <div class="profile-avatar">{{ initials(profile.employee.employeeName) }}</div>
          <div class="profile-heading__main">
            <div class="profile-heading__title">
              <h2>{{ profile.employee.employeeName || '-' }}</h2>
              <el-tag :type="statusType(profile.employee.employmentStatus)">
                {{ profile.employee.employmentStatusLabel || '-' }}
              </el-tag>
            </div>
            <p>{{ profile.employee.employeeNo || '-' }} · {{ profile.employee.deptName || '未分配部门' }} · {{ profile.employee.positionName || '未分配职位' }}</p>
            <span>入职 {{ profile.employee.hireDate || '-' }} · {{ profile.employee.serviceYears || 0 }} 年工龄</span>
          </div>
        </header>

        <section class="metric-grid" aria-label="员工工作摘要">
          <article v-for="metric in metricCards" :key="metric.key" class="metric-card" :class="`metric-card--${metric.tone}`">
            <span>{{ metric.label }}</span>
            <strong>{{ metric.value }}</strong>
            <small>{{ metric.note }}</small>
          </article>
        </section>

        <section class="profile-section">
          <div class="section-heading">
            <div>
              <h3>考勤趋势</h3>
              <p>近 90 天有记录的工作日</p>
            </div>
            <span class="section-hint">{{ (profile.attendanceTrend || []).length }} 条记录</span>
          </div>
          <div v-if="trendPoints.length" class="trend-chart" aria-label="员工考勤趋势图">
            <div v-for="point in trendPoints" :key="point.date" class="trend-column" :title="`${point.date} ${point.statusText}`">
              <span class="trend-column__bar" :class="`trend-column__bar--${point.tone}`"></span>
              <small>{{ point.date.slice(5) }}</small>
            </div>
          </div>
          <el-empty v-else description="暂无考勤趋势" :image-size="64" />
        </section>

        <section class="profile-section">
          <div class="section-heading">
            <div>
              <h3>工作动态</h3>
              <p>入职、考勤、审批、计划和会议按时间排列</p>
            </div>
          </div>
          <el-timeline v-if="profile.timeline?.length">
            <el-timeline-item
              v-for="item in profile.timeline"
              :key="item.key"
              :timestamp="formatDateTime(item.time)"
              :type="timelineType(item.type)"
              placement="top"
            >
              <div class="timeline-entry">
                <div>
                  <strong>{{ item.title }}</strong>
                  <el-tag v-if="item.status" size="small" effect="plain">{{ item.status }}</el-tag>
                </div>
                <p>{{ item.description || '暂无补充说明' }}</p>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无工作动态" :image-size="64" />
        </section>

        <section class="split-section">
          <div class="profile-section profile-section--half">
            <div class="section-heading">
              <div><h3>进行中计划</h3><p>当前周期内的计划</p></div>
            </div>
            <div v-if="activePlans.length" class="plan-list">
              <article v-for="plan in activePlans" :key="plan.id" class="plan-item">
                <div><strong>{{ plan.title }}</strong><span>{{ formatDateRange(plan.startDate, plan.endDate) }}</span></div>
                <el-progress :percentage="plan.progress || 0" :stroke-width="6" :show-text="false" />
                <small>{{ plan.progress || 0 }}% · {{ planStatus(plan.status) }}</small>
              </article>
            </div>
            <el-empty v-else description="暂无进行中计划" :image-size="54" />
          </div>
          <div class="profile-section profile-section--half">
            <div class="section-heading">
              <div><h3>近期会议</h3><p>未来 90 天的会议安排</p></div>
            </div>
            <div v-if="profile.meetings?.length" class="meeting-list">
              <article v-for="meeting in profile.meetings.slice(0, 5)" :key="meeting.id" class="meeting-item">
                <span class="meeting-item__time">{{ formatDateTime(meeting.startTime) }}</span>
                <strong>{{ meeting.meetingTitle || '未命名会议' }}</strong>
                <small>{{ meeting.roomName || '会议室待定' }} · {{ meeting.participantCount ?? '--' }} 人</small>
              </article>
            </div>
            <el-empty v-else description="暂无近期会议" :image-size="54" />
          </div>
        </section>

        <section class="profile-section">
          <div class="section-heading"><div><h3>联系方式与账号</h3><p>敏感字段由服务端统一脱敏</p></div></div>
          <dl class="info-grid">
            <div><dt>手机号</dt><dd>{{ profile.employee.phone || '-' }}</dd></div>
            <div><dt>邮箱</dt><dd>{{ profile.employee.email || '-' }}</dd></div>
            <div><dt>身份证号</dt><dd>{{ profile.employee.idCard || '-' }}</dd></div>
            <div><dt>紧急联系人</dt><dd>{{ profile.employee.emergencyContact || '-' }}</dd></div>
            <div><dt>紧急电话</dt><dd>{{ profile.employee.emergencyPhone || '-' }}</dd></div>
            <div><dt>系统账号</dt><dd>{{ profile.account?.username || '未分配' }}</dd></div>
            <div><dt>系统角色</dt><dd>{{ profile.account?.roleName || '-' }}</dd></div>
            <div><dt>账号状态</dt><dd>{{ accountStatus(profile.account) }}</dd></div>
            <div class="info-grid__wide"><dt>住址</dt><dd>{{ profile.employee.address || '-' }}</dd></div>
          </dl>
        </section>
      </template>
    </div>
  </el-drawer>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: { type: Boolean, required: true },
  profile: { type: Object, default: null },
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

const metricCards = computed(() => {
  const metrics = props.profile?.metrics || {}
  return [
    { key: 'attendance', label: '本月出勤率', value: `${metrics.attendanceRate || 0}%`, note: `${metrics.actualWorkDays || 0}/${metrics.shouldWorkDays || 0} 天`, tone: 'blue' },
    { key: 'late', label: '迟到 / 早退', value: `${metrics.lateCount || 0} / ${metrics.earlyLeaveCount || 0}`, note: '本月异常', tone: 'amber' },
    { key: 'leave', label: '请假天数', value: metrics.leaveDays || 0, note: `外出 ${metrics.outCount || 0} 次`, tone: 'slate' },
    { key: 'plans', label: '进行中计划', value: metrics.activePlanCount || 0, note: `近期会议 ${metrics.upcomingMeetingCount || 0} 场`, tone: 'green' }
  ]
})

const trendPoints = computed(() => (props.profile?.attendanceTrend || []).map((item) => ({
  ...item,
  tone: item.status === 'NORMAL' ? 'normal' : item.status === 'ABNORMAL' ? 'warning' : 'danger'
})))

const activePlans = computed(() => (props.profile?.plans || []).filter((plan) => plan.status === 1 || plan.status === 3))

function initials(value) {
  return String(value || '?').slice(0, 1)
}

function statusType(status) {
  return { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }[status] || 'info'
}

function timelineType(type) {
  return { employee: 'primary', attendance: 'warning', approval: 'info', plan: 'success', meeting: 'primary' }[type] || 'info'
}

function formatDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : '时间待确认'
}

function formatDateRange(start, end) {
  return `${start || '--'} 至 ${end || '--'}`
}

function planStatus(status) {
  return { 0: '未开始', 1: '进行中', 2: '已完成', 3: '已延期', 4: '已取消' }[status] || '未知'
}

function accountStatus(account) {
  if (!account?.accountAssigned) return '未分配'
  return account.accountStatus === 1 ? '启用' : '停用'
}
</script>

<style scoped>
.profile-drawer { min-height: 100%; }
.profile-heading { display: flex; align-items: center; gap: 14px; padding-bottom: 20px; border-bottom: 1px solid #e5e7eb; }
.profile-avatar { display: grid; flex: 0 0 52px; width: 52px; height: 52px; place-items: center; color: #fff; background: #2563eb; border-radius: 50%; font-size: 22px; font-weight: 700; }
.profile-heading__main { min-width: 0; }
.profile-heading__title { display: flex; align-items: center; gap: 8px; }
.profile-heading h2 { margin: 0; color: #111827; font-size: 21px; }
.profile-heading p { margin: 7px 0 3px; overflow: hidden; color: #374151; font-size: 13px; text-overflow: ellipsis; white-space: nowrap; }
.profile-heading span { color: #6b7280; font-size: 12px; }
.metric-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 10px; margin: 18px 0 4px; }
.metric-card { min-height: 84px; padding: 13px; border-left: 3px solid #2563eb; border-radius: 5px; background: #f8fafc; }
.metric-card span, .metric-card strong, .metric-card small { display: block; }
.metric-card span { color: #6b7280; font-size: 11px; }
.metric-card strong { margin-top: 9px; color: #111827; font-size: 20px; line-height: 1; }
.metric-card small { margin-top: 7px; color: #6b7280; font-size: 11px; }
.metric-card--blue { border-left-color: #2563eb; background: #eff6ff; }
.metric-card--amber { border-left-color: #d97706; background: #fffbeb; }
.metric-card--slate { border-left-color: #64748b; background: #f8fafc; }
.metric-card--green { border-left-color: #16a34a; background: #f0fdf4; }
.profile-section { padding: 20px 0; border-bottom: 1px solid #e5e7eb; }
.section-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; margin-bottom: 14px; }
.section-heading h3 { margin: 0; color: #1f2937; font-size: 15px; }
.section-heading p { margin: 5px 0 0; color: #6b7280; font-size: 12px; }
.section-hint { color: #6b7280; font-size: 12px; }
.trend-chart { display: flex; align-items: flex-end; gap: 4px; height: 132px; padding: 12px 2px 0; overflow-x: auto; border-bottom: 1px solid #d1d5db; }
.trend-column { display: flex; flex: 1 0 12px; flex-direction: column; align-items: center; justify-content: flex-end; min-width: 12px; height: 100%; gap: 6px; }
.trend-column__bar { width: 8px; min-height: 7px; border-radius: 3px 3px 0 0; }
.trend-column__bar--normal { height: 72%; background: #16a34a; }
.trend-column__bar--warning { height: 48%; background: #d97706; }
.trend-column__bar--danger { height: 30%; background: #dc2626; }
.trend-column small { color: #9ca3af; font-size: 9px; transform: rotate(-45deg); transform-origin: center; }
.timeline-entry { padding: 9px 11px; background: #f8fafc; border-left: 3px solid #cbd5e1; border-radius: 4px; }
.timeline-entry > div { display: flex; align-items: center; gap: 8px; }
.timeline-entry strong { color: #1f2937; font-size: 13px; }
.timeline-entry p { margin: 5px 0 0; color: #6b7280; font-size: 12px; line-height: 1.55; }
.split-section { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 18px; }
.profile-section--half { min-width: 0; }
.plan-list, .meeting-list { display: flex; flex-direction: column; gap: 11px; }
.plan-item { padding: 10px; border: 1px solid #e5e7eb; border-radius: 5px; }
.plan-item > div { display: flex; justify-content: space-between; gap: 8px; }
.plan-item strong { overflow: hidden; color: #374151; font-size: 12px; text-overflow: ellipsis; white-space: nowrap; }
.plan-item span, .plan-item small { color: #6b7280; font-size: 11px; }
.plan-item .el-progress { margin-top: 9px; }
.plan-item small { display: block; margin-top: 6px; text-align: right; }
.meeting-item { padding: 10px; border-left: 3px solid #2563eb; background: #eff6ff; border-radius: 4px; }
.meeting-item__time, .meeting-item strong, .meeting-item small { display: block; }
.meeting-item__time { color: #2563eb; font-size: 11px; }
.meeting-item strong { margin-top: 4px; overflow: hidden; color: #1f2937; font-size: 12px; text-overflow: ellipsis; white-space: nowrap; }
.meeting-item small { margin-top: 4px; color: #6b7280; font-size: 11px; }
.info-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px 18px; margin: 0; }
.info-grid div { min-width: 0; }
.info-grid__wide { grid-column: span 2; }
.info-grid dt { color: #9ca3af; font-size: 11px; }
.info-grid dd { margin: 4px 0 0; overflow-wrap: anywhere; color: #374151; font-size: 13px; }
@media (max-width: 560px) {
  .metric-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .split-section { grid-template-columns: 1fr; gap: 0; }
  .info-grid { grid-template-columns: 1fr; }
  .info-grid__wide { grid-column: auto; }
}
</style>
