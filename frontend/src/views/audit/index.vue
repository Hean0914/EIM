<template>
  <section class="audit-page">
    <div class="page-heading">
      <div><h1 class="page-title">审计日志</h1><p class="page-desc">记录关键业务写操作，支持按模块、结果和时间范围追溯。</p></div>
      <el-button :loading="loading" @click="reload"><el-icon><Refresh /></el-icon>刷新</el-button>
    </div>
    <div class="audit-metrics">
      <article class="metric-card"><span>筛选结果</span><strong>{{ summary.total }}</strong><em>当前条件下的操作</em></article>
      <article class="metric-card"><span>成功操作</span><strong class="success-text">{{ summary.success }}</strong><em>业务执行成功</em></article>
      <article class="metric-card"><span>失败操作</span><strong class="danger-text">{{ summary.failure }}</strong><em>需要复盘关注</em></article>
    </div>
    <section class="audit-panel">
      <div class="filter-bar">
        <el-select v-model="filters.module" clearable placeholder="业务模块" size="small" @change="reload"><el-option v-for="item in modules" :key="item.value" :label="item.label" :value="item.value" /></el-select>
        <el-select v-model="filters.operationStatus" clearable placeholder="操作结果" size="small" @change="reload"><el-option label="成功" :value="1" /><el-option label="失败" :value="0" /></el-select>
        <el-date-picker v-model="dateRange" type="datetimerange" size="small" value-format="YYYY-MM-DDTHH:mm:ss" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" @change="reload" />
      </div>
      <div v-loading="loading" class="audit-timeline">
        <button v-for="log in logs" :key="log.id" class="audit-card" type="button" @click="openDetail(log)">
          <span class="audit-line" :class="auditStatusMeta(log.operationStatus).type" />
          <div class="audit-card__time"><strong>{{ formatTime(log.createTime) }}</strong><small>{{ log.requestMethod }} {{ log.requestPath }}</small></div>
          <div class="audit-card__content"><div class="audit-card__title"><el-tag size="small">{{ auditModuleLabel(log.module) }}</el-tag><strong>{{ log.action }}</strong><el-tag size="small" :type="auditStatusMeta(log.operationStatus).type">{{ log.statusLabel || auditStatusMeta(log.operationStatus).label }}</el-tag></div><p>{{ log.realName || log.username || '系统用户' }} · {{ log.detail || '未提供操作详情' }}</p></div>
          <el-icon><ArrowRight /></el-icon>
        </button>
        <el-empty v-if="!loading && !logs.length" description="暂无审计记录" />
      </div>
      <el-alert v-if="error" title="审计日志加载失败，请重试" type="error" show-icon :closable="false">
        <template #default><el-button link type="danger" @click="retry">重新加载</el-button></template>
      </el-alert>
      <el-pagination v-if="total" v-model:current-page="page.current" v-model:page-size="page.size" class="pagination-bar" background layout="total, prev, pager, next" :total="total" @current-change="loadLogs" />
    </section>
    <el-drawer v-model="drawerVisible" title="审计详情" size="480px">
      <template v-if="activeLog">
        <div class="detail-grid"><span>模块</span><strong>{{ auditModuleLabel(activeLog.module) }}</strong><span>动作</span><strong>{{ activeLog.action }}</strong><span>执行人</span><strong>{{ activeLog.realName || activeLog.username }}</strong><span>结果</span><el-tag :type="auditStatusMeta(activeLog.operationStatus).type">{{ activeLog.statusLabel }}</el-tag><span>请求</span><strong>{{ activeLog.requestMethod }} {{ activeLog.requestPath }}</strong><span>客户端 IP</span><strong>{{ activeLog.clientIp || '未知' }}</strong></div>
        <div class="detail-block"><span>操作详情</span><p>{{ formatAuditDetail(activeLog.detail) || '未提供详情' }}</p></div>
      </template>
    </el-drawer>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ArrowRight, Refresh } from '@element-plus/icons-vue'
import { getAuditLogSummary, listAuditLogPage } from '@/api/auditLog'
import { auditModuleLabel, auditStatusMeta, formatAuditDetail } from '@/utils/audit-log-view'

const loading = ref(false); const error = ref(false); const logs = ref([]); const total = ref(0); const drawerVisible = ref(false); const activeLog = ref(null); const dateRange = ref([])
const summary = reactive({ total: 0, success: 0, failure: 0 }); const filters = reactive({ module: '', operationStatus: null }); const page = reactive({ current: 1, size: 15 })
const modules = [{ value: 'NOTICE', label: '公告' }, { value: 'APPROVAL', label: '审批' }, { value: 'PLAN', label: '计划' }, { value: 'MEETING', label: '会议' }, { value: 'AUTH', label: '账号' }]
onMounted(async () => { await retry() })
function params() { return { current: page.current, size: page.size, module: filters.module || undefined, operationStatus: filters.operationStatus, startTime: dateRange.value?.[0], endTime: dateRange.value?.[1] } }
async function loadSummary() { Object.assign(summary, (await getAuditLogSummary(params())).data || {}) }
async function loadLogs() { loading.value = true; try { const result = await listAuditLogPage(params()); logs.value = result.data?.records || []; total.value = Number(result.data?.total || 0); error.value = false } catch { error.value = true } finally { loading.value = false } }
async function reload() { page.current = 1; await Promise.all([loadSummary(), loadLogs()]) }
async function retry() { error.value = false; await Promise.allSettled([loadSummary(), loadLogs()]) }
function openDetail(log) { activeLog.value = log; drawerVisible.value = true }
function formatTime(value) { return String(value || '').replace('T', ' ').slice(0, 16) || '时间未知' }
</script>

<style scoped>
.audit-page { display: flex; flex-direction: column; gap: 20px; } .page-heading, .filter-bar, .audit-card__title { display: flex; align-items: center; gap: 12px; } .page-heading { justify-content: space-between; }
.audit-metrics { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 14px; } .metric-card, .audit-panel { padding: 20px; background: #fff; border: var(--panel-border); border-radius: 8px; box-shadow: var(--panel-shadow); } .metric-card span, .metric-card em { color: var(--gray-600); font-size: 12px; font-style: normal; } .metric-card strong { display: block; margin: 8px 0 2px; color: var(--gray-900); font-size: 26px; } .success-text { color: var(--color-success) !important; } .danger-text { color: var(--color-error) !important; }
.filter-bar { flex-wrap: wrap; padding-bottom: 18px; border-bottom: 1px solid var(--gray-200); } .audit-timeline { display: flex; flex-direction: column; gap: 10px; min-height: 240px; padding-top: 18px; }
.audit-card { display: grid; grid-template-columns: 5px 150px minmax(0, 1fr) 18px; gap: 14px; align-items: center; width: 100%; padding: 0; text-align: left; cursor: pointer; background: #fff; border: 1px solid var(--gray-200); border-radius: 8px; transition: border-color .16s ease, box-shadow .16s ease, transform .16s ease; } .audit-card:hover { border-color: #a5b4fc; box-shadow: 0 8px 20px rgba(79,70,229,.08); transform: translateX(2px); } .audit-line { align-self: stretch; background: var(--color-info); } .audit-line.success { background: var(--color-success); } .audit-line.danger { background: var(--color-error); } .audit-card__time { padding: 14px 0; } .audit-card__time strong, .audit-card__time small { display: block; } .audit-card__time strong { color: var(--gray-900); font-size: 13px; } .audit-card__time small { margin-top: 4px; color: var(--gray-500); font-size: 11px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; } .audit-card__content { min-width: 0; padding: 14px 0; } .audit-card__title { flex-wrap: wrap; } .audit-card__title strong { color: var(--gray-900); } .audit-card__content p { margin: 6px 0 0; overflow: hidden; color: var(--gray-600); font-size: 13px; text-overflow: ellipsis; white-space: nowrap; } .audit-card > .el-icon { color: var(--gray-400); }
.detail-grid { display: grid; grid-template-columns: 90px minmax(0, 1fr); gap: 14px 12px; padding-bottom: 20px; border-bottom: 1px solid var(--gray-200); } .detail-grid span, .detail-block > span { color: var(--gray-500); font-size: 12px; } .detail-grid strong { color: var(--gray-800); font-size: 13px; word-break: break-all; } .detail-block { padding-top: 20px; } .detail-block p { margin: 10px 0 0; color: var(--gray-700); white-space: pre-wrap; line-height: 1.8; }
@media (max-width: 720px) { .page-heading { align-items: flex-start; flex-direction: column; } .audit-metrics { grid-template-columns: 1fr; } .audit-card { grid-template-columns: 5px minmax(0, 1fr) 18px; } .audit-card__time { display: none; } }
</style>
