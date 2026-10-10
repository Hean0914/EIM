<template>
  <section class="position-page">
    <div class="position-toolbar">
      <el-form :model="query" inline>
        <el-form-item label="职位名称">
          <el-input v-model.trim="query.positionName" clearable placeholder="请输入职位名称" />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="query.deptId" clearable placeholder="全部部门" class="filter-select">
            <el-option v-for="item in departmentOptions" :key="item.id" :label="item.deptName" :value="item.id" />
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
    </div>

    <div class="table-panel">
      <div class="table-actions">
        <div>
          <h1>职位管理</h1>
          <p>维护部门岗位、职位编码、级别和启停状态。</p>
        </div>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增职位</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" row-key="id" border class="position-table">
        <el-table-column prop="positionCode" label="职位编码" min-width="150" />
        <el-table-column prop="positionName" label="职位名称" min-width="150" />
        <el-table-column label="所属部门" min-width="150">
          <template #default="{ row }">{{ getDepartmentName(row.deptId) }}</template>
        </el-table-column>
        <el-table-column prop="positionLevel" label="职位级别" width="110" />
        <el-table-column prop="sortOrder" label="排序" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑职位' : '新增职位'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="所属部门" prop="deptId">
          <el-select v-model="form.deptId" clearable placeholder="请选择部门">
            <el-option label="通用职位" :value="null" />
            <el-option v-for="item in departmentOptions" :key="item.id" :label="item.deptName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="职位编码" prop="positionCode">
          <el-input v-model.trim="form.positionCode" placeholder="请输入职位编码" />
        </el-form-item>
        <el-form-item label="职位名称" prop="positionName">
          <el-input v-model.trim="form.positionName" placeholder="请输入职位名称" />
        </el-form-item>
        <el-form-item label="职位级别" prop="positionLevel">
          <el-input v-model.trim="form.positionLevel" placeholder="例如 P1、P2、M1" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio-button :label="1">启用</el-radio-button>
            <el-radio-button :label="0">禁用</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model.trim="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { listDepartments } from '@/api/department'
import { createPosition, deletePosition, listPositionPage, updatePosition } from '@/api/position'

const statusOptions = [
  { value: 1, label: '启用' },
  { value: 0, label: '禁用' }
]

const emptyForm = {
  id: null,
  deptId: null,
  positionCode: '',
  positionName: '',
  positionLevel: '',
  sortOrder: 0,
  status: 1,
  remark: ''
}

const query = reactive({
  current: 1,
  size: 10,
  deptId: null,
  positionName: '',
  status: null
})
const departmentOptions = ref([])
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ ...emptyForm })

const isEdit = computed(() => Boolean(form.id))

const rules = {
  positionCode: [{ required: true, message: '请输入职位编码', trigger: 'blur' }],
  positionName: [{ required: true, message: '请输入职位名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

onMounted(async () => {
  await fetchDepartments()
  fetchData()
})

async function fetchDepartments() {
  const result = await listDepartments({ status: 1 })
  departmentOptions.value = result.data || []
}

async function fetchData() {
  loading.value = true
  try {
    const result = await listPositionPage(cleanQuery())
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
  query.deptId = null
  query.positionName = ''
  query.status = null
  fetchData()
}

function handleSizeChange() {
  query.current = 1
  fetchData()
}

function openCreate() {
  Object.assign(form, emptyForm)
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, emptyForm, row)
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  submitting.value = true
  try {
    if (isEdit.value) {
      await updatePosition({ ...form })
      ElMessage.success('职位信息已更新')
    } else {
      await createPosition({ ...form })
      ElMessage.success('职位已新增')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除职位【${row.positionName}】？`, '删除确认', { type: 'warning' })
    await deletePosition(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      throw error
    }
  }
}

function getDepartmentName(id) {
  return departmentOptions.value.find((item) => item.id === id)?.deptName || '通用职位'
}
</script>

<style scoped>
.position-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.position-toolbar,
.table-panel {
  padding: 18px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.position-toolbar :deep(.el-form-item) {
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

.position-table {
  width: 100%;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.el-select {
  width: 100%;
}
</style>
