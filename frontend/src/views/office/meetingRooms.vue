<template>
  <section class="office-page">
    <div class="table-panel">
      <div class="table-actions">
        <div>
          <h1>会议室管理</h1>
          <p>维护会议室位置、容量、设备和可用状态。</p>
        </div>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增会议室</el-button>
      </div>

      <el-form :model="query" inline class="search-form">
        <el-form-item label="会议室">
          <el-input v-model.trim="query.roomName" clearable placeholder="请输入会议室名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部状态" class="filter-select">
            <el-option label="停用" :value="0" />
            <el-option label="可用" :value="1" />
            <el-option label="维护中" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" border row-key="id">
        <el-table-column prop="roomCode" label="编码" min-width="130" />
        <el-table-column prop="roomName" label="名称" min-width="150" />
        <el-table-column prop="location" label="位置" min-width="160" />
        <el-table-column prop="capacity" label="容量" width="90" />
        <el-table-column prop="equipment" label="设备" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTypeMap[row.status]">{{ statusMap[row.status] || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑会议室' : '新增会议室'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="编码" prop="roomCode"><el-input v-model.trim="form.roomCode" /></el-form-item>
        <el-form-item label="名称" prop="roomName"><el-input v-model.trim="form.roomName" /></el-form-item>
        <el-form-item label="位置" prop="location"><el-input v-model.trim="form.location" /></el-form-item>
        <el-form-item label="容量"><el-input-number v-model="form.capacity" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio-button :label="1">可用</el-radio-button>
            <el-radio-button :label="0">停用</el-radio-button>
            <el-radio-button :label="2">维护中</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="设备"><el-input v-model.trim="form.equipment" /></el-form-item>
        <el-form-item label="备注"><el-input v-model.trim="form.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { createMeetingRoom, deleteMeetingRoom, listMeetingRoomPage, updateMeetingRoom } from '@/api/meetingRoom'

const statusMap = { 0: '停用', 1: '可用', 2: '维护中' }
const statusTypeMap = { 0: 'info', 1: 'success', 2: 'warning' }
const emptyForm = { id: null, roomCode: '', roomName: '', location: '', capacity: 0, equipment: '', status: 1, remark: '' }

const query = reactive({ current: 1, size: 100, roomName: '', status: null })
const form = reactive({ ...emptyForm })
const formRef = ref()
const tableData = ref([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const rules = {
  roomCode: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  roomName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  location: [{ required: true, message: '请输入位置', trigger: 'blur' }]
}

onMounted(fetchData)

async function fetchData() {
  loading.value = true
  try {
    const result = await listMeetingRoomPage(cleanQuery())
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
      await updateMeetingRoom({ ...form })
      ElMessage.success('会议室已更新')
    } else {
      await createMeetingRoom({ ...form })
      ElMessage.success('会议室已新增')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除会议室【${row.roomName}】？`, '删除确认', { type: 'warning' })
  await deleteMeetingRoom(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

function handleSearch() {
  fetchData()
}

function handleReset() {
  Object.assign(query, { current: 1, size: 100, roomName: '', status: null })
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

.filter-select {
  width: 180px;
}
</style>
