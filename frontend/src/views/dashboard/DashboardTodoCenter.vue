<template>
  <section v-loading="loading" class="dashboard-panel todo-center">
    <div class="panel-header">
      <div>
        <h2>待办中心</h2>
        <p>把需要关注的审批、考勤、计划和会议集中到这里。</p>
      </div>
      <span>{{ summary.total }} 项待办</span>
    </div>

    <div class="todo-summary" aria-label="待办摘要">
      <span><strong>{{ summary.total }}</strong>全部</span>
      <span class="todo-summary--urgent"><strong>{{ summary.highPriority }}</strong>高优先级</span>
      <span><strong>{{ summary.categories }}</strong>个分类</span>
    </div>

    <el-segmented v-model="activeCategory" :options="categoryOptions" class="todo-filter" />

    <div v-if="visibleTodos.length" class="todo-feed">
      <article v-for="item in visibleTodos" :key="item.key" class="todo-item">
        <div class="todo-item__icon" :class="`todo-item__icon--${item.semantic || 'info'}`">
          <el-icon><component :is="categoryIcons[item.category]" /></el-icon>
        </div>
        <div class="todo-item__body">
          <div class="todo-item__heading">
            <strong>{{ item.title }}</strong>
            <el-tag size="small" :type="priorityType(item.priority)" effect="plain">{{ priorityLabel(item.priority) }}</el-tag>
          </div>
          <p>{{ item.description }}</p>
        </div>
        <div class="todo-item__action">
          <strong>{{ item.count }}</strong>
          <el-button link type="primary" @click="go(item.targetPath)">去处理</el-button>
        </div>
      </article>
    </div>
    <el-empty v-else description="当前没有待办事项" :image-size="72" />
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'
import { Calendar, Checked, Clock, List } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { filterDashboardTodos, summarizeDashboardTodos } from '@/utils/dashboard-view'

const props = defineProps({
  todos: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false }
})

const router = useRouter()
const activeCategory = ref('all')
const categoryOptions = [
  { label: '全部', value: 'all' },
  { label: '审批', value: 'approval' },
  { label: '考勤', value: 'attendance' },
  { label: '计划', value: 'plan' },
  { label: '会议', value: 'meeting' }
]
const categoryIcons = { approval: Checked, attendance: Clock, plan: List, meeting: Calendar }
const summary = computed(() => summarizeDashboardTodos(props.todos))
const visibleTodos = computed(() => filterDashboardTodos(props.todos, activeCategory.value))

function priorityType(priority) {
  return { high: 'danger', medium: 'warning', low: 'info' }[priority] || 'info'
}

function priorityLabel(priority) {
  return { high: '高优先级', medium: '中优先级', low: '普通提醒' }[priority] || '提醒'
}

function go(path) {
  if (path) router.push(path)
}
</script>

<style scoped>
.todo-center { min-width: 0; }
.panel-header { align-items: flex-start; }
.panel-header h2 { margin: 0; color: var(--gray-900); font-size: 16px; font-weight: 650; }
.panel-header p { margin: 6px 0 0; color: var(--gray-600); font-size: 12px; line-height: 1.5; }
.todo-summary { display: flex; flex-wrap: wrap; gap: 18px; padding: 12px 0; color: var(--gray-600); font-size: 12px; border-bottom: 1px solid var(--gray-200); }
.todo-summary span { display: inline-flex; align-items: baseline; gap: 5px; }
.todo-summary strong { color: var(--gray-900); font-size: 18px; }
.todo-summary--urgent strong { color: #dc2626; }
.todo-filter { margin: 14px 0; }
.todo-feed { display: flex; flex-direction: column; gap: 10px; }
.todo-item { display: grid; grid-template-columns: 34px minmax(0, 1fr) auto; gap: 11px; align-items: center; padding: 12px; border: 1px solid var(--gray-200); border-radius: 6px; background: #fff; }
.todo-item__icon { display: grid; width: 32px; height: 32px; place-items: center; border-radius: 6px; }
.todo-item__icon--warning { color: #b45309; background: #fef3c7; }
.todo-item__icon--danger { color: #b91c1c; background: #fee2e2; }
.todo-item__icon--info { color: #1d4ed8; background: #dbeafe; }
.todo-item__icon--success { color: #15803d; background: #dcfce7; }
.todo-item__body { min-width: 0; }
.todo-item__heading { display: flex; align-items: center; gap: 8px; min-width: 0; }
.todo-item__heading strong { overflow: hidden; color: var(--gray-900); font-size: 13px; text-overflow: ellipsis; white-space: nowrap; }
.todo-item__body p { margin: 5px 0 0; overflow: hidden; color: var(--gray-600); font-size: 12px; text-overflow: ellipsis; white-space: nowrap; }
.todo-item__action { display: flex; align-items: center; gap: 10px; }
.todo-item__action > strong { min-width: 24px; color: var(--gray-900); font-size: 18px; text-align: right; }
@media (max-width: 620px) {
  .todo-item { grid-template-columns: 32px minmax(0, 1fr); }
  .todo-item__action { grid-column: 2; justify-content: space-between; }
  .todo-filter { width: 100%; overflow-x: auto; }
}
</style>
