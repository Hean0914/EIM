<template>
  <section class="department-page">
    <div class="department-toolbar">
      <el-form :model="query" inline>
        <el-form-item label="部门名称">
          <el-input v-model.trim="query.deptName" clearable placeholder="请输入部门名称" />
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
          <h1>部门管理</h1>
          <p>维护企业组织架构、上下级部门和启停状态。</p>
        </div>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增部门</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        row-key="id"
        border
        default-expand-all
        class="department-table"
      >
        <el-table-column prop="deptName" label="部门名称" min-width="180" />
        <el-table-column prop="deptCode" label="部门编码" min-width="140" />
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
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑部门' : '新增部门'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="上级部门" prop="parentId">
          <el-select v-model="form.parentId" clearable placeholder="请选择上级部门">
            <el-option label="根部门" :value="0" />
            <el-option
              v-for="item in parentOptions"
              :key="item.id"
              :label="item.deptName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="部门编码" prop="deptCode">
          <el-input v-model.trim="form.deptCode" placeholder="请输入部门编码" />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model.trim="form.deptName" placeholder="请输入部门名称" />
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
import {
  createDepartment,
  deleteDepartment,
  listDepartments,
  listDepartmentTree,
  updateDepartment
} from '@/api/department'

const statusOptions = [
  { value: 1, label: '启用' },
  { value: 0, label: '禁用' }
]

const emptyForm = {
  id: null,
  parentId: 0,
  deptCode: '',
  deptName: '',
  leaderId: null,
  sortOrder: 0,
  status: 1,
  remark: ''
}

const query = reactive({
  deptName: '',
  status: null
})
const tableData = ref([])
const departmentList = ref([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ ...emptyForm })

const isEdit = computed(() => Boolean(form.id))
const parentOptions = computed(() => departmentList.value.filter((item) => item.id !== form.id))

const rules = {
  deptCode: [{ required: true, message: '请输入部门编码', trigger: 'blur' }],
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

onMounted(fetchData)

async function fetchData() {
  loading.value = true
  try {
    const params = cleanQuery()
    const [treeResult, listResult] = await Promise.all([listDepartmentTree(params), listDepartments()])
    tableData.value = treeResult.data || []
    departmentList.value = listResult.data || []
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
  fetchData()
}

function handleReset() {
  query.deptName = ''
  query.status = null
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
      await updateDepartment({ ...form })
      ElMessage.success('部门信息已更新')
    } else {
      await createDepartment({ ...form })
      ElMessage.success('部门已新增')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除部门【${row.deptName}】？`, '删除确认', { type: 'warning' })
    await deleteDepartment(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      throw error
    }
  }
}
</script>

<style scoped>
.department-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.department-toolbar,
.table-panel {
  padding: 18px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.department-toolbar :deep(.el-form-item) {
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

.department-table {
  width: 100%;
}

.el-select {
  width: 100%;
}
</style>
