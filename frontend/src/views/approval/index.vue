<template>
  <section class="approval-center">
    <header class="page-header">
      <div>
        <h1>审批中心</h1>
        <p>集中查看申请状态，沿着时间线追踪每一步处理结果。</p>
      </div>
      <div class="header-actions">
        <el-button :icon="DocumentChecked" @click="router.push('/leaves')">请假管理</el-button>
        <el-button type="primary" :icon="Position" @click="router.push('/outs')">外出管理</el-button>
      </div>
    </header>

    <div class="summary-grid">
      <article v-for="item in summaryItems" :key="item.key" :class="['summary-item', `summary-item--${item.key}`]">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}<small>{{ item.unit }}</small></strong>
        <em>{{ item.description }}</em>
      </article>
    </div>

    <section class="workspace-toolbar">
      <el-segmented v-model="viewMode" :options="viewOptions" />
      <el-form :model="query" inline class="search-form">
        <el-form-item label="类型">
          <el-select v-model="query.kind" clearable placeholder="全部类型" class="filter-select">
            <el-option label="请假" value="leave" />
            <el-option label="外出" value="out" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部状态" class="filter-select">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="canApprove" label="员工">
          <el-select v-model="query.employeeId" clearable filterable placeholder="全部员工" class="employee-select">
            <el-option
              v-for="item in employeeOptions"
              :key="item.id"
              :label="`${item.employeeName}（${item.employeeNo}）`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model.trim="query.keyword" clearable placeholder="申请单号或事由" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </section>

    <el-alert
      v-if="hasHiddenResults"
      title="审批中心当前展示每类申请前 100 条，请缩小筛选范围查看完整结果。"
      type="warning"
      :closable="false"
      show-icon
    />
    <el-alert
      v-if="partialError"
      :title="partialError"
      type="warning"
      :closable="false"
      show-icon
    />

    <div v-loading="loading" class="workspace-content">
      <div v-if="dataError" class="load-state">
        <el-result icon="error" title="审批数据加载失败" :sub-title="dataError">
          <template #extra>
            <el-button type="primary" :icon="Refresh" @click="fetchData">重新加载</el-button>
          </template>
        </el-result>
      </div>

      <el-empty v-else-if="!filteredRecords.length" description="当前筛选条件下没有申请">
        <div class="empty-actions">
          <el-button type="primary" :icon="DocumentChecked" @click="router.push('/leaves')">提交请假申请</el-button>
          <el-button :icon="Position" @click="router.push('/outs')">提交外出登记</el-button>
        </div>
      </el-empty>

      <div v-else class="approval-feed">
        <article
          v-for="application in filteredRecords"
          :key="`${application.kind}-${application.id}`"
          class="approval-card"
          tabindex="0"
          @click="openDetail(application)"
          @keydown.enter.prevent="openDetail(application)"
        >
          <div class="card-heading">
            <div class="heading-tags">
              <el-tag size="small" effect="plain">{{ application.kindLabel }}</el-tag>
              <el-tag size="small" effect="plain">{{ application.typeLabel }}</el-tag>
              <el-tag size="small" :type="application.statusType">{{ application.statusLabel }}</el-tag>
            </div>
            <span class="apply-no">{{ application.applyNo || '申请单' }}</span>
          </div>

          <div class="card-title-row">
            <h2>{{ application.title }}</h2>
            <span>{{ formatTime(application.createTime) }}</span>
          </div>
          <div class="card-meta">
            <span><el-icon><User /></el-icon>{{ application.employeeName || `员工 ${application.employeeId || '-'}` }}</span>
            <span><el-icon><Calendar /></el-icon>{{ formatRange(application) }}</span>
          </div>
          <p class="card-reason">{{ application.reason || '未填写申请说明' }}</p>

          <div class="card-footer" @click.stop>
            <span class="timeline-hint">点击查看流程时间线</span>
            <div class="card-actions">
              <el-button v-if="canApprove && application.status === 0" link type="primary" @click="openApprove(application)">
                审批
              </el-button>
              <el-button v-if="application.status === 0" link type="warning" @click="handleCancel(application)">
                撤销
              </el-button>
            </div>
          </div>
        </article>
      </div>
    </div>

    <ApprovalDetailDrawer
      v-model="detailVisible"
      :application="selectedApplication"
      :timeline="timeline"
      :loading="timelineLoading"
      :error="timelineError"
      :can-approve="canApprove"
      :can-cancel="Boolean(selectedApplication?.status === 0)"
      @approve="openApprove"
      @cancel="handleCancel"
      @retry="loadTimeline"
    />

    <el-dialog v-model="approveVisible" title="处理审批" width="520px" destroy-on-close>
      <el-form :model="approveForm" label-width="96px">
        <el-form-item label="审批结果">
          <el-radio-group v-model="approveForm.result">
            <el-radio-button :label="1">通过</el-radio-button>
            <el-radio-button :label="2">驳回</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model.trim="approveForm.comment" type="textarea" :rows="3" placeholder="填写处理意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleApprove">确定</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar, DocumentChecked, Position, Refresh, Search, User } from '@element-plus/icons-vue'
import { listApprovalRecords } from '@/api/approval'
import { approveLeave, cancelLeave, listLeavePage } from '@/api/leave'
import { approveOut, cancelOut, listOutPage } from '@/api/out'
import { listEmployeePage } from '@/api/employee'
import { useUserStore } from '@/store/user'
import {
  buildApprovalTimeline,
  mergeApprovalApplications,
  summarizeApprovals
} from '@/utils/approval-view'
import ApprovalDetailDrawer from './ApprovalDetailDrawer.vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const userStore = useUserStore()
const canApprove = computed(() => userStore.canApprove)
const userEmployeeId = computed(() => userStore.user?.employeeId)

const statusOptions = [
  { value: 0, label: '待审批' },
  { value: 1, label: '已通过' },
  { value: 2, label: '已驳回' },
  { value: 3, label: '已撤销' }
]
const viewOptions = [
  { label: '全部申请', value: 'all' },
  { label: '待审批', value: 'pending' },
  { label: '我的申请', value: 'mine' }
]
const query = reactive({ kind: '', status: null, employeeId: null, keyword: '' })
const viewMode = ref('all')
const employeeOptions = ref([])
const leaveRecords = ref([])
const outRecords = ref([])
const leaveTotal = ref(0)
const outTotal = ref(0)
const loading = ref(false)
const submitting = ref(false)
const dataError = ref('')
const partialError = ref('')
const detailVisible = ref(false)
const selectedApplication = ref(null)
const timeline = ref([])
const timelineLoading = ref(false)
const timelineError = ref('')
const approveVisible = ref(false)
const approveTarget = ref(null)
const approveForm = reactive({ result: 1, comment: '' })

const employeeNameMap = computed(() => {
  const map = {}
  for (const employee of employeeOptions.value) {
    map[employee.id] = employee.employeeName
  }
  if (userEmployeeId.value && userStore.user?.realName) {
    map[userEmployeeId.value] = userStore.user.realName
  }
  return map
})

const records = computed(() => mergeApprovalApplications(leaveRecords.value, outRecords.value).map((item) => ({
  ...item,
  employeeName: employeeNameMap.value[item.employeeId] || item.employeeName
})))

const filteredRecords = computed(() => records.value
  .filter((item) => !query.kind || item.kind === query.kind)
  .filter((item) => query.status === null || item.status === query.status)
  .filter((item) => !query.employeeId || item.employeeId === query.employeeId)
  .filter((item) => viewMode.value !== 'pending' || item.status === 0)
  .filter((item) => viewMode.value !== 'mine' || item.employeeId === userEmployeeId.value)
  .filter((item) => {
    if (!query.keyword) return true
    const keyword = query.keyword.toLowerCase()
    return [item.applyNo, item.reason, item.destination, item.title].some((value) => String(value || '').toLowerCase().includes(keyword))
  })
  .sort((left, right) => String(right.createTime || '').localeCompare(String(left.createTime || ''))))

const summary = computed(() => summarizeApprovals(filteredRecords.value))
const summaryItems = computed(() => [
  { key: 'total', label: '当前申请', value: summary.value.total, unit: '项', description: '符合当前筛选条件' },
  { key: 'pending', label: '待审批', value: summary.value.pending, unit: '项', description: '需要处理的申请' },
  { key: 'approved', label: '已通过', value: summary.value.approved, unit: '项', description: '审批通过的申请' },
  { key: 'rejected', label: '已驳回', value: summary.value.rejected, unit: '项', description: '需要关注的申请' },
  { key: 'types', label: '业务构成', value: `${summary.value.leaveCount}/${summary.value.outCount}`, unit: '请假/外出', description: '当前筛选结果构成' }
])
const hasHiddenResults = computed(() => leaveTotal.value > leaveRecords.value.length || outTotal.value > outRecords.value.length)

onMounted(async () => {
  if (canApprove.value) {
    await loadEmployeeOptions()
  }
  await fetchData()
})

async function loadEmployeeOptions() {
  try {
    const result = await listEmployeePage({ current: 1, size: 100, employmentStatus: 1 })
    employeeOptions.value = result.data?.records || []
  } catch {
    employeeOptions.value = []
  }
}

async function fetchData() {
  loading.value = true
  dataError.value = ''
  partialError.value = ''
  const params = { current: 1, size: 100 }
  if (query.status !== null) params.status = query.status
  if (canApprove.value && query.employeeId) params.employeeId = query.employeeId

  try {
    const [leaveResult, outResult] = await Promise.allSettled([listLeavePage(params), listOutPage(params)])
    const errors = []
    if (leaveResult.status === 'fulfilled') {
      const data = leaveResult.value.data || {}
      leaveRecords.value = data.records || []
      leaveTotal.value = Number(data.total || 0)
    } else {
      leaveRecords.value = []
      leaveTotal.value = 0
      errors.push('请假申请')
    }
    if (outResult.status === 'fulfilled') {
      const data = outResult.value.data || {}
      outRecords.value = data.records || []
      outTotal.value = Number(data.total || 0)
    } else {
      outRecords.value = []
      outTotal.value = 0
      errors.push('外出申请')
    }
    if (errors.length === 2) {
      dataError.value = '请假和外出申请均无法加载'
    } else if (errors.length) {
      partialError.value = `${errors.join('、')}加载失败，当前展示其余可用数据。`
    }
    syncSelectedApplication()
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  fetchData()
}

function handleReset() {
  Object.assign(query, { kind: '', status: null, employeeId: null, keyword: '' })
  viewMode.value = 'all'
  fetchData()
}

async function openDetail(application) {
  selectedApplication.value = application
  detailVisible.value = true
  await loadTimeline()
}

async function loadTimeline() {
  if (!selectedApplication.value?.id) return
  timelineLoading.value = true
  timelineError.value = ''
  try {
    const result = await listApprovalRecords(selectedApplication.value.kind, selectedApplication.value.id)
    timeline.value = buildApprovalTimeline(selectedApplication.value, result.data || [])
  } catch (error) {
    timeline.value = []
    timelineError.value = error?.message || '流程时间线加载失败'
  } finally {
    timelineLoading.value = false
  }
}

function openApprove(application) {
  approveTarget.value = application
  Object.assign(approveForm, { result: 1, comment: '' })
  approveVisible.value = true
}

async function handleApprove() {
  if (!approveTarget.value) return
  submitting.value = true
  try {
    const action = approveTarget.value.kind === 'leave' ? approveLeave : approveOut
    await action(approveTarget.value.id, { ...approveForm })
    ElMessage.success('审批已完成')
    approveVisible.value = false
    await fetchData()
    if (selectedApplication.value?.id === approveTarget.value.id && selectedApplication.value?.kind === approveTarget.value.kind) {
      selectedApplication.value = records.value.find((item) => item.id === approveTarget.value.id && item.kind === approveTarget.value.kind) || null
      if (selectedApplication.value) await loadTimeline()
    }
  } finally {
    submitting.value = false
  }
}

async function handleCancel(application) {
  await ElMessageBox.confirm(`确认撤销申请【${application.applyNo || application.title}】？`, '撤销确认', { type: 'warning' })
  const action = application.kind === 'leave' ? cancelLeave : cancelOut
  await action(application.id)
  ElMessage.success('申请已撤销')
  await fetchData()
  if (selectedApplication.value?.id === application.id && selectedApplication.value?.kind === application.kind) {
    selectedApplication.value = records.value.find((item) => item.id === application.id && item.kind === application.kind) || null
    if (selectedApplication.value) await loadTimeline()
  }
}

function syncSelectedApplication() {
  if (!selectedApplication.value) return
  const refreshed = records.value.find((item) => item.id === selectedApplication.value.id && item.kind === selectedApplication.value.kind)
  if (refreshed) selectedApplication.value = refreshed
}

function formatRange(application) {
  if (!application.startTime && !application.endTime) return '时间待确认'
  return `${formatTime(application.startTime)} 至 ${formatTime(application.endTime)}`
}

function formatTime(value) {
  if (!value) return '时间待确认'
  return String(value).replace('T', ' ').slice(0, 16)
}
</script>

<style scoped>
.approval-center {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header,
.workspace-toolbar,
.card-heading,
.card-title-row,
.card-meta,
.card-footer,
.header-actions,
.heading-tags,
.card-actions,
.empty-actions {
  display: flex;
  align-items: center;
}

.page-header,
.workspace-toolbar {
  justify-content: space-between;
  gap: 16px;
  padding: 18px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.page-header h1 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
}

.page-header p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
}

.header-actions,
.card-actions,
.heading-tags,
.empty-actions {
  gap: 8px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.summary-item {
  min-height: 102px;
  padding: 16px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-top: 3px solid #94a3b8;
  border-radius: 6px;
}

.summary-item--pending { border-top-color: #d97706; }
.summary-item--approved { border-top-color: #16a34a; }
.summary-item--rejected { border-top-color: #dc2626; }
.summary-item--types { border-top-color: #2563eb; }

.summary-item span,
.summary-item em {
  display: block;
}

.summary-item span {
  color: #64748b;
  font-size: 12px;
}

.summary-item strong {
  display: block;
  margin-top: 8px;
  color: #0f172a;
  font-size: 24px;
  line-height: 1;
}

.summary-item small {
  margin-left: 4px;
  color: #64748b;
  font-size: 12px;
  font-weight: 400;
}

.summary-item em {
  margin-top: 8px;
  color: #94a3b8;
  font-size: 11px;
  font-style: normal;
}

.search-form {
  flex: 1;
  justify-content: flex-end;
  margin: 0;
}

.filter-select,
.employee-select {
  width: 150px;
}

.workspace-content {
  min-height: 220px;
}

.approval-feed {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.approval-card {
  display: flex;
  min-height: 230px;
  flex-direction: column;
  padding: 16px;
  cursor: pointer;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  transition: border-color 0.16s ease, box-shadow 0.16s ease;
}

.approval-card:hover,
.approval-card:focus-visible {
  border-color: #93c5fd;
  outline: none;
  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.1);
}

.card-heading,
.card-title-row,
.card-footer {
  justify-content: space-between;
  gap: 10px;
}

.apply-no,
.card-title-row > span,
.timeline-hint {
  color: #94a3b8;
  font-size: 11px;
}

.card-title-row {
  align-items: baseline;
  margin-top: 16px;
}

.card-title-row h2 {
  min-width: 0;
  margin: 0;
  overflow: hidden;
  color: #0f172a;
  font-size: 16px;
  line-height: 1.4;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  flex-wrap: wrap;
  gap: 8px 16px;
  margin-top: 12px;
  color: #64748b;
  font-size: 12px;
}

.card-meta span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.card-reason {
  display: -webkit-box;
  min-height: 42px;
  margin: 14px 0;
  overflow: hidden;
  color: #475569;
  font-size: 13px;
  line-height: 1.6;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.card-footer {
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid #edf2f7;
}

.load-state {
  min-height: 300px;
  background: #fff;
}

@media (max-width: 1180px) {
  .approval-feed {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .page-header,
  .workspace-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .search-form {
    justify-content: flex-start;
  }

  .summary-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .summary-grid,
  .approval-feed {
    grid-template-columns: 1fr;
  }

  .search-form {
    width: 100%;
  }
}
</style>
