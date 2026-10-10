<template>
  <section class="office-page">
    <div class="table-panel">
      <div class="table-actions">
        <div>
          <h1>会议室预定</h1>
          <p>选择可用会议室并登记会议时间，系统会校验时间冲突。</p>
        </div>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增预定</el-button>
      </div>

      <el-form :model="query" inline class="search-form">
        <el-form-item label="会议室">
          <el-select v-model="query.roomId" clearable placeholder="全部会议室" class="filter-select">
            <el-option v-for="room in roomOptions" :key="room.id" :label="room.roomName" :value="room.id" />
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
        <el-table-column prop="bookingNo" label="预定单号" min-width="170" />
        <el-table-column label="会议室" min-width="130">
          <template #default="{ row }">{{ getRoomName(row.roomId) }}</template>
        </el-table-column>
        <el-table-column prop="meetingTitle" label="会议主题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="participantCount" label="人数" width="80" />
        <el-table-column prop="startTime" label="开始时间" min-width="170" />
        <el-table-column prop="endTime" label="结束时间" min-width="170" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTypeMap[row.status]">{{ statusMap[row.status] || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="warning" :disabled="row.status === 3" @click="handleCancel(row)">取消</el-button>
            <el-button v-if="canManageOffice" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" title="新增会议预定" width="620px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="会议室" prop="roomId">
          <el-select v-model="form.roomId" placeholder="请选择会议室">
            <el-option v-for="room in roomOptions" :key="room.id" :label="room.roomName" :value="room.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="会议主题" prop="meetingTitle">
          <el-input v-model.trim="form.meetingTitle" />
        </el-form-item>
        <el-form-item label="参会人数">
          <el-input-number v-model="form.participantCount" :min="0" />
        </el-form-item>
        <el-form-item label="时间范围" required>
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            value-format="YYYY-MM-DDTHH:mm:ss"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model.trim="form.remark" type="textarea" :rows="3" />
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
import { cancelMeetingBooking, createMeetingBooking, deleteMeetingBooking, listMeetingBookingPage } from '@/api/meetingBooking'
import { listAvailableMeetingRooms } from '@/api/meetingRoom'
import { useUserStore } from '@/store/user'

const statusOptions = [
  { value: 0, label: '待开始', type: 'info' },
  { value: 1, label: '进行中', type: 'success' },
  { value: 2, label: '已结束', type: 'info' },
  { value: 3, label: '已取消', type: 'warning' }
]
const statusMap = Object.fromEntries(statusOptions.map((item) => [item.value, item.label]))
const statusTypeMap = Object.fromEntries(statusOptions.map((item) => [item.value, item.type]))
const emptyForm = { roomId: null, meetingTitle: '', participantCount: 0, remark: '' }
const userStore = useUserStore()
const canManageOffice = computed(() => userStore.canManageOffice)

const query = reactive({ current: 1, size: 100, roomId: null, status: null })
const form = reactive({ ...emptyForm })
const timeRange = ref([])
const formRef = ref()
const roomOptions = ref([])
const tableData = ref([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const rules = {
  roomId: [{ required: true, message: '请选择会议室', trigger: 'change' }],
  meetingTitle: [{ required: true, message: '请输入会议主题', trigger: 'blur' }]
}

onMounted(async () => {
  await fetchRooms()
  fetchData()
})

async function fetchRooms() {
  const result = await listAvailableMeetingRooms()
  roomOptions.value = result.data || []
}

async function fetchData() {
  loading.value = true
  try {
    const result = await listMeetingBookingPage(cleanQuery())
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
  timeRange.value = []
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid || timeRange.value.length !== 2) {
    ElMessage.warning('请完整填写预定信息')
    return
  }
  submitting.value = true
  try {
    await createMeetingBooking({ ...form, startTime: timeRange.value[0], endTime: timeRange.value[1] })
    ElMessage.success('预定成功')
    dialogVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

async function handleCancel(row) {
  await cancelMeetingBooking(row.id)
  ElMessage.success('已取消')
  fetchData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除预定【${row.bookingNo}】？`, '删除确认', { type: 'warning' })
  await deleteMeetingBooking(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

function handleSearch() {
  fetchData()
}

function handleReset() {
  Object.assign(query, { current: 1, size: 100, roomId: null, status: null })
  fetchData()
}

function getRoomName(id) {
  return roomOptions.value.find((room) => room.id === id)?.roomName || '-'
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

.el-select,
.el-date-editor {
  width: 100%;
}
</style>
