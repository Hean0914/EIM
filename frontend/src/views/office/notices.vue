<template>
  <section class="office-page">
    <header class="notice-header">
      <div>
        <div class="eyebrow">信息发布中心</div>
        <h1>公告信息流</h1>
        <p>把重要通知、制度和活动集中在一个易于浏览的动态信息流中。</p>
      </div>
      <div class="header-actions">
        <el-radio-group v-if="canManageOffice" v-model="viewMode" size="small">
          <el-radio-button label="feed">信息流</el-radio-button>
          <el-radio-button label="manage">管理列表</el-radio-button>
        </el-radio-group>
        <el-button v-if="canManageOffice" type="primary" :icon="Plus" @click="openCreate">新增公告</el-button>
      </div>
    </header>

    <section class="notice-summary" aria-label="公告摘要">
      <article class="summary-card summary-card--total">
        <span>当前页公告</span>
        <strong>{{ feedSummary.total }}</strong>
        <small>共 {{ total }} 条结果</small>
      </article>
      <article class="summary-card summary-card--unread">
        <span>未读公告</span>
        <strong>{{ feedSummary.unread }}</strong>
        <small>打开详情后自动标记已读</small>
      </article>
      <article class="summary-card summary-card--pinned">
        <span>置顶公告</span>
        <strong>{{ feedSummary.pinned }}</strong>
        <small>置顶内容优先展示</small>
      </article>
    </section>

    <section v-loading="loading" class="notice-workspace">
      <div class="workspace-toolbar">
        <div>
          <h2>{{ viewMode === 'feed' ? '最新公告' : '公告管理' }}</h2>
          <p>支持按类型筛选，点击卡片即可查看完整内容。</p>
        </div>
        <el-form :model="query" inline class="search-form" @submit.prevent="handleSearch">
          <el-form-item label="关键词">
            <el-input v-model.trim="query.title" clearable placeholder="搜索公告标题" @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="query.noticeType" clearable placeholder="全部类型" class="filter-select">
              <el-option v-for="item in noticeTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="canManageOffice" label="状态">
            <el-select v-model="query.publishStatus" clearable placeholder="全部状态" class="filter-select">
              <el-option v-for="item in noticeStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item class="toolbar-buttons">
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-alert v-if="errorMessage" title="公告加载失败" :description="errorMessage" type="error" show-icon closable @close="errorMessage = ''">
        <template #default>
          <el-button link type="primary" @click="fetchData">重新加载</el-button>
        </template>
      </el-alert>

      <template v-if="viewMode === 'feed'">
        <div v-if="!loading && !noticeFeed.length" class="empty-state">
          <el-empty description="暂无符合条件的公告" />
        </div>
        <div v-else class="notice-feed">
          <article
            v-for="notice in noticeFeed"
            :key="notice.id"
            class="notice-card"
            :class="{ 'notice-card--unread': !notice.isRead, 'notice-card--pinned': notice.isPinned }"
            tabindex="0"
            role="button"
            @click="openDetail(notice)"
            @keydown.enter="openDetail(notice)"
          >
            <div class="notice-card__topline">
              <div class="notice-card__labels">
                <el-tag :type="notice.typeTag" effect="plain" size="small">{{ notice.typeLabel }}</el-tag>
                <el-tag v-if="notice.isPinned" type="danger" effect="plain" size="small">置顶</el-tag>
                <span v-if="!notice.isRead" class="unread-label"><i />未读</span>
              </div>
              <time>{{ formatDateTime(notice.publishTime || notice.createTime) }}</time>
            </div>
            <div class="notice-card__title-row">
              <h3>{{ notice.title }}</h3>
              <el-icon class="notice-card__arrow"><ArrowRight /></el-icon>
            </div>
            <p class="notice-card__summary">{{ notice.summary }}</p>
            <div class="notice-card__footer">
              <span>{{ notice.statusLabel }}</span>
              <span>{{ notice.isRead ? '已读' : '点击查看详情' }}</span>
            </div>
            <div v-if="canManageOffice" class="notice-card__actions">
              <el-button link type="primary" @click.stop="openEdit(notice)">编辑</el-button>
              <el-button link type="success" @click.stop="handlePublish(notice)">发布</el-button>
              <el-button link type="warning" @click.stop="handleOffline(notice)">下线</el-button>
              <el-button link type="danger" @click.stop="handleDelete(notice)">删除</el-button>
            </div>
          </article>
        </div>
      </template>

      <template v-else>
        <el-table v-if="!loading && noticeFeed.length" :data="tableData" border row-key="id" @row-click="openDetail">
          <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
          <el-table-column label="类型" width="100">
            <template #default="{ row }">{{ noticeTypeMeta(row.noticeType).label }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="noticeStatusMeta(row.publishStatus).tag">{{ noticeStatusMeta(row.publishStatus).label }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="publishTime" label="发布时间" min-width="170" />
          <el-table-column label="置顶" width="80">
            <template #default="{ row }">{{ Number(row.topFlag) === 1 ? '是' : '否' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click.stop="openEdit(row)">编辑</el-button>
              <el-button link type="success" @click.stop="handlePublish(row)">发布</el-button>
              <el-button link type="warning" @click.stop="handleOffline(row)">下线</el-button>
              <el-button link type="danger" @click.stop="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-else-if="!loading" class="empty-state">
          <el-empty description="暂无符合条件的公告" />
        </div>
      </template>

      <div v-if="total > 0" class="pagination-bar">
        <el-pagination
          v-model:current-page="query.current"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[8, 12, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="fetchData"
        />
      </div>
    </section>

    <el-drawer v-model="detailVisible" title="公告详情" size="500px" destroy-on-close>
      <div v-if="detailLoading" v-loading="detailLoading" class="detail-loading" />
      <template v-else-if="selectedNotice">
        <el-alert v-if="detailError" :title="detailError" type="warning" show-icon :closable="false" />
        <article class="notice-detail">
          <div class="notice-detail__labels">
            <el-tag :type="noticeTypeMeta(selectedNotice.noticeType).tag" effect="plain">{{ noticeTypeMeta(selectedNotice.noticeType).label }}</el-tag>
            <el-tag v-if="Number(selectedNotice.topFlag) === 1" type="danger" effect="plain">置顶公告</el-tag>
            <el-tag :type="noticeStatusMeta(selectedNotice.publishStatus).tag" effect="plain">{{ noticeStatusMeta(selectedNotice.publishStatus).label }}</el-tag>
          </div>
          <h2>{{ selectedNotice.title }}</h2>
          <div class="notice-detail__meta">
            <span>发布时间：{{ formatDateTime(selectedNotice.publishTime || selectedNotice.createTime) }}</span>
            <span>{{ selectedNotice.readFlag === true || isNoticeRead(selectedNotice.id) ? '已读' : '未读' }}</span>
          </div>
          <p class="notice-detail__content">{{ selectedNotice.content || '暂无正文' }}</p>
        </article>
      </template>
      <el-empty v-else description="公告详情暂不可用" />
      <template #footer>
        <div class="drawer-footer">
          <el-button v-if="canManageOffice && selectedNotice" type="primary" @click="openEdit(selectedNotice)">编辑公告</el-button>
          <el-button @click="detailVisible = false">关闭</el-button>
        </div>
      </template>
    </el-drawer>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑公告' : '新增公告'" width="720px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="标题" prop="title">
          <el-input v-model.trim="form.title" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.noticeType">
            <el-option v-for="item in noticeTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="发布状态">
          <el-radio-group v-model="form.publishStatus">
            <el-radio-button :label="0">草稿</el-radio-button>
            <el-radio-button :label="1">发布</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否置顶">
          <el-switch v-model="form.topFlag" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model.trim="form.content" type="textarea" :rows="8" show-word-limit maxlength="5000" />
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
import { ArrowRight, Plus, Refresh, Search } from '@element-plus/icons-vue'
import {
  createNotice,
  deleteNotice,
  getNotice,
  listNoticePage,
  markNoticeRead,
  offlineNotice,
  publishNotice,
  updateNotice
} from '@/api/notice'
import { useUserStore } from '@/store/user'
import { buildNoticeFeed, getNoticeStatusMeta, getNoticeTypeMeta, summarizeNoticeFeed } from '@/utils/notice-view'

const noticeTypeOptions = [
  { value: 1, label: '通知' },
  { value: 2, label: '制度' },
  { value: 3, label: '活动' },
  { value: 4, label: '其他' }
]
const noticeStatusOptions = [
  { value: 0, label: '草稿' },
  { value: 1, label: '已发布' },
  { value: 2, label: '已下线' }
]
const emptyForm = { id: null, title: '', content: '', noticeType: 1, publishStatus: 0, topFlag: 0 }
const userStore = useUserStore()
const canManageOffice = computed(() => userStore.canManageOffice)
const viewMode = ref('feed')
const query = reactive({ current: 1, size: 12, title: '', noticeType: null, publishStatus: null })
const form = reactive({ ...emptyForm })
const formRef = ref()
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailError = ref('')
const errorMessage = ref('')
const selectedNotice = ref(null)
const readNoticeIds = ref(loadReadNoticeIds())
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const noticeFeed = computed(() => buildNoticeFeed(tableData.value, readNoticeIds.value))
const feedSummary = computed(() => summarizeNoticeFeed(noticeFeed.value))

onMounted(fetchData)

async function fetchData() {
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await listNoticePage(cleanQuery())
    const data = result.data || {}
    tableData.value = data.records || []
    total.value = Number(data.total || 0)
  } catch (error) {
    errorMessage.value = error?.message || '公告列表加载失败'
  } finally {
    loading.value = false
  }
}

function cleanQuery() {
  const params = { ...query }
  if (!canManageOffice.value) {
    delete params.publishStatus
  }
  return Object.fromEntries(Object.entries(params).filter(([, value]) => value !== null && value !== ''))
}

async function openDetail(notice) {
  selectedNotice.value = notice
  detailVisible.value = true
  detailLoading.value = true
  detailError.value = ''
  try {
    const result = await getNotice(notice.id)
    selectedNotice.value = { ...notice, ...(result.data || {}) }
    await markAsRead(selectedNotice.value)
  } catch (error) {
    detailError.value = error?.message || '公告详情加载失败，当前展示列表摘要'
  } finally {
    detailLoading.value = false
  }
}

async function markAsRead(notice) {
  if (!notice || Number(notice.publishStatus) !== 1 || isNoticeRead(notice.id)) {
    return
  }
  try {
    await markNoticeRead(notice.id)
    const record = tableData.value.find((item) => String(item.id) === String(notice.id))
    if (record) {
      record.readFlag = true
    }
    if (selectedNotice.value && String(selectedNotice.value.id) === String(notice.id)) {
      selectedNotice.value.readFlag = true
    }
    rememberRead(notice.id)
  } catch (error) {
    detailError.value = error?.message || '已读状态同步失败'
  }
}

function openCreate() {
  Object.assign(form, emptyForm)
  dialogVisible.value = true
}

function openEdit(row) {
  detailVisible.value = false
  Object.assign(form, emptyForm, row)
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (form.id) {
      await updateNotice({ ...form })
      ElMessage.success('公告已更新')
    } else {
      await createNotice({ ...form })
      ElMessage.success('公告已新增')
    }
    dialogVisible.value = false
    await fetchData()
  } finally {
    submitting.value = false
  }
}

async function handlePublish(row) {
  await publishNotice(row.id)
  ElMessage.success('公告已发布')
  await fetchData()
}

async function handleOffline(row) {
  await offlineNotice(row.id)
  ElMessage.success('公告已下线')
  await fetchData()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除公告【${row.title}】？`, '删除确认', { type: 'warning' })
  } catch {
    return
  }
  await deleteNotice(row.id)
  ElMessage.success('删除成功')
  await fetchData()
}

function handleSearch() {
  query.current = 1
  fetchData()
}

function handleReset() {
  Object.assign(query, { current: 1, size: 12, title: '', noticeType: null, publishStatus: null })
  fetchData()
}

function handleSizeChange() {
  query.current = 1
  fetchData()
}

function noticeTypeMeta(type) {
  return getNoticeTypeMeta(type)
}

function noticeStatusMeta(status) {
  return getNoticeStatusMeta(status)
}

function formatDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : '未发布'
}

function isNoticeRead(id) {
  return readNoticeIds.value.includes(String(id))
}

function rememberRead(id) {
  if (isNoticeRead(id)) return
  readNoticeIds.value = [...readNoticeIds.value, String(id)]
  localStorage.setItem(readNoticeStorageKey(), JSON.stringify(readNoticeIds.value))
}

function readNoticeStorageKey() {
  const userId = userStore.user?.userId || userStore.user?.id || 'current'
  return `enterprise_notice_read_${userId}`
}

function loadReadNoticeIds() {
  try {
    const value = JSON.parse(localStorage.getItem(readNoticeStorageKey()) || '[]')
    return Array.isArray(value) ? value.map(String) : []
  } catch {
    return []
  }
}
</script>

<style scoped>
.office-page { display: flex; flex-direction: column; gap: 16px; }
.notice-header, .notice-workspace { padding: 20px; background: #fff; border: 1px solid #e5e7eb; border-radius: 8px; }
.notice-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 20px; }
.eyebrow { color: #2563eb; font-size: 11px; font-weight: 700; letter-spacing: 0.08em; text-transform: uppercase; }
.notice-header h1, .workspace-toolbar h2 { margin: 5px 0 0; color: #111827; font-weight: 650; }
.notice-header h1 { font-size: 22px; }
.workspace-toolbar h2 { font-size: 18px; }
.notice-header p, .workspace-toolbar p { margin: 7px 0 0; color: #6b7280; font-size: 13px; line-height: 1.6; }
.header-actions, .workspace-toolbar, .notice-card__topline, .notice-card__title-row, .notice-card__footer, .drawer-footer { display: flex; align-items: center; }
.header-actions { justify-content: flex-end; flex-wrap: wrap; gap: 10px; }
.notice-summary { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; }
.summary-card { min-height: 98px; padding: 16px 18px; background: #fff; border: 1px solid #e5e7eb; border-left: 4px solid #2563eb; border-radius: 6px; }
.summary-card span, .summary-card strong, .summary-card small { display: block; }
.summary-card span { color: #6b7280; font-size: 12px; }
.summary-card strong { margin-top: 10px; color: #111827; font-size: 26px; line-height: 1; }
.summary-card small { margin-top: 8px; color: #9ca3af; font-size: 12px; }
.summary-card--unread { border-left-color: #d97706; }
.summary-card--pinned { border-left-color: #db2777; }
.workspace-toolbar { align-items: flex-start; justify-content: space-between; gap: 20px; margin-bottom: 18px; }
.search-form { display: flex; flex-wrap: wrap; justify-content: flex-end; margin: 0; }
.filter-select { width: 130px; }
.toolbar-buttons { margin-right: 0; }
.notice-feed { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 14px; }
.notice-card { position: relative; display: flex; min-height: 228px; flex-direction: column; padding: 16px; cursor: pointer; background: #fff; border: 1px solid #e2e8f0; border-radius: 6px; transition: border-color 0.16s ease, box-shadow 0.16s ease, transform 0.16s ease; }
.notice-card:hover, .notice-card:focus-visible { border-color: #93c5fd; outline: none; box-shadow: 0 6px 18px rgba(37, 99, 235, 0.1); transform: translateY(-1px); }
.notice-card--unread { border-top: 3px solid #2563eb; }
.notice-card--pinned { background: #fffdf7; }
.notice-card__topline, .notice-card__footer { justify-content: space-between; gap: 10px; }
.notice-card__labels { display: flex; align-items: center; flex-wrap: wrap; gap: 6px; }
.notice-card__topline time { color: #94a3b8; font-size: 11px; white-space: nowrap; }
.unread-label { display: inline-flex; align-items: center; gap: 4px; color: #2563eb; font-size: 11px; font-weight: 600; }
.unread-label i { width: 6px; height: 6px; background: #2563eb; border-radius: 50%; }
.notice-card__title-row { align-items: flex-start; gap: 10px; margin-top: 17px; }
.notice-card__title-row h3 { min-width: 0; margin: 0; overflow: hidden; color: #0f172a; font-size: 16px; line-height: 1.45; text-overflow: ellipsis; white-space: nowrap; }
.notice-card__arrow { flex: 0 0 auto; margin-top: 3px; color: #94a3b8; }
.notice-card__summary { display: -webkit-box; min-height: 63px; margin: 14px 0; overflow: hidden; color: #475569; font-size: 13px; line-height: 1.62; -webkit-box-orient: vertical; -webkit-line-clamp: 3; }
.notice-card__footer { margin-top: auto; padding-top: 12px; color: #94a3b8; border-top: 1px solid #edf2f7; font-size: 11px; }
.notice-card__actions { display: flex; flex-wrap: wrap; gap: 2px; margin-top: 9px; }
.empty-state { min-height: 260px; padding: 36px 0; }
.pagination-bar { display: flex; justify-content: flex-end; margin-top: 18px; }
.detail-loading { min-height: 260px; }
.notice-detail { padding-top: 18px; }
.notice-detail__labels { display: flex; flex-wrap: wrap; gap: 8px; }
.notice-detail h2 { margin: 18px 0 10px; color: #111827; font-size: 22px; line-height: 1.4; }
.notice-detail__meta { display: flex; flex-wrap: wrap; gap: 8px 18px; color: #94a3b8; font-size: 12px; }
.notice-detail__content { margin: 24px 0 0; color: #334155; font-size: 14px; line-height: 1.85; white-space: pre-wrap; }
.drawer-footer { justify-content: flex-end; gap: 8px; }
.el-select, .el-input { max-width: 220px; }
@media (max-width: 1180px) { .notice-feed { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 900px) { .notice-header, .workspace-toolbar { align-items: flex-start; flex-direction: column; } .header-actions, .search-form { justify-content: flex-start; } }
@media (max-width: 640px) { .notice-summary, .notice-feed { grid-template-columns: 1fr; } .notice-header, .notice-workspace { padding: 16px; } .search-form { width: 100%; } .search-form .el-form-item { margin-right: 0; } .filter-select, .el-input { width: 100%; max-width: none; } .notice-card__topline { align-items: flex-start; flex-direction: column; } .notice-card__topline time { white-space: normal; } }
</style>
