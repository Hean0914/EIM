<template>
  <section class="employee-page">
    <div class="employee-toolbar">
      <el-form :model="query" inline>
        <el-form-item label="&#x5458;&#x5de5;&#x59d3;&#x540d;">
          <el-input v-model.trim="query.employeeName" clearable placeholder="&#x8bf7;&#x8f93;&#x5165;&#x59d3;&#x540d;" />
        </el-form-item>
        <el-form-item label="&#x90e8;&#x95e8;">
          <el-select v-model="query.deptId" clearable placeholder="&#x5168;&#x90e8;&#x90e8;&#x95e8;" class="filter-select">
            <el-option v-for="item in departmentOptions" :key="item.id" :label="item.deptName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="&#x72b6;&#x6001;">
          <el-select v-model="query.employmentStatus" clearable placeholder="&#x5168;&#x90e8;&#x72b6;&#x6001;" class="filter-select">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">&#x67e5;&#x8be2;</el-button>
          <el-button :icon="Refresh" @click="handleReset">&#x91cd;&#x7f6e;</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-panel">
      <div class="table-actions">
        <div>
          <h1>&#x5458;&#x5de5;&#x6863;&#x6848;&#x7ba1;&#x7406;</h1>
          <p>&#x7ba1;&#x7406;&#x5458;&#x5de5;&#x57fa;&#x672c;&#x4fe1;&#x606f;&#x3001;&#x90e8;&#x95e8;&#x804c;&#x4f4d;&#x548c;&#x8d26;&#x53f7;&#x5206;&#x914d;&#x72b6;&#x6001;&#x3002;</p>
        </div>
        <div class="action-buttons">
          <el-button type="primary" :icon="Plus" @click="openCreate">&#x65b0;&#x589e;</el-button>
          <el-button type="danger" :icon="Delete" :disabled="!selectedRows.length" @click="handleBatchDelete">
            &#x6279;&#x91cf;&#x5220;&#x9664;
          </el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        row-key="id"
        class="employee-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" />
        <el-table-column prop="employeeNo" label="&#x5de5;&#x53f7;" min-width="120" />
        <el-table-column prop="employeeName" label="&#x59d3;&#x540d;" min-width="120" />
        <el-table-column label="&#x90e8;&#x95e8;" min-width="130">
          <template #default="{ row }">{{ getDepartmentName(row.deptId) }}</template>
        </el-table-column>
        <el-table-column label="&#x804c;&#x4f4d;" min-width="130">
          <template #default="{ row }">{{ getPositionName(row.positionId) }}</template>
        </el-table-column>
        <el-table-column label="&#x6027;&#x522b;" width="90">
          <template #default="{ row }">{{ genderMap[row.gender] || '-' }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="&#x624b;&#x673a;&#x53f7;" min-width="130" />
        <el-table-column prop="email" label="&#x90ae;&#x7bb1;" min-width="170" show-overflow-tooltip />
        <el-table-column prop="hireDate" label="&#x5165;&#x804c;&#x65e5;&#x671f;" width="120" />
        <el-table-column label="&#x72b6;&#x6001;" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.employmentStatus)">
              {{ getStatusLabel(row.employmentStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="&#x64cd;&#x4f5c;" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openProfile(row)">画像</el-button>
            <el-button link type="primary" @click="viewAccountRole(row)">账号</el-button>
            <el-button link type="primary" @click="openEdit(row)">&#x7f16;&#x8f91;</el-button>
            <el-button link type="danger" @click="handleDelete(row)">&#x5220;&#x9664;</el-button>
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
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </div>

    <el-dialog v-model="accountRoleVisible" title="账号与岗位角色" width="520px">
      <el-descriptions v-loading="accountRoleLoading" :column="1" border>
        <el-descriptions-item label="员工">{{ accountRole.employeeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="职位">{{ accountRole.positionName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="账号状态">
          <el-tag :type="accountRole.accountAssigned ? 'success' : 'info'">
            {{ accountRole.accountAssigned ? '已分配账号' : '未分配账号' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="登录账号">{{ accountRole.username || '-' }}</el-descriptions-item>
        <el-descriptions-item label="系统角色">{{ accountRole.roleName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色编码">{{ accountRole.roleCode || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <EmployeeProfileDrawer
      v-model="profileVisible"
      :profile="profile"
      :loading="profileLoading"
      :error="profileError"
    />

    <EmployeeDialog
      v-model="dialogVisible"
      :employee="currentEmployee"
      :department-options="departmentOptions"
      :position-options="positionOptions"
      :status-options="statusOptions"
      @success="fetchData"
    />
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Plus, Refresh, Search } from '@element-plus/icons-vue'
import { batchDeleteEmployees, deleteEmployee, getEmployeeAccountRole, getEmployeeProfile, listEmployeePage } from '@/api/employee'
import { listDepartments } from '@/api/department'
import { listPositions } from '@/api/position'
import EmployeeDialog from './EmployeeDialog.vue'
import EmployeeProfileDrawer from './EmployeeProfileDrawer.vue'

const statusOptions = [
  { value: 0, label: '\u79bb\u804c', type: 'info' },
  { value: 1, label: '\u5728\u804c', type: 'success' },
  { value: 2, label: '\u8bd5\u7528', type: 'warning' },
  { value: 3, label: '\u505c\u804c', type: 'danger' }
]

const genderMap = {
  0: '\u672a\u77e5',
  1: '\u7537',
  2: '\u5973'
}

const query = reactive({
  current: 1,
  size: 10,
  employeeName: '',
  deptId: null,
  employmentStatus: null
})

const loading = ref(false)
const departmentOptions = ref([])
const positionOptions = ref([])
const tableData = ref([])
const total = ref(0)
const selectedRows = ref([])
const dialogVisible = ref(false)
const currentEmployee = ref(null)
const accountRoleVisible = ref(false)
const accountRoleLoading = ref(false)
const accountRole = ref({})
const profileVisible = ref(false)
const profileLoading = ref(false)
const profileError = ref('')
const profile = ref(null)

onMounted(async () => {
  await fetchOptions()
  fetchData()
})

async function fetchOptions() {
  const [departmentResult, positionResult] = await Promise.all([
    listDepartments({ status: 1 }),
    listPositions({ status: 1 })
  ])
  departmentOptions.value = departmentResult.data || []
  positionOptions.value = positionResult.data || []
}

async function fetchData() {
  loading.value = true
  try {
    const result = await listEmployeePage(cleanQuery())
    const data = result.data || {}
    tableData.value = data.records || []
    total.value = Number(data.total || 0)
  } finally {
    loading.value = false
  }
}

function cleanQuery() {
  return Object.fromEntries(
    Object.entries(query).filter(([, value]) => value !== '' && value !== null && typeof value !== 'undefined')
  )
}

function handleSearch() {
  query.current = 1
  fetchData()
}

function handleReset() {
  query.current = 1
  query.size = 10
  query.employeeName = ''
  query.deptId = null
  query.employmentStatus = null
  fetchData()
}

function openCreate() {
  currentEmployee.value = null
  dialogVisible.value = true
}

function openEdit(row) {
  currentEmployee.value = { ...row }
  dialogVisible.value = true
}

async function viewAccountRole(row) {
  accountRoleVisible.value = true
  accountRoleLoading.value = true
  accountRole.value = {}
  try {
    const result = await getEmployeeAccountRole(row.id)
    accountRole.value = result.data || {}
  } finally {
    accountRoleLoading.value = false
  }
}

async function openProfile(row) {
  profileVisible.value = true
  profileLoading.value = true
  profileError.value = ''
  profile.value = null
  try {
    const result = await getEmployeeProfile(row.id)
    profile.value = result.data || null
  } catch (error) {
    profileError.value = error?.message || '员工画像加载失败'
  } finally {
    profileLoading.value = false
  }
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

async function handleDelete(row) {
  await ElMessageBox.confirm(
    `\u786e\u8ba4\u5220\u9664\u5458\u5de5\u3010${row.employeeName}\u3011\uff1f`,
    '\u5220\u9664\u786e\u8ba4',
    { type: 'warning' }
  )
  await deleteEmployee(row.id)
  ElMessage.success('\u5220\u9664\u6210\u529f')
  fetchData()
}

async function handleBatchDelete() {
  const ids = selectedRows.value.map((row) => row.id)
  await ElMessageBox.confirm(
    `\u786e\u8ba4\u5220\u9664\u9009\u4e2d\u7684 ${ids.length} \u540d\u5458\u5de5\uff1f`,
    '\u6279\u91cf\u5220\u9664',
    { type: 'warning' }
  )
  await batchDeleteEmployees(ids)
  ElMessage.success('\u6279\u91cf\u5220\u9664\u6210\u529f')
  fetchData()
}

function getDepartmentName(id) {
  return departmentOptions.value.find((item) => item.id === id)?.deptName || '-'
}

function getPositionName(id) {
  return positionOptions.value.find((item) => item.id === id)?.positionName || '-'
}

function getStatusLabel(value) {
  return statusOptions.find((item) => item.value === value)?.label || '-'
}

function getStatusType(value) {
  return statusOptions.find((item) => item.value === value)?.type || 'info'
}
</script>

<style scoped>
.employee-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.employee-toolbar,
.table-panel {
  padding: 18px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.employee-toolbar :deep(.el-form-item) {
  margin-bottom: 0;
}

.filter-select {
  width: 180px;
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
  color: #111827;
  font-size: 20px;
}

.table-actions p {
  margin: 6px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.employee-table {
  width: 100%;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 900px) {
  .table-actions {
    align-items: flex-start;
    flex-direction: column;
  }

  .employee-toolbar :deep(.el-form) {
    display: grid;
    gap: 12px;
  }

  .employee-toolbar :deep(.el-form-item) {
    margin-right: 0;
  }
}
</style>
