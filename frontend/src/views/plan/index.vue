<template>
  <section class="plan-page">
    <header class="plan-header">
      <div>
        <h1>计划管理</h1>
        <p>按状态推进计划，按时间掌握交付节奏。</p>
      </div>
      <div class="plan-header__actions">
        <el-button :icon="Download" :disabled="!filteredPlans.length" @click="exportPlans">导出日历</el-button>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增计划</el-button>
      </div>
    </header>

    <div class="summary-grid">
      <article v-for="item in summaryItems" :key="item.label" :class="['summary-item', `summary-item--${item.key}`]">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}<small>{{ item.unit }}</small></strong>
        <em>{{ item.description }}</em>
      </article>
    </div>

    <div class="workspace-toolbar">
      <el-segmented v-model="activeView" :options="viewOptions" />
      <el-form :model="query" inline class="search-form">
        <el-form-item label="标题">
          <el-input v-model.trim="query.title" clearable placeholder="搜索计划标题" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.planType" clearable placeholder="全部类型" class="filter-select">
            <el-option v-for="item in visibleTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部状态" class="filter-select">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划周期">
          <el-date-picker
            v-model="filterDateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            unlink-panels
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-alert
      v-if="hasHiddenResults"
      title="当前展示前 100 条计划，请缩小筛选范围查看完整结果。"
      type="warning"
      :closable="false"
      show-icon
    />

    <div v-loading="loading" class="workspace-content">
      <div v-if="dataError" class="load-state">
        <el-result icon="error" title="计划数据加载失败" :sub-title="dataError">
          <template #extra>
            <el-button type="primary" :icon="Refresh" @click="fetchData">重新加载</el-button>
          </template>
        </el-result>
      </div>

      <el-empty v-else-if="!filteredPlans.length" description="当前筛选条件下没有计划">
        <el-button type="primary" :icon="Plus" @click="openCreate">新增计划</el-button>
      </el-empty>

      <div v-else-if="activeView === 'kanban'" class="kanban-scroll">
        <PlanKanban
          :groups="planGroups"
          :statuses="statusOptions"
          :type-map="typeMap"
          :can-manage="canManagePlan"
          :deadline-state="deadlineFor"
          :saving-plan-id="savingPlanId"
          @open="openDetail"
          @edit="openEdit"
          @progress="openProgress"
          @delete="handleDelete"
          @status-change="handleStatusChange"
        />
      </div>

      <PlanGantt
        v-else-if="activeView === 'gantt'"
        :plans="filteredPlans"
        :month-value="ganttMonth"
        :today="today"
        :type-map="typeMap"
        @open="openDetail"
        @month-change="ganttMonth = $event"
      />

      <div v-else class="list-panel">
        <el-table :data="filteredPlans" border stripe row-key="id">
          <el-table-column prop="planNo" label="计划编号" min-width="170" />
          <el-table-column label="标题" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <el-button link type="primary" @click="openDetail(row)">{{ row.title }}</el-button>
            </template>
          </el-table-column>
          <el-table-column label="类型" width="110">
            <template #default="{ row }">{{ typeMap[row.planType] || '-' }}</template>
          </el-table-column>
          <el-table-column prop="startDate" label="开始日期" width="120" />
          <el-table-column prop="endDate" label="结束日期" width="120" />
          <el-table-column label="进度" width="170">
            <template #default="{ row }"><el-progress :percentage="row.progress || 0" /></template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="statusTypeMap[row.status]">{{ statusMap[row.status] || '-' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="240" fixed="right">
            <template #default="{ row }">
              <el-button v-if="canManagePlan(row)" link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button v-if="canUpdateProgress(row)" link type="success" @click="openProgress(row)">进度</el-button>
              <el-button v-if="canManagePlan(row)" link type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <PlanDetailDrawer
      v-model="detailVisible"
      :plan="selectedPlan"
      :progress-items="progressItems"
      :progress-loading="progressLoading"
      :progress-error="progressError"
      :today="today"
      :type-map="typeMap"
      :status-map="statusMap"
      :status-type-map="statusTypeMap"
      :can-manage="selectedPlan ? canManagePlan(selectedPlan) : false"
      :can-update-progress="selectedPlan ? canUpdateProgress(selectedPlan) : false"
      @edit="openEdit"
      @progress="openProgress"
      @delete="handleDelete"
      @retry="loadProgressHistory"
    />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑计划' : '新增计划'" width="720px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="计划类型" prop="planType">
          <el-select v-model="form.planType">
            <el-option v-for="item in visibleTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title"><el-input v-model.trim="form.title" /></el-form-item>
        <el-form-item label="内容" prop="content"><el-input v-model.trim="form.content" type="textarea" :rows="4" /></el-form-item>
        <el-form-item v-if="canManageAllPlans" label="部门ID"><el-input-number v-model="form.deptId" :min="1" /></el-form-item>
        <el-form-item v-if="canManageAllPlans" label="员工ID"><el-input-number v-model="form.employeeId" :min="1" /></el-form-item>
        <el-form-item label="日期范围" required>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="form.priority">
            <el-radio-button :value="1">低</el-radio-button>
            <el-radio-button :value="2">中</el-radio-button>
            <el-radio-button :value="3">高</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="progressVisible" title="提交计划进度" width="540px" destroy-on-close>
      <el-form :model="progressForm" label-width="96px">
        <el-form-item label="完成进度">
          <el-slider v-model="progressForm.progress" :min="0" :max="100" show-input />
        </el-form-item>
        <el-form-item label="进度说明">
          <el-input v-model.trim="progressForm.content" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="progressVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleProgress">提交</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Plus, Refresh, Search } from '@element-plus/icons-vue'
import {
  createPlan,
  deletePlan,
  listPlanPage,
  listPlanProgress,
  submitPlanProgress,
  updatePlan,
  updatePlanStatus
} from '@/api/plan'
import { getDeadlineState, groupPlansByStatus, summarizePlans } from '@/utils/plan-view'
import { buildCalendarFile, buildPlanEvent, downloadCalendarFile } from '@/utils/calendar-export'
import { useUserStore } from '@/store/user'
import PlanDetailDrawer from './PlanDetailDrawer.vue'
import PlanGantt from './PlanGantt.vue'
import PlanKanban from './PlanKanban.vue'

const typeOptions = [
  { value: 1, label: '企业计划' },
  { value: 2, label: '部门计划' },
  { value: 3, label: '个人计划' }
]
const statusOptions = [
  { value: 0, label: '未开始', type: 'info', key: 'pending' },
  { value: 1, label: '进行中', type: 'primary', key: 'active' },
  { value: 2, label: '已完成', type: 'success', key: 'completed' },
  { value: 3, label: '已延期', type: 'warning', key: 'delayed' },
  { value: 4, label: '已取消', type: 'danger', key: 'cancelled' }
]
const typeMap = Object.fromEntries(typeOptions.map((item) => [item.value, item.label]))
const statusMap = Object.fromEntries(statusOptions.map((item) => [item.value, item.label]))
const statusTypeMap = Object.fromEntries(statusOptions.map((item) => [item.value, item.type]))
const viewOptions = [
  { label: '看板', value: 'kanban' },
  { label: '甘特图', value: 'gantt' },
  { label: '列表', value: 'list' }
]
const userStore = useUserStore()
const canManageAllPlans = computed(() => userStore.canManageAllPlans)
const userEmployeeId = computed(() => userStore.user?.employeeId)
const visibleTypeOptions = computed(() => (canManageAllPlans.value ? typeOptions : typeOptions.filter((item) => item.value === 3)))
const emptyForm = {
  id: null,
  planType: 1,
  title: '',
  content: '',
  deptId: null,
  employeeId: null,
  priority: 2,
  progress: 0,
  status: 0
}

const query = reactive({ current: 1, size: 100, title: '', planType: null, status: null })
const form = reactive({ ...emptyForm })
const progressForm = reactive({ planId: null, progress: 0, content: '' })
const records = ref([])
const total = ref(0)
const filterDateRange = ref([])
const dateRange = ref([])
const formRef = ref()
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const progressVisible = ref(false)
const activeView = ref('kanban')
const today = formatLocalDate(new Date())
const ganttMonth = ref(`${today.slice(0, 7)}-01`)
const dataError = ref('')
const detailVisible = ref(false)
const selectedPlan = ref(null)
const progressItems = ref([])
const progressLoading = ref(false)
const progressError = ref('')
const savingPlanId = ref(null)
const rules = {
  planType: [{ required: true, message: '请选择计划类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const filteredPlans = computed(() => {
  if (filterDateRange.value.length !== 2) {
    return records.value
  }
  const [start, end] = filterDateRange.value
  return records.value.filter((plan) => plan.startDate <= end && plan.endDate >= start)
})
const planGroups = computed(() => groupPlansByStatus(filteredPlans.value))
const planSummary = computed(() => summarizePlans(filteredPlans.value, today))
const hasHiddenResults = computed(() => total.value > records.value.length)
const summaryItems = computed(() => [
  { key: 'total', label: '当前计划', value: planSummary.value.total, unit: '项', description: '符合当前筛选条件' },
  { key: 'active', label: '进行中', value: planSummary.value.inProgress, unit: '项', description: '正在推进的计划' },
  { key: 'due', label: '7 天内到期', value: planSummary.value.dueSoon, unit: '项', description: '需要优先关注' },
  { key: 'overdue', label: '已逾期', value: planSummary.value.overdue, unit: '项', description: '尚未完成或取消' },
  { key: 'progress', label: '平均完成率', value: planSummary.value.averageProgress, unit: '%', description: '当前计划整体进度' }
])

onMounted(fetchData)

async function fetchData() {
  loading.value = true
  dataError.value = ''
  try {
    const result = await listPlanPage(cleanQuery())
    records.value = result.data?.records || []
    total.value = Number(result.data?.total || 0)
    syncSelectedPlan()
  } catch (error) {
    dataError.value = error?.message || '无法获取计划数据'
  } finally {
    loading.value = false
  }
}

function cleanQuery() {
  return Object.fromEntries(Object.entries(query).filter(([, value]) => value !== null && value !== ''))
}

function openCreate() {
  Object.assign(form, emptyForm)
  normalizeEmployeePlanForm()
  dateRange.value = []
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, emptyForm, row)
  normalizeEmployeePlanForm()
  dateRange.value = [row.startDate, row.endDate]
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid || dateRange.value.length !== 2) {
    ElMessage.warning('请完整填写计划信息')
    return
  }
  const payload = { ...form, startDate: dateRange.value[0], endDate: dateRange.value[1] }
  if (!canManageAllPlans.value) {
    payload.planType = 3
    payload.deptId = null
    payload.employeeId = userEmployeeId.value
  }
  submitting.value = true
  try {
    if (form.id) {
      await updatePlan(payload)
      ElMessage.success('计划已更新')
    } else {
      await createPlan(payload)
      ElMessage.success('计划已新增')
    }
    dialogVisible.value = false
    await fetchData()
  } finally {
    submitting.value = false
  }
}

function openProgress(row) {
  Object.assign(progressForm, { planId: row.id, progress: row.progress || 0, content: '' })
  progressVisible.value = true
}

async function handleProgress() {
  if (!progressForm.content) {
    ElMessage.warning('请输入进度说明')
    return
  }
  submitting.value = true
  try {
    await submitPlanProgress({ ...progressForm })
    ElMessage.success('进度已提交')
    progressVisible.value = false
    await fetchData()
    if (selectedPlan.value?.id === progressForm.planId) {
      await loadProgressHistory()
    }
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除计划【${row.title}】？`, '删除确认', { type: 'warning' })
  await deletePlan(row.id)
  if (selectedPlan.value?.id === row.id) {
    detailVisible.value = false
    selectedPlan.value = null
  }
  ElMessage.success('删除成功')
  await fetchData()
}

async function handleStatusChange({ plan, previousStatus, nextStatus }) {
  if (!canManagePlan(plan) || savingPlanId.value) {
    return
  }
  const previousProgress = plan.progress
  savingPlanId.value = plan.id
  plan.status = nextStatus
  if (nextStatus === 2) {
    plan.progress = 100
  }
  try {
    await updatePlanStatus(plan.id, nextStatus)
    ElMessage.success('计划状态已更新')
    await fetchData()
  } catch {
    plan.status = previousStatus
    plan.progress = previousProgress
    await fetchData()
  } finally {
    savingPlanId.value = null
  }
}

async function openDetail(plan) {
  selectedPlan.value = plan
  detailVisible.value = true
  await loadProgressHistory()
}

async function loadProgressHistory() {
  if (!selectedPlan.value?.id) {
    return
  }
  progressLoading.value = true
  progressError.value = ''
  try {
    const result = await listPlanProgress(selectedPlan.value.id)
    progressItems.value = result.data || []
  } catch (error) {
    progressItems.value = []
    progressError.value = error?.message || '进展记录加载失败'
  } finally {
    progressLoading.value = false
  }
}

function handleSearch() {
  query.current = 1
  fetchData()
}

function handleReset() {
  Object.assign(query, { current: 1, size: 100, title: '', planType: null, status: null })
  filterDateRange.value = []
  fetchData()
}

function exportPlans() {
  const events = filteredPlans.value
    .filter((plan) => plan.startDate && plan.endDate)
    .map(buildPlanEvent)
  if (!events.length) {
    ElMessage.info('当前筛选条件下没有可导出的计划')
    return
  }
  downloadCalendarFile(`计划安排-${today}.ics`, buildCalendarFile('计划安排', events))
  ElMessage.success(`已导出 ${events.length} 项计划`)
}

function normalizeEmployeePlanForm() {
  if (canManageAllPlans.value) {
    return
  }
  form.planType = 3
  form.deptId = null
  form.employeeId = userEmployeeId.value
}

function canManagePlan(row) {
  return canManageAllPlans.value || (row.planType === 3 && row.employeeId === userEmployeeId.value)
}

function canUpdateProgress(row) {
  return canManagePlan(row)
}

function deadlineFor(plan) {
  return getDeadlineState(plan, today)
}

function syncSelectedPlan() {
  if (!selectedPlan.value) {
    return
  }
  const refreshed = records.value.find((plan) => plan.id === selectedPlan.value.id)
  if (refreshed) {
    selectedPlan.value = refreshed
  }
}

function formatLocalDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
</script>

<style scoped>
.plan-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.plan-header,
.workspace-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.plan-header h1 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
}

.plan-header p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
}

.plan-header__actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.summary-item {
  min-height: 116px;
  padding: 16px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-top: 3px solid var(--summary-color, #64748b);
  border-radius: 6px;
}

.summary-item--active { --summary-color: #2563eb; }
.summary-item--due { --summary-color: #d97706; }
.summary-item--overdue { --summary-color: #dc2626; }
.summary-item--progress { --summary-color: #16a34a; }

.summary-item span,
.summary-item em {
  display: block;
  color: #64748b;
  font-size: 12px;
  font-style: normal;
}

.summary-item strong {
  display: block;
  margin: 8px 0;
  color: #0f172a;
  font-size: 26px;
  line-height: 1;
}

.summary-item small {
  margin-left: 4px;
  color: #475569;
  font-size: 12px;
}

.workspace-toolbar {
  align-items: flex-start;
}

.search-form {
  display: flex;
  flex: 1;
  flex-wrap: wrap;
  justify-content: flex-end;
  margin-bottom: -16px;
}

.filter-select {
  width: 150px;
}

.workspace-content {
  min-height: 480px;
}

.kanban-scroll {
  overflow-x: auto;
  padding-bottom: 6px;
}

.list-panel {
  padding: 16px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
}

.load-state {
  min-height: 420px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
}

.el-select,
.el-date-editor {
  width: 100%;
}

@media (max-width: 1180px) {
  .summary-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .workspace-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .search-form {
    justify-content: flex-start;
  }
}

@media (max-width: 720px) {
  .plan-header {
    align-items: flex-start;
  }

  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .summary-item {
    min-height: 104px;
  }
}
</style>
