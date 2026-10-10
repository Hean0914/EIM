<template>
  <section class="office-page">
    <div class="table-panel">
      <div class="table-actions">
        <div>
          <h1>通讯录</h1>
          <p>维护员工和外部联系人的电话、邮箱和办公电话。</p>
        </div>
        <el-button v-if="canManageOffice" type="primary" :icon="Plus" @click="openCreate">新增联系人</el-button>
      </div>

      <el-form :model="query" inline class="search-form">
        <el-form-item label="姓名"><el-input v-model.trim="query.contactName" clearable /></el-form-item>
        <el-form-item label="部门"><el-input v-model.trim="query.deptName" clearable /></el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" border row-key="id">
        <el-table-column prop="contactName" label="姓名" min-width="120" />
        <el-table-column prop="deptName" label="部门" min-width="140" />
        <el-table-column prop="positionName" label="职位" min-width="140" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="officePhone" label="办公电话" min-width="130" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '显示' : '隐藏' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="canManageOffice" label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑联系人' : '新增联系人'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="姓名" prop="contactName"><el-input v-model.trim="form.contactName" /></el-form-item>
        <el-form-item label="部门"><el-input v-model.trim="form.deptName" /></el-form-item>
        <el-form-item label="职位"><el-input v-model.trim="form.positionName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model.trim="form.phone" /></el-form-item>
        <el-form-item label="邮箱" prop="email"><el-input v-model.trim="form.email" /></el-form-item>
        <el-form-item label="办公电话"><el-input v-model.trim="form.officePhone" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio-button :label="1">显示</el-radio-button>
            <el-radio-button :label="0">隐藏</el-radio-button>
          </el-radio-group>
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
import { createAddressBook, deleteAddressBook, listAddressBookPage, updateAddressBook } from '@/api/addressBook'
import { useUserStore } from '@/store/user'

const emptyForm = {
  id: null,
  employeeId: null,
  contactName: '',
  deptName: '',
  positionName: '',
  phone: '',
  email: '',
  officePhone: '',
  sortOrder: 0,
  status: 1
}

const query = reactive({ current: 1, size: 100, contactName: '', deptName: '', status: null })
const form = reactive({ ...emptyForm })
const userStore = useUserStore()
const canManageOffice = computed(() => userStore.canManageOffice)
const formRef = ref()
const tableData = ref([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const rules = {
  contactName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

onMounted(fetchData)

async function fetchData() {
  loading.value = true
  try {
    const result = await listAddressBookPage(cleanQuery())
    tableData.value = result.data?.records || []
  } finally {
    loading.value = false
  }
}

function cleanQuery() {
  return Object.fromEntries(Object.entries(query).filter(([, value]) => value !== null && value !== ''))
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
  if (!valid) return
  submitting.value = true
  try {
    if (form.id) {
      await updateAddressBook({ ...form })
      ElMessage.success('联系人已更新')
    } else {
      await createAddressBook({ ...form })
      ElMessage.success('联系人已新增')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除联系人【${row.contactName}】？`, '删除确认', { type: 'warning' })
  await deleteAddressBook(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

function handleSearch() {
  fetchData()
}

function handleReset() {
  Object.assign(query, { current: 1, size: 100, contactName: '', deptName: '', status: null })
  fetchData()
}
</script>

<style scoped>
.office-page {
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
</style>
