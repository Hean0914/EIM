<template>
  <section class="message-page">
    <div class="page-heading">
      <div>
        <h1 class="page-title">统一消息</h1>
        <p class="page-desc">把公告、审批、计划和办公提醒集中到一个可处理的信息流。</p>
      </div>
      <div class="message-heading-actions">
        <el-button
          v-if="notificationPermission === 'default'"
          type="primary"
          plain
          @click="enableBrowserNotifications"
        >
          <el-icon><Bell /></el-icon>开启桌面提醒
        </el-button>
        <el-tag v-else-if="notificationPermission === 'granted'" type="success" effect="plain">
          桌面提醒已开启
        </el-tag>
        <el-tag v-else-if="notificationPermission === 'denied'" type="info" effect="plain">
          桌面提醒未开启
        </el-tag>
        <el-tag v-else type="info" effect="plain">
          当前浏览器不支持
        </el-tag>
        <el-button type="primary" plain :disabled="!summary.unread" @click="handleMarkAllRead">
          <el-icon><Check /></el-icon>全部标为已读
        </el-button>
      </div>
    </div>

    <div class="message-metrics">
      <article v-for="item in metrics" :key="item.label" class="metric-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <em>{{ item.note }}</em>
      </article>
    </div>

    <section class="message-panel">
      <div class="message-toolbar">
        <el-radio-group v-model="filters.type" size="small" @change="reload">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button v-for="item in typeOptions" :key="item.value" :label="item.value">{{ item.label }}</el-radio-button>
        </el-radio-group>
        <el-select v-model="filters.readStatus" clearable placeholder="阅读状态" size="small" @change="reload">
          <el-option label="未读" :value="0" />
          <el-option label="已读" :value="1" />
        </el-select>
        <el-select v-model="filters.priority" clearable placeholder="优先级" size="small" @change="reload">
          <el-option label="高优先级" :value="3" />
          <el-option label="重要提醒" :value="2" />
          <el-option label="普通提醒" :value="1" />
        </el-select>
      </div>

      <div v-loading="loading" class="message-stream">
        <button v-for="message in messages" :key="message.id" class="message-card" :class="{ unread: Number(message.readStatus) === 0 }" type="button" @click="openMessage(message)">
          <div class="message-card__rail" :class="messageTypeMeta(message.messageType).type" />
          <div class="message-card__body">
            <div class="message-card__topline">
              <div class="message-card__tags">
                <el-tag size="small" effect="plain">{{ messageTypeMeta(message.messageType).label }}</el-tag>
                <el-tag size="small" :type="priorityMeta(message.priority).type" effect="light">{{ priorityMeta(message.priority).label }}</el-tag>
                <i v-if="Number(message.readStatus) === 0" class="unread-dot" />
              </div>
              <time>{{ formatMessageTime(message.createTime) }}</time>
            </div>
            <h2>{{ message.title }}</h2>
            <p>{{ message.content || '暂无详细内容' }}</p>
            <div class="message-card__footer"><span>{{ Number(message.readStatus) === 0 ? '点击查看并标记已读' : '已读消息' }}</span><el-icon><ArrowRight /></el-icon></div>
          </div>
        </button>
        <el-empty v-if="!loading && !messages.length" description="暂时没有符合条件的消息" />
      </div>
      <el-alert v-if="error" title="消息加载失败，请重试" type="error" show-icon :closable="false">
        <template #default><el-button link type="danger" @click="retry">重新加载</el-button></template>
      </el-alert>
      <el-pagination v-if="total" v-model:current-page="page.current" v-model:page-size="page.size" class="pagination-bar" background layout="total, prev, pager, next" :total="total" @current-change="loadMessages" />
    </section>

    <el-drawer v-model="drawerVisible" title="消息详情" size="460px">
      <template v-if="activeMessage">
        <div class="message-detail-head">
          <el-tag size="small">{{ messageTypeMeta(activeMessage.messageType).label }}</el-tag>
          <el-tag size="small" :type="priorityMeta(activeMessage.priority).type">{{ priorityMeta(activeMessage.priority).label }}</el-tag>
          <time>{{ formatMessageTime(activeMessage.createTime) }}</time>
        </div>
        <h2 class="drawer-title">{{ activeMessage.title }}</h2>
        <p class="drawer-content">{{ activeMessage.content }}</p>
        <el-button v-if="activeMessage.targetPath" type="primary" @click="goTarget">去处理</el-button>
      </template>
    </el-drawer>
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowRight, Bell, Check } from '@element-plus/icons-vue'
import { getMessageSummary, listMessagePage, markAllMessagesRead, markMessageRead } from '@/api/message'
import {
  dispatchMessageUpdated,
  getBrowserNotificationPermission,
  MESSAGE_UPDATED_EVENT,
  requestBrowserNotificationPermission
} from '@/utils/message-notifier'
import { formatMessageTime, messageTypeMeta, priorityMeta } from '@/utils/message-view'

const router = useRouter()
const loading = ref(false)
const error = ref(false)
const messages = ref([])
const total = ref(0)
const drawerVisible = ref(false)
const activeMessage = ref(null)
const notificationPermission = ref(getBrowserNotificationPermission())
const summary = reactive({ total: 0, unread: 0, highPriority: 0, noticeCount: 0, approvalCount: 0, planCount: 0, meetingCount: 0, attendanceCount: 0 })
const filters = reactive({ type: '', readStatus: null, priority: null })
const page = reactive({ current: 1, size: 12 })
const typeOptions = [
  { value: 'NOTICE', label: '公告' },
  { value: 'APPROVAL', label: '审批' },
  { value: 'ATTENDANCE', label: '考勤' },
  { value: 'PLAN', label: '计划' },
  { value: 'MEETING', label: '会议' }
]
const metrics = computed(() => [
  { label: '消息总数', value: summary.total, note: '当前账号可见' },
  { label: '未读消息', value: summary.unread, note: '需要关注' },
  { label: '高优先级', value: summary.highPriority, note: '优先处理' },
  { label: '业务分类', value: new Set(messages.value.map((item) => item.messageType)).size, note: '当前页覆盖' }
])

onMounted(async () => {
  window.addEventListener(MESSAGE_UPDATED_EVENT, handleMessageUpdated)
  await retry()
})

onBeforeUnmount(() => {
  window.removeEventListener(MESSAGE_UPDATED_EVENT, handleMessageUpdated)
})

async function handleMessageUpdated(event) {
  if (event?.detail?.source !== 'poll') {
    return
  }
  await retry()
}

async function enableBrowserNotifications() {
  notificationPermission.value = await requestBrowserNotificationPermission()
  if (notificationPermission.value === 'granted') {
    ElMessage.success('桌面提醒已开启')
  } else if (notificationPermission.value === 'denied') {
    ElMessage.info('浏览器未授予通知权限，请在站点设置中重新开启')
  } else if (notificationPermission.value === 'unsupported') {
    ElMessage.info('当前浏览器不支持桌面通知，将继续使用站内未读提醒')
  }
}

async function loadSummary() {
  const result = await getMessageSummary()
  Object.assign(summary, result.data || {})
}

async function loadMessages() {
  loading.value = true
  try {
    const result = await listMessagePage({ current: page.current, size: page.size, messageType: filters.type || undefined, readStatus: filters.readStatus, priority: filters.priority })
    messages.value = result.data?.records || []
    total.value = Number(result.data?.total || 0)
    error.value = false
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

async function retry() {
  error.value = false
  await Promise.allSettled([loadSummary(), loadMessages()])
}

async function reload() { page.current = 1; await loadMessages() }

async function openMessage(message) {
  activeMessage.value = message
  drawerVisible.value = true
  if (Number(message.readStatus) === 0) {
    await markMessageRead(message.id)
    message.readStatus = 1
    summary.unread = Math.max(0, summary.unread - 1)
    await loadSummary()
    dispatchMessageUpdated({ source: 'read', messageId: message.id })
  }
}

async function handleMarkAllRead() {
  await markAllMessagesRead()
  messages.value.forEach((message) => { message.readStatus = 1 })
  await loadSummary()
  dispatchMessageUpdated({ source: 'read-all' })
  ElMessage.success('已全部标记为已读')
}

function goTarget() {
  drawerVisible.value = false
  if (activeMessage.value?.targetPath) router.push(activeMessage.value.targetPath)
}
</script>

<style scoped>
.message-page { display: flex; flex-direction: column; gap: 20px; }
.page-heading, .message-toolbar, .message-card__topline, .message-card__footer, .message-detail-head, .message-heading-actions { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.message-heading-actions { justify-content: flex-end; flex-wrap: wrap; }
.message-metrics { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 14px; }
.message-panel { padding: 20px; background: #fff; border: var(--panel-border); border-radius: 8px; box-shadow: var(--panel-shadow); }
.message-toolbar { justify-content: flex-start; flex-wrap: wrap; padding-bottom: 18px; border-bottom: 1px solid var(--gray-200); }
.message-stream { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px; padding-top: 18px; min-height: 220px; }
.message-card { position: relative; display: flex; width: 100%; padding: 0; overflow: hidden; text-align: left; cursor: pointer; background: #fff; border: 1px solid var(--gray-200); border-radius: 8px; transition: transform .16s ease, border-color .16s ease, box-shadow .16s ease; }
.message-card:hover { border-color: #a5b4fc; box-shadow: 0 8px 22px rgba(79,70,229,.10); transform: translateY(-2px); }
.message-card.unread { background: #fcfcff; }
.message-card__rail { width: 5px; flex: 0 0 5px; background: var(--color-brand); }
.message-card__rail.approval { background: var(--color-warning); } .message-card__rail.attendance { background: var(--color-success); } .message-card__rail.plan { background: var(--color-info); } .message-card__rail.meeting { background: #0891b2; }
.message-card__body { flex: 1; min-width: 0; padding: 16px; }
.message-card__topline { align-items: flex-start; } .message-card__tags { display: inline-flex; align-items: center; gap: 6px; flex-wrap: wrap; } .message-card time, .message-detail-head time { color: var(--gray-500); font-size: 12px; white-space: nowrap; }
.message-card h2 { margin: 14px 0 6px; color: var(--gray-900); font-size: 16px; } .message-card p { display: -webkit-box; min-height: 44px; margin: 0; overflow: hidden; color: var(--gray-600); font-size: 13px; line-height: 1.7; -webkit-box-orient: vertical; -webkit-line-clamp: 2; }
.message-card__footer { justify-content: flex-start; margin-top: 14px; color: var(--color-brand); font-size: 12px; } .unread-dot { width: 7px; height: 7px; background: var(--color-error); border-radius: 50%; }
.drawer-title { margin: 20px 0 12px; color: var(--gray-900); font-size: 20px; } .drawer-content { min-height: 180px; color: var(--gray-700); white-space: pre-wrap; line-height: 1.8; }
@media (max-width: 900px) { .message-stream, .message-metrics { grid-template-columns: 1fr 1fr; } }
@media (max-width: 640px) { .page-heading { align-items: flex-start; flex-direction: column; } .message-heading-actions { justify-content: flex-start; width: 100%; } .message-stream, .message-metrics { grid-template-columns: 1fr; } }
</style>
