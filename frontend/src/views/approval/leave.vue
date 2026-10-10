<template>
  <section class="approval-page">
    <div class="table-panel">
      <div class="table-actions">
        <div>
          <h1>请假申请</h1>
          <p>员工提交请假申请，管理员审批通过或驳回。</p>
        </div>
        <el-button type="primary" :icon="Plus" @click="openApply">新增申请</el-button>
      </div>

      <el-form :model="query" inline class="search-form">
        <el-form-item v-if="canApprove" label="员工">
          <el-select v-model="query.employeeId" clearable filterable placeholder="全部员工" class="employee-select">
            <el-option v-for="item in employeeOptions" :key="item.id" :label="`${item.employeeName}（${item.employeeNo}）`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部状态" class="filter-select">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" border row-key="id">
        <el-table-column prop="applyNo" label="申请单号" min-width="170" />
        <el-table-column prop="employeeId" label="员工ID" width="100" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">{{ leaveTypeMap[row.leaveType] || '其他' }}</template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" min-width="170" />
        <el-table-column prop="endTime" label="结束时间" min-width="170" />
        <el-table-column prop="leaveDays" label="天数" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canApprove" link type="primary" :disabled="row.status !== 0" @click="openApprove(row)">审批</el-button>
            <el-button link type="warning" :disabled="row.status !== 0" @click="handleCancel(row)">撤销</el-button>
            <el-button v-if="canApprove" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="query.current"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="fetchData"
        />
      </div>
    </div>

    <el-dialog v-model="applyVisible" title="新增请假申请" width="620px" destroy-on-close>
      <el-form ref="applyFormRef" :model="applyForm" :rules="applyRules" label-width="96px">
        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="applyForm.leaveType" placeholder="请选择">
            <el-option v-for="item in leaveTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围" required>
          <el-date-picker
            v-model="applyTimeRange"
            type="datetimerange"
            value-format="YYYY-MM-DDTHH:mm:ss"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item label="请假天数" prop="leaveDays">
          <el-input-number v-model="applyForm.leaveDays" :min="0.5" :step="0.5" />
        </el-form-item>
        <el-form-item label="请假原因" prop="reason">
          <el-input v-model.trim="applyForm.reason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleApply">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="approveVisible" title="审批请假申请" width="520px" destroy-on-close>
      <el-form :model="approveForm" label-width="96px">
        <el-form-item label="审批结果">
          <el-radio-group v-model="approveForm.result">
            <el-radio-button :label="1">通过</el-radio-button>
            <el-radio-button :label="2">驳回</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model.trim="approveForm.comment" type="textarea" :rows="3" />
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
import { computed, reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { approveLeave, cancelLeave, createLeave, deleteLeave, listLeavePage } from '@/api/leave'
import { listEmployeePage } from '@/api/employee'
import { useUserStore } from '@/store/user'

const leaveTypeOptions = [
  { value: 1, label: '事假' },
  { value: 2, label: '病假' },
  { value: 3, label: '年假' },
  { value: 4, label: '调休' },
  { value: 5, label: '其他' }
]
const leaveTypeMap = Object.fromEntries(leaveTypeOptions.map((item) => [item.value, item.label]))
const statusOptions = [
  { value: 0, label: '待审批', type: 'warning' },
  { value: 1, label: '已通过', type: 'success' },
  { value: 2, label: '已驳回', type: 'danger' },
  { value: 3, label: '已撤销', type: 'info' }
]
const userStore = useUserStore()
const canApprove = computed(() => userStore.canApprove)

const query = reactive({ current: 1, size: 10, employeeId: null, status: null })
const employeeOptions = ref([])
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const submitting = ref(false)
const applyVisible = ref(false)
const approveVisible = ref(false)
const applyFormRef = ref()
const currentRow = ref(null)
const applyTimeRange = ref([])
const applyForm = reactive({ leaveType: 1, leaveDays: 1, reason: '' })
const approveForm = reactive({ result: 1, comment: '' })

const applyRules = {
  leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  leaveDays: [{ required: true, message: '请输入请假天数', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入请假原因', trigger: 'blur' }]
}

onMounted(async () => {
  if (canApprove.value) {
    await loadEmployeeOptions()
  }
  fetchData()
})

async function loadEmployeeOptions() {
  const result = await listEmployeePage({ current: 1, size: 100, employmentStatus: 1 })
  employeeOptions.value = result.data?.records || []
}

async function fetchData() {
  loading.value = true
  try {
    const result = await listLeavePage(cleanQuery())
    const data = result.data || {}
    tableData.value = data.records || []
    total.value = Number(data.total || 0)
  } finally {
    loading.value = false
  }
}

function cleanQuery() {
  return Object.fromEntries(Object.entries(query).filter(([, value]) => value !== null && value !== ''))
}

function openApply() {
  Object.assign(applyForm, { leaveType: 1, leaveDays: 1, reason: '' })
  applyTimeRange.value = []
  applyVisible.value = true
}

async function handleApply() {
  const valid = await applyFormRef.value.validate().catch(() => false)
  if (!valid || applyTimeRange.value.length !== 2) {
    ElMessage.warning('请完整填写申请信息')
    return
  }
  submitting.value = true
  try {
    await createLeave({ ...applyForm, startTime: applyTimeRange.value[0], endTime: applyTimeRange.value[1] })
    ElMessage.success('申请已提交')
    applyVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

function openApprove(row) {
  currentRow.value = row
  Object.assign(approveForm, { result: 1, comment: '' })
  approveVisible.value = true
}

async function handleApprove() {
  submitting.value = true
  try {
    await approveLeave(currentRow.value.id, { ...approveForm })
    ElMessage.success('审批完成')
    approveVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

async function handleCancel(row) {
  await cancelLeave(row.id)
  ElMessage.success('已撤销')
  fetchData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除申请【${row.applyNo}】？`, '删除确认', { type: 'warning' })
  await deleteLeave(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

function handleSearch() {
  query.current = 1
  fetchData()
}

function handleReset() {
  Object.assign(query, { current: 1, size: 10, employeeId: null, status: null })
  fetchData()
}

function handleSizeChange() {
  query.current = 1
  fetchData()
}

function getStatusLabel(value) {
  return statusOptions.find((item) => item.value === value)?.label || '-'
}

function getStatusType(value) {
  return statusOptions.find((item) => item.value === value)?.type || 'info'
}
</script>

<style scoped>
.approval-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.table-panel {
  padding: 18px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.table-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.table-actions h1 {
  margin: 0;
  font-size: 20px;
}

.table-actions p {
  margin: 6px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.search-form {
  margin-bottom: 16px;
}

.filter-select {
  width: 180px;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.el-select,
.el-date-editor {
  width: 100%;
}
.employee-select {
  width: 220px;
}

</style>
