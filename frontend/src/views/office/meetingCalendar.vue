<template>
  <section class="calendar-page">
    <header class="calendar-header">
      <div>
        <h1>会议室日历</h1>
        <p>按会议室查看时间占用，快速找到空闲时段并发起预定。</p>
      </div>
      <div class="calendar-toolbar">
        <el-button circle :icon="ArrowLeft" title="上一个周期" aria-label="上一个周期" @click="shiftPeriod(-1)" />
        <el-date-picker
          v-model="anchorDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="选择日期"
          class="anchor-picker"
          @change="fetchCalendar"
        />
        <el-button circle :icon="ArrowRight" title="下一个周期" aria-label="下一个周期" @click="shiftPeriod(1)" />
        <el-button :icon="Calendar" @click="goToday">今天</el-button>
        <el-radio-group v-model="viewMode" size="small" @change="fetchCalendar">
          <el-radio-button label="week">周视图</el-radio-button>
          <el-radio-button label="month">月视图</el-radio-button>
        </el-radio-group>
        <el-select v-model="roomFilter" clearable placeholder="全部会议室" class="room-filter" @change="fetchCalendar">
          <el-option v-for="room in rooms" :key="room.id" :label="room.roomName" :value="room.id" />
        </el-select>
        <el-select v-model="statusFilter" clearable placeholder="全部状态" class="status-filter" @change="fetchCalendar">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button :icon="Download" :disabled="!bookings.length" @click="exportMeetings">导出日历</el-button>
        <el-button :icon="Refresh" :loading="loading" @click="fetchCalendar">刷新</el-button>
      </div>
    </header>

    <div class="calendar-summary">
      <article class="summary-card summary-card--rooms">
        <span>可用会议室</span>
        <strong>{{ rooms.length }}</strong>
        <small>当前可用于预定</small>
      </article>
      <article class="summary-card summary-card--upcoming">
        <span>待开始会议</span>
        <strong>{{ upcomingCount }}</strong>
        <small>当前周期内待开始</small>
      </article>
      <article class="summary-card summary-card--active">
        <span>进行中会议</span>
        <strong>{{ ongoingCount }}</strong>
        <small>当前周期内进行中</small>
      </article>
      <article class="summary-card summary-card--rate">
        <span>周期占用率</span>
        <strong>{{ occupancyRate }}%</strong>
        <small>{{ periodLabel }}</small>
      </article>
    </div>

    <section v-loading="loading" class="calendar-workspace">
      <div class="workspace-heading">
        <div>
          <h2>{{ periodLabel }}</h2>
          <p>点击会议查看详情，点击空闲格可直接创建预定。</p>
        </div>
        <span class="workspace-count">{{ bookings.length }} 场会议</span>
      </div>

      <div v-if="viewMode === 'week'" class="week-board">
        <div class="week-board__header">
          <div class="room-heading">会议室</div>
          <div v-for="day in weekDays" :key="day.date" class="day-heading">
            <strong>{{ day.weekday }}</strong>
            <span>{{ day.date.slice(5) }}</span>
          </div>
        </div>
        <div v-for="room in visibleRooms" :key="room.id" class="week-board__row">
          <div class="room-label">
            <strong>{{ room.roomName }}</strong>
            <span>{{ room.capacity || 0 }} 人 · {{ room.location || '位置未填写' }}</span>
          </div>
          <div
            v-for="day in weekDays"
            :key="`${room.id}-${day.date}`"
            class="week-cell"
            :class="{ 'week-cell--busy': bookingsFor(room.id, day.date).length }"
            role="button"
            tabindex="0"
            @click="openCreate(room, day.date)"
            @keydown.enter="openCreate(room, day.date)"
          >
            <button
              v-for="booking in bookingsFor(room.id, day.date)"
              :key="booking.id"
              type="button"
              class="booking-chip"
              :class="`booking-chip--${meetingStatusTone(booking.status)}`"
              @click.stop="openDay(day.date)"
            >
              <span>{{ formatTime(booking.startTime) }} - {{ formatTime(booking.endTime) }}</span>
              <strong>{{ booking.meetingTitle }}</strong>
              <small>{{ booking.statusText }} · {{ booking.participantCount || '--' }} 人</small>
            </button>
            <span v-if="!bookingsFor(room.id, day.date).length" class="free-slot">空闲 · 新建</span>
          </div>
        </div>
        <el-empty v-if="!visibleRooms.length" description="暂无可用会议室" />
      </div>

      <div v-else class="month-view">
        <div class="month-weekdays" aria-hidden="true">
          <span v-for="weekday in weekdays" :key="weekday">{{ weekday }}</span>
        </div>
        <div class="month-grid">
          <article
            v-for="day in monthGrid"
            :key="day.date"
            class="month-cell"
            :class="{ 'month-cell--muted': !day.inMonth, 'month-cell--busy': bookingsForDay(day.date).length }"
            tabindex="0"
            @click="openDay(day.date)"
            @keydown.enter="openDay(day.date)"
          >
            <div class="month-cell__top">
              <strong>{{ day.day }}</strong>
              <button type="button" class="month-add" :aria-label="`在 ${day.date} 新建会议`" @click.stop="openCreate(null, day.date)">+</button>
            </div>
            <button
              v-for="booking in bookingsForDay(day.date).slice(0, 3)"
              :key="booking.id"
              type="button"
              class="month-booking"
              :class="`month-booking--${meetingStatusTone(booking.status)}`"
              @click.stop="openDay(day.date)"
            >
              <span>{{ formatTime(booking.startTime) }}</span>
              <strong>{{ booking.meetingTitle }}</strong>
            </button>
            <span v-if="bookingsForDay(day.date).length > 3" class="month-more">+ {{ bookingsForDay(day.date).length - 3 }} 场</span>
            <span v-if="!bookingsForDay(day.date).length" class="month-empty">暂无会议</span>
          </article>
        </div>
      </div>
    </section>

    <el-drawer v-model="dayDrawerVisible" :title="`${selectedDay || ''} 会议安排`" size="440px">
      <div v-if="selectedDayBookings.length" class="day-bookings">
        <p class="day-bookings__summary">共 {{ selectedDayBookings.length }} 场会议，点击空闲时间可新增预定。</p>
        <article v-for="booking in selectedDayBookings" :key="booking.id" class="booking-detail">
          <div class="booking-detail__header">
            <div>
              <strong>{{ booking.meetingTitle }}</strong>
              <span>{{ booking.statusText }}</span>
            </div>
            <el-tag :type="bookingStatusTag(booking.status)" size="small">{{ booking.statusText }}</el-tag>
          </div>
          <dl class="booking-detail__grid">
            <div><dt>会议室</dt><dd>{{ booking.roomName || roomName(booking.roomId) }}</dd></div>
            <div><dt>时间</dt><dd>{{ formatDateTimeRange(booking.startTime, booking.endTime) }}</dd></div>
            <div><dt>地点</dt><dd>{{ booking.location || '--' }}</dd></div>
            <div><dt>参会人数</dt><dd>{{ booking.participantCount ?? '--' }}</dd></div>
            <div><dt>预定人</dt><dd>{{ booking.bookerName || (booking.masked ? '已隐藏' : '--') }}</dd></div>
            <div><dt>设备</dt><dd>{{ booking.equipment || '--' }}</dd></div>
          </dl>
          <p v-if="booking.remark" class="booking-detail__remark">备注：{{ booking.remark }}</p>
          <el-button v-if="canCancel(booking)" type="warning" link @click="handleCancel(booking)">取消我的预定</el-button>
        </article>
      </div>
      <el-empty v-else description="当天暂无会议" />
      <template #footer>
        <el-button type="primary" :icon="Plus" @click="openCreate(null, selectedDay || anchorDate)">新建预定</el-button>
      </template>
    </el-drawer>

    <el-dialog v-model="createVisible" title="新增会议预定" width="620px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="会议室" prop="roomId">
          <el-select v-model="form.roomId" placeholder="请选择会议室">
            <el-option v-for="room in rooms" :key="room.id" :label="room.roomName" :value="room.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="会议主题" prop="meetingTitle"><el-input v-model.trim="form.meetingTitle" /></el-form-item>
        <el-form-item label="参会人数"><el-input-number v-model="form.participantCount" :min="0" /></el-form-item>
        <el-form-item label="时间范围" required>
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            value-format="YYYY-MM-DDTHH:mm:ss"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model.trim="form.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存预定</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowRight, Calendar, Download, Plus, Refresh } from '@element-plus/icons-vue'
import { createMeetingBooking, cancelMeetingBooking, listMeetingBookingCalendar } from '@/api/meetingBooking'
import { listAvailableMeetingRooms } from '@/api/meetingRoom'
import {
  buildMonthGrid,
  buildWeekDays,
  calculateOccupancyRate,
  groupBookingsByRoomAndDay,
  meetingStatusTone
} from '@/utils/meeting-calendar-view'
import { buildCalendarFile, buildMeetingEvent, downloadCalendarFile } from '@/utils/calendar-export'

const weekdays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const statusOptions = [
  { value: 0, label: '待开始', type: 'info' },
  { value: 1, label: '进行中', type: 'success' },
  { value: 2, label: '已结束', type: 'info' },
  { value: 3, label: '已取消', type: 'warning' }
]
const anchorDate = ref(today())
const viewMode = ref('week')
const roomFilter = ref(null)
const statusFilter = ref(null)
const rooms = ref([])
const bookings = ref([])
const loading = ref(false)
const dayDrawerVisible = ref(false)
const selectedDay = ref('')
const createVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const timeRange = ref([])
const form = reactive({ roomId: null, meetingTitle: '', participantCount: 0, remark: '' })
const rules = {
  roomId: [{ required: true, message: '请选择会议室', trigger: 'change' }],
  meetingTitle: [{ required: true, message: '请输入会议主题', trigger: 'blur' }]
}

const weekDays = computed(() => buildWeekDays(anchorDate.value))
const monthGrid = computed(() => buildMonthGrid(anchorDate.value.slice(0, 7)))
const visibleRooms = computed(() => roomFilter.value ? rooms.value.filter((room) => room.id === roomFilter.value) : rooms.value)
const groupedBookings = computed(() => groupBookingsByRoomAndDay(bookings.value))
const selectedDayBookings = computed(() => bookingsForDay(selectedDay.value))
const upcomingCount = computed(() => bookings.value.filter((item) => item.status === 0).length)
const ongoingCount = computed(() => bookings.value.filter((item) => item.status === 1).length)
const periodLabel = computed(() => viewMode.value === 'week'
  ? `${weekDays.value[0].date} 至 ${weekDays.value[6].date}`
  : `${anchorDate.value.slice(0, 7)} 月度日历`)
const periodRange = computed(() => viewMode.value === 'week'
  ? { startTime: `${weekDays.value[0].date}T00:00:00`, endTime: `${weekDays.value[6].date}T23:59:59` }
  : monthRange(anchorDate.value.slice(0, 7)))
const occupancyRate = computed(() => calculateOccupancyRate(
  bookings.value,
  visibleRooms.value.length,
  periodRange.value.startTime,
  periodRange.value.endTime
))

onMounted(async () => {
  await loadRooms()
  await fetchCalendar()
})

function today() {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
}

function monthRange(month) {
  const [year, monthNumber] = String(month).split('-').map(Number)
  const lastDay = new Date(year, monthNumber, 0).getDate()
  return {
    startTime: `${year}-${String(monthNumber).padStart(2, '0')}-01T00:00:00`,
    endTime: `${year}-${String(monthNumber).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}T23:59:59`
  }
}

async function loadRooms() {
  const result = await listAvailableMeetingRooms()
  rooms.value = result.data || []
}

async function fetchCalendar() {
  loading.value = true
  try {
    const result = await listMeetingBookingCalendar({
      ...periodRange.value,
      roomId: roomFilter.value || undefined,
      status: statusFilter.value ?? undefined
    })
    bookings.value = result.data || []
  } finally {
    loading.value = false
  }
}

function shiftPeriod(step) {
  const current = new Date(`${anchorDate.value}T00:00:00`)
  if (viewMode.value === 'week') {
    current.setDate(current.getDate() + step * 7)
  } else {
    current.setMonth(current.getMonth() + step)
  }
  anchorDate.value = formatDate(current)
  fetchCalendar()
}

function goToday() {
  anchorDate.value = today()
  fetchCalendar()
}

function exportMeetings() {
  const events = bookings.value
    .filter((booking) => booking.startTime && booking.endTime)
    .map(buildMeetingEvent)
  if (!events.length) {
    ElMessage.info('当前筛选条件下没有可导出的会议')
    return
  }
  const suffix = viewMode.value === 'week' ? weekDays.value[0].date : anchorDate.value.slice(0, 7)
  downloadCalendarFile(`会议安排-${suffix}.ics`, buildCalendarFile('会议安排', events))
  ElMessage.success(`已导出 ${events.length} 场会议`)
}

function bookingsFor(roomId, date) {
  return groupedBookings.value[`${roomId}@${date}`] || []
}

function bookingsForDay(date) {
  if (!date) return []
  return bookings.value.filter((booking) => booking.startTime?.slice(0, 10) === date)
}

function openDay(date) {
  selectedDay.value = date
  dayDrawerVisible.value = true
}

function openCreate(room, date) {
  const targetDate = date || selectedDay.value || anchorDate.value
  Object.assign(form, {
    roomId: room?.id || roomFilter.value || rooms.value[0]?.id || null,
    meetingTitle: '',
    participantCount: 0,
    remark: ''
  })
  timeRange.value = [
    `${targetDate}T09:00:00`,
    `${targetDate}T10:00:00`
  ]
  createVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid || timeRange.value.length !== 2) {
    ElMessage.warning('请完整填写预定信息')
    return
  }
  submitting.value = true
  try {
    await createMeetingBooking({ ...form, startTime: timeRange.value[0], endTime: timeRange.value[1] })
    ElMessage.success('预定成功')
    createVisible.value = false
    await fetchCalendar()
  } finally {
    submitting.value = false
  }
}

async function handleCancel(booking) {
  await ElMessageBox.confirm(`确认取消会议【${booking.meetingTitle}】？`, '取消预定', { type: 'warning' })
  await cancelMeetingBooking(booking.id)
  ElMessage.success('预定已取消')
  await fetchCalendar()
}

function canCancel(booking) {
  return !booking.masked && booking.status !== 3 && Boolean(booking.bookerId)
}

function roomName(roomId) {
  return rooms.value.find((room) => room.id === roomId)?.roomName || '--'
}

function formatTime(value) {
  return value ? String(value).slice(11, 16) : '--:--'
}

function formatDateTimeRange(start, end) {
  return `${formatDateTime(start)} - ${formatTime(end)}`
}

function formatDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : '--'
}

function formatDate(date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function bookingStatusTag(status) {
  return { 0: 'info', 1: 'success', 2: 'info', 3: 'warning' }[status] || 'info'
}
</script>

<style scoped>
.calendar-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.calendar-header,
.calendar-workspace {
  padding: 20px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.calendar-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.calendar-header h1,
.workspace-heading h2 {
  margin: 0;
  color: #111827;
  font-weight: 650;
}

.calendar-header h1 { font-size: 21px; }
.workspace-heading h2 { font-size: 18px; }

.calendar-header p,
.workspace-heading p {
  margin: 7px 0 0;
  color: #6b7280;
  font-size: 13px;
  line-height: 1.6;
}

.calendar-toolbar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
}

.anchor-picker { width: 142px; }
.room-filter { width: 170px; }
.status-filter { width: 130px; }

.calendar-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.summary-card {
  min-height: 98px;
  padding: 16px 18px;
  border-left: 4px solid #2563eb;
  border-radius: 6px;
  background: #fff;
  border-top: 1px solid #e5e7eb;
  border-right: 1px solid #e5e7eb;
  border-bottom: 1px solid #e5e7eb;
}

.summary-card span,
.summary-card strong,
.summary-card small { display: block; }
.summary-card span { color: #6b7280; font-size: 12px; }
.summary-card strong { margin-top: 10px; color: #111827; font-size: 26px; line-height: 1; }
.summary-card small { margin-top: 8px; color: #9ca3af; font-size: 12px; }
.summary-card--rooms { border-left-color: #2563eb; }
.summary-card--upcoming { border-left-color: #d97706; }
.summary-card--active { border-left-color: #16a34a; }
.summary-card--rate { border-left-color: #0891b2; }

.workspace-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.workspace-count { color: #6b7280; font-size: 13px; }

.week-board {
  min-width: 920px;
  overflow-x: auto;
}

.week-board__header,
.week-board__row {
  display: grid;
  grid-template-columns: 190px repeat(7, minmax(120px, 1fr));
}

.room-heading,
.day-heading,
.room-label,
.week-cell {
  border-right: 1px solid #e5e7eb;
  border-bottom: 1px solid #e5e7eb;
}

.room-heading,
.day-heading {
  min-height: 56px;
  padding: 10px 12px;
  background: #f8fafc;
}

.room-heading { color: #6b7280; font-size: 12px; }
.day-heading strong,
.day-heading span { display: block; }
.day-heading strong { color: #111827; font-size: 13px; }
.day-heading span { margin-top: 4px; color: #9ca3af; font-size: 12px; }

.room-label {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 114px;
  padding: 12px;
  background: #fff;
}

.room-label strong { color: #111827; font-size: 14px; }
.room-label span { margin-top: 5px; color: #9ca3af; font-size: 11px; line-height: 1.5; }

.week-cell {
  display: flex;
  flex-direction: column;
  gap: 7px;
  min-height: 114px;
  padding: 8px;
  background: #fff;
  cursor: pointer;
}

.week-cell:hover,
.week-cell:focus-visible { background: #f8fbff; outline: none; }
.week-cell--busy { background: #fcfdff; }

.booking-chip,
.month-booking {
  display: block;
  width: 100%;
  padding: 8px;
  border: 0;
  border-left: 3px solid #2563eb;
  border-radius: 4px;
  color: #1f2937;
  background: #eff6ff;
  text-align: left;
  cursor: pointer;
}

.booking-chip:hover,
.month-booking:hover { filter: brightness(0.98); }
.booking-chip span,
.booking-chip strong,
.booking-chip small,
.month-booking span,
.month-booking strong { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.booking-chip span { color: #64748b; font-size: 10px; }
.booking-chip strong { margin-top: 4px; font-size: 12px; }
.booking-chip small { margin-top: 4px; color: #64748b; font-size: 10px; }
.booking-chip--success { border-left-color: #16a34a; background: #f0fdf4; }
.booking-chip--warning { border-left-color: #d97706; background: #fffbeb; }
.booking-chip--neutral { border-left-color: #64748b; background: #f8fafc; }
.free-slot { margin: auto; color: #cbd5e1; font-size: 11px; }

.month-view { overflow-x: auto; }
.month-weekdays,
.month-grid { display: grid; grid-template-columns: repeat(7, minmax(110px, 1fr)); gap: 6px; min-width: 770px; }
.month-weekdays { margin-bottom: 6px; color: #6b7280; font-size: 12px; text-align: center; }
.month-grid { grid-auto-rows: minmax(118px, auto); }

.month-cell {
  min-height: 118px;
  padding: 9px;
  border: 1px solid #e5e7eb;
  border-radius: 5px;
  background: #fff;
  cursor: pointer;
}

.month-cell:hover,
.month-cell:focus-visible { border-color: #93c5fd; outline: none; box-shadow: 0 2px 8px rgba(37, 99, 235, 0.1); }
.month-cell--muted { background: #f8fafc; }
.month-cell__top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.month-cell__top strong { color: #111827; font-size: 14px; }
.month-cell--muted .month-cell__top strong { color: #9ca3af; }
.month-add { width: 22px; height: 22px; padding: 0; border: 0; color: #94a3b8; background: transparent; border-radius: 4px; cursor: pointer; }
.month-add:hover { color: #2563eb; background: #eff6ff; }
.month-booking { margin-bottom: 5px; padding: 5px 7px; border-left-width: 2px; }
.month-booking span { color: #64748b; font-size: 10px; }
.month-booking strong { margin-top: 3px; font-size: 11px; }
.month-more,
.month-empty { display: block; color: #9ca3af; font-size: 11px; }
.month-more { margin-top: 5px; color: #2563eb; }
.month-empty { margin-top: 18px; text-align: center; }

.day-bookings__summary { margin: 0 0 14px; color: #6b7280; font-size: 13px; }
.booking-detail { padding: 15px 0; border-bottom: 1px solid #e5e7eb; }
.booking-detail__header { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; }
.booking-detail__header strong,
.booking-detail__header span { display: block; }
.booking-detail__header strong { color: #111827; font-size: 15px; }
.booking-detail__header span { margin-top: 4px; color: #64748b; font-size: 12px; }
.booking-detail__grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; margin: 16px 0 0; }
.booking-detail__grid div { min-width: 0; }
.booking-detail__grid dt { color: #9ca3af; font-size: 11px; }
.booking-detail__grid dd { margin: 4px 0 0; color: #374151; font-size: 13px; overflow-wrap: anywhere; }
.booking-detail__remark { margin: 14px 0 0; color: #6b7280; font-size: 12px; line-height: 1.6; }

@media (max-width: 1050px) {
  .calendar-header { flex-direction: column; }
  .calendar-toolbar { justify-content: flex-start; width: 100%; }
  .calendar-summary { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

@media (max-width: 620px) {
  .calendar-header,
  .calendar-workspace { padding: 15px; }
  .calendar-toolbar { justify-content: flex-start; }
  .anchor-picker,
  .room-filter,
  .status-filter { width: 100%; }
  .calendar-toolbar > .el-button:not(.is-circle) { flex: 1 1 100%; }
  .calendar-summary { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px; }
  .summary-card { min-height: 86px; padding: 12px; }
  .summary-card strong { font-size: 22px; }
}
</style>
