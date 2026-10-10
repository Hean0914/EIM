<template>
  <section class="dashboard-page">
    <div class="dashboard-header">
      <div>
        <h1 class="page-title">工作台</h1>
        <p class="page-desc">{{ isManagementUser ? '人事与组织管理数据实时看板' : `${roleName}可在这里快速进入常用业务模块。` }}</p>
      </div>
      <div class="dashboard-actions">
        <el-radio-group v-if="isGeneralManager" v-model="trendDays" size="small" @change="fetchDashboard">
          <el-radio-button :value="7">7天</el-radio-button>
          <el-radio-button :value="14">14天</el-radio-button>
          <el-radio-button :value="30">30天</el-radio-button>
        </el-radio-group>
        <div class="role-chip">{{ roleName }}</div>
      </div>
    </div>

    <template v-if="isManagementUser">
      <div v-loading="loading" class="admin-dashboard">
        <div class="metric-grid admin-metrics">
          <article v-for="item in adminMetrics" :key="item.label" class="metric-card">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}<small>{{ item.unit }}</small></strong>
            <em>{{ item.description }}</em>
          </article>
        </div>

        <div class="chart-grid">
          <section class="dashboard-panel trend-panel">
            <div class="panel-header">
              <h2>近 7 天考勤趋势</h2>
              <span>实时聚合打卡记录</span>
            </div>
            <div class="line-chart" aria-label="近7天考勤趋势折线图">
              <svg viewBox="0 0 640 220" role="img">
                <g class="chart-grid-lines">
                  <line x1="32" y1="40" x2="608" y2="40" />
                  <line x1="32" y1="80" x2="608" y2="80" />
                  <line x1="32" y1="120" x2="608" y2="120" />
                  <line x1="32" y1="160" x2="608" y2="160" />
                </g>
                <polygon v-if="areaPoints" :points="areaPoints" class="chart-area" />
                <polyline v-if="linePoints" :points="linePoints" class="chart-line" />
                <g>
                  <circle v-for="point in linePointObjects" :key="point.date" :cx="point.x" :cy="point.y" r="4" class="chart-dot" />
                </g>
                <g class="chart-x-labels">
                  <text v-for="point in linePointObjects" :key="`${point.date}-label`" :x="point.x" y="204" text-anchor="middle">
                    {{ point.label }}
                  </text>
                </g>
              </svg>
              <div v-if="!trendPoints.length" class="empty-chart">暂无考勤趋势数据</div>
            </div>
          </section>

          <section class="dashboard-panel pie-panel">
            <div class="panel-header">
              <h2>企业人员部门分布</h2>
              <span>按部门聚合员工档案</span>
            </div>
            <div class="pie-content">
              <div class="pie-chart" :style="{ background: pieGradient }">
                <span>{{ pieTotal }}</span>
                <em>总人数</em>
              </div>
              <ul class="pie-legend">
                <li v-for="item in pieLegend" :key="item.name">
                  <i :style="{ background: item.color }" />
                  <span>{{ item.name }}</span>
                  <strong>{{ item.value }}人</strong>
                </li>
              </ul>
            </div>
          </section>
        </div>

        <section class="dashboard-panel">
          <div class="panel-header">
            <h2>核心业务入口</h2>
            <span>管理员常用管理模块</span>
          </div>
          <div class="shortcut-list admin-shortcuts">
            <router-link v-for="item in shortcuts" :key="item.path" :to="item.path" class="shortcut-item">
              <span>{{ item.title }}</span>
              <em>{{ item.desc }}</em>
            </router-link>
          </div>
        </section>
        <DashboardTodoCenter :todos="dashboard.todos" :loading="loading" />
      </div>
    </template>

    <template v-else>
      <div class="metric-grid">
        <article v-for="item in personalMetrics" :key="item.label" class="metric-card">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <em>{{ item.desc }}</em>
        </article>
      </div>

      <div class="dashboard-grid">
        <section class="dashboard-panel">
          <div class="panel-header">
            <h2>快捷入口</h2>
            <span>按当前角色展示</span>
          </div>
          <div class="shortcut-list">
            <router-link v-for="item in shortcuts" :key="item.path" :to="item.path" class="shortcut-item">
              <span>{{ item.title }}</span>
              <em>{{ item.desc }}</em>
            </router-link>
          </div>
        </section>

        <DashboardTodoCenter :todos="dashboard.todos" :loading="loading" />
      </div>
    </template>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useUserStore } from '@/store/user'
import { getAdminDashboard, getHrDashboard, getPersonalDashboard } from '@/api/dashboard'
import DashboardTodoCenter from '@/views/dashboard/DashboardTodoCenter.vue'

const chartLeft = 32
const chartRight = 608
const chartTop = 34
const chartBottom = 178
const pieColors = ['#4f46e5', '#2563eb', '#16a34a', '#d97706', '#dc2626', '#0891b2', '#7c3aed']

const userStore = useUserStore()
const isGeneralManager = computed(() => userStore.isGeneralManager)
const canManageEmployees = computed(() => userStore.canManageEmployees)
const isManagementUser = computed(() => userStore.canManageEmployees)
const roleName = computed(() => userStore.roleName)
const loading = ref(false)
const trendDays = ref(7)
const dashboard = ref({
  metrics: [],
  attendanceTrend: [],
  departmentPie: [],
  todos: []
})

const adminMetrics = computed(() => dashboard.value.metrics || [])
const personalMetrics = computed(() => dashboard.value.metrics || [])
const trendPoints = computed(() => dashboard.value.attendanceTrend || [])
const departmentPie = computed(() => dashboard.value.departmentPie || [])
const maxTrendValue = computed(() => Math.max(...trendPoints.value.map((item) => Number(item.value || 0)), 1))

const linePointObjects = computed(() => {
  const points = trendPoints.value
  if (!points.length) {
    return []
  }
  const span = points.length === 1 ? 0 : (chartRight - chartLeft) / (points.length - 1)
  return points.map((item, index) => {
    const value = Number(item.value || 0)
    const x = points.length === 1 ? (chartLeft + chartRight) / 2 : chartLeft + span * index
    const y = chartBottom - (value / maxTrendValue.value) * (chartBottom - chartTop)
    return { ...item, value, x, y }
  })
})
const linePoints = computed(() => linePointObjects.value.map((point) => `${point.x},${point.y}`).join(' '))
const areaPoints = computed(() => (linePoints.value ? `${chartLeft},${chartBottom} ${linePoints.value} ${chartRight},${chartBottom}` : ''))

const pieTotal = computed(() => departmentPie.value.reduce((sum, item) => sum + Number(item.value || 0), 0))
const pieLegend = computed(() =>
  departmentPie.value.map((item, index) => ({
    ...item,
    value: Number(item.value || 0),
    color: pieColors[index % pieColors.length]
  }))
)
const pieGradient = computed(() => {
  if (!pieTotal.value) {
    return 'conic-gradient(#e5e7eb 0 100%)'
  }
  let start = 0
  const segments = pieLegend.value.map((item) => {
    const percent = (item.value / pieTotal.value) * 100
    const end = start + percent
    const segment = `${item.color} ${start}% ${end}%`
    start = end
    return segment
  })
  return `conic-gradient(${segments.join(', ')})`
})

const shortcuts = computed(() => {
  const shared = [
    { title: '考勤打卡', desc: '上下班打卡与记录查询', path: '/attendance' },
    { title: '请假申请', desc: '提交请假并查看审批状态', path: '/leaves' },
    { title: '会议室预定', desc: '查看可用会议室并发起预定', path: '/meeting-bookings' },
    { title: '计划管理', desc: '查看计划并提交进度', path: '/plans' }
  ]
  if (!canManageEmployees.value) {
    return shared
  }
  return [
    { title: '员工档案', desc: '维护员工资料、账号和状态', path: '/employees' },
    { title: '组织架构', desc: '维护部门树和职位信息', path: '/departments' },
    { title: '企业公告', desc: '发布通知、制度和活动公告', path: '/notices' },
    ...shared
  ]
})

onMounted(fetchDashboard)

async function fetchDashboard() {
  loading.value = true
  try {
    const result = isManagementUser.value
      ? (isGeneralManager.value ? await getAdminDashboard({ trendDays: trendDays.value }) : await getHrDashboard())
      : await getPersonalDashboard()
    dashboard.value = result.data || { metrics: [], attendanceTrend: [], departmentPie: [], todos: [] }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.dashboard-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 20px;
  background: #fff;
  border: 1px solid var(--gray-200);
  border-radius: 8px;
  box-shadow: var(--panel-shadow);
}

.dashboard-actions {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.role-chip {
  padding: 4px 10px;
  color: var(--color-brand);
  background: #eef2ff;
  border: 1px solid #c7d2fe;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.admin-dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.admin-metrics {
  grid-template-columns: repeat(6, minmax(0, 1fr));
}

.metric-card,
.dashboard-panel {
  background: #fff;
  border: 1px solid var(--gray-200);
  border-radius: 8px;
  box-shadow: var(--panel-shadow);
}

.metric-card {
  padding: 20px;
}

.metric-card span,
.panel-header span,
.shortcut-item em,
.todo-list em {
  color: var(--gray-600);
  font-size: 12px;
  font-style: normal;
}

.metric-card strong {
  display: block;
  margin-top: 8px;
  color: var(--gray-900);
  font-size: 26px;
  line-height: 1.25;
}

.metric-card small {
  margin-left: 4px;
  color: var(--gray-500);
  font-size: 13px;
  font-weight: 500;
}

.metric-card em {
  display: block;
  margin-top: 6px;
  color: var(--gray-600);
  font-size: 13px;
  font-style: normal;
}

.dashboard-grid,
.chart-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(320px, 0.65fr);
  gap: 24px;
}

.dashboard-panel {
  padding: 20px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.panel-header h2 {
  margin: 0;
  color: var(--gray-900);
  font-size: 16px;
  font-weight: 650;
}

.line-chart {
  position: relative;
  min-height: 260px;
}

.line-chart svg {
  display: block;
  width: 100%;
  height: auto;
}

.chart-grid-lines line {
  stroke: var(--gray-200);
  stroke-width: 1;
}

.chart-area {
  fill: rgba(79, 70, 229, 0.1);
}

.chart-line {
  fill: none;
  stroke: var(--color-brand);
  stroke-linecap: round;
  stroke-linejoin: round;
  stroke-width: 3;
}

.chart-dot {
  fill: #fff;
  stroke: var(--color-brand);
  stroke-width: 2;
}

.chart-x-labels {
  fill: var(--gray-500);
  font-size: 12px;
}

.empty-chart {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--gray-500);
  font-size: 13px;
}

.pie-content {
  display: grid;
  grid-template-columns: 180px minmax(0, 1fr);
  gap: 20px;
  align-items: center;
}

.pie-chart {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 180px;
  height: 180px;
  border-radius: 50%;
}

.pie-chart::after {
  position: absolute;
  inset: 42px;
  content: "";
  background: #fff;
  border-radius: 50%;
}

.pie-chart span,
.pie-chart em {
  position: relative;
  z-index: 1;
}

.pie-chart span {
  color: var(--gray-900);
  font-size: 24px;
  font-weight: 700;
  line-height: 1.25;
}

.pie-chart em {
  color: var(--gray-500);
  font-size: 12px;
  font-style: normal;
}

.pie-legend {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 0;
  margin: 0;
  list-style: none;
}

.pie-legend li {
  display: grid;
  grid-template-columns: 10px minmax(0, 1fr) auto;
  gap: 8px;
  align-items: center;
  color: var(--gray-700);
  font-size: 13px;
}

.pie-legend i {
  width: 10px;
  height: 10px;
  border-radius: 3px;
}

.pie-legend strong {
  color: var(--gray-900);
  font-weight: 600;
}

.shortcut-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.admin-shortcuts {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.shortcut-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-height: 74px;
  padding: 14px;
  border: 1px solid var(--gray-200);
  border-radius: 6px;
  transition: background-color 0.16s ease, border-color 0.16s ease;
}

.shortcut-item:hover {
  background: #eef2ff;
  border-color: #c7d2fe;
}

.shortcut-item:active {
  background: #e0e7ff;
}

.shortcut-item span {
  color: var(--gray-900);
  font-weight: 600;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0;
  margin: 0;
  list-style: none;
}

.todo-list li {
  display: flex;
  gap: 10px;
  padding: 12px;
  border: 1px solid var(--gray-200);
  border-radius: 6px;
}

.todo-list strong {
  display: block;
  color: var(--gray-900);
  font-weight: 600;
}

.todo-dot {
  width: 8px;
  height: 8px;
  margin-top: 7px;
  border-radius: 4px;
}

.todo-dot.warning {
  background: var(--color-warning);
}

.todo-dot.info {
  background: var(--color-info);
}

.todo-dot.success {
  background: var(--color-success);
}

@media (max-width: 1280px) {
  .admin-metrics {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .admin-shortcuts {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1100px) {
  .metric-grid,
  .dashboard-grid,
  .chart-grid,
  .shortcut-list {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 760px) {
  .metric-grid,
  .dashboard-grid,
  .chart-grid,
  .shortcut-list,
  .pie-content {
    grid-template-columns: 1fr;
  }

  .dashboard-header {
    flex-direction: column;
  }

  .dashboard-actions {
    width: 100%;
    justify-content: space-between;
  }

  .pie-chart {
    margin: 0 auto;
  }
}
</style>
