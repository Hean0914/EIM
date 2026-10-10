<template>
  <div class="kanban-board">
    <section
      v-for="status in statuses"
      :key="status.value"
      :class="['kanban-column', `kanban-column--${status.key}`, { 'is-over': overStatus === status.value }]"
      @dragover.prevent="overStatus = status.value"
      @dragleave="handleDragLeave($event, status.value)"
      @drop.prevent="handleDrop(status.value)"
    >
      <header class="column-header">
        <div>
          <span class="status-mark" />
          <strong>{{ status.label }}</strong>
        </div>
        <span class="column-count">{{ groups[status.value]?.length || 0 }}</span>
      </header>

      <div class="column-body">
        <article
          v-for="plan in groups[status.value] || []"
          :key="plan.id"
          :class="['plan-card', { 'is-saving': savingPlanId === plan.id }]"
          :draggable="canManage(plan) && savingPlanId !== plan.id"
          tabindex="0"
          @click="emit('open', plan)"
          @keydown.enter.prevent="emit('open', plan)"
          @dragstart="handleDragStart($event, plan)"
          @dragend="handleDragEnd"
        >
          <div class="card-heading">
            <el-tag size="small" effect="plain">{{ typeMap[plan.planType] || '计划' }}</el-tag>
            <el-dropdown v-if="canManage(plan)" trigger="click" @command="(command) => handleCommand(command, plan)">
              <el-button text circle :icon="MoreFilled" aria-label="计划操作" @click.stop />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit" :icon="Edit">编辑</el-dropdown-item>
                  <el-dropdown-item command="progress" :icon="TrendCharts">提交进度</el-dropdown-item>
                  <el-dropdown-item command="delete" :icon="Delete" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <h3>{{ plan.title }}</h3>
          <p>{{ plan.content }}</p>

          <div class="card-meta">
            <span :class="['priority', `priority--${plan.priority || 2}`]">{{ priorityMap[plan.priority] || '中优先级' }}</span>
            <span :class="['deadline', `deadline--${deadlineState(plan).kind}`]">
              {{ deadlineState(plan).label }}
            </span>
          </div>

          <div class="date-range">
            <el-icon><Calendar /></el-icon>
            <span>{{ plan.startDate }} 至 {{ plan.endDate }}</span>
          </div>

          <div class="progress-row">
            <el-progress :percentage="plan.progress || 0" :stroke-width="6" :show-text="false" />
            <strong>{{ plan.progress || 0 }}%</strong>
          </div>
        </article>

        <div v-if="!(groups[status.value]?.length)" class="column-empty">
          暂无{{ status.label }}计划
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Calendar, Delete, Edit, MoreFilled, TrendCharts } from '@element-plus/icons-vue'

defineProps({
  groups: { type: Object, required: true },
  statuses: { type: Array, required: true },
  typeMap: { type: Object, required: true },
  canManage: { type: Function, required: true },
  deadlineState: { type: Function, required: true },
  savingPlanId: { type: Number, default: null }
})

const emit = defineEmits(['open', 'edit', 'progress', 'delete', 'status-change'])
const draggedPlan = ref(null)
const overStatus = ref(null)
const priorityMap = { 1: '低优先级', 2: '中优先级', 3: '高优先级' }

function handleDragStart(event, plan) {
  draggedPlan.value = plan
  event.dataTransfer.effectAllowed = 'move'
  event.dataTransfer.setData('text/plain', String(plan.id))
}

function handleDragEnd() {
  draggedPlan.value = null
  overStatus.value = null
}

function handleDragLeave(event, status) {
  if (!event.currentTarget.contains(event.relatedTarget)) {
    if (overStatus.value === status) {
      overStatus.value = null
    }
  }
}

function handleDrop(nextStatus) {
  const plan = draggedPlan.value
  overStatus.value = null
  draggedPlan.value = null
  if (!plan || plan.status === nextStatus) {
    return
  }
  emit('status-change', { plan, previousStatus: plan.status, nextStatus })
}

function handleCommand(command, plan) {
  emit(command, plan)
}
</script>

<style scoped>
.kanban-board {
  display: grid;
  grid-template-columns: repeat(5, minmax(248px, 1fr));
  gap: 12px;
  min-width: 1288px;
}

.kanban-column {
  min-height: 480px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-top: 3px solid var(--column-color, #94a3b8);
  border-radius: 6px;
  transition: background-color 0.16s ease, border-color 0.16s ease;
}

.kanban-column--pending { --column-color: #64748b; }
.kanban-column--active { --column-color: #2563eb; }
.kanban-column--completed { --column-color: #16a34a; }
.kanban-column--delayed { --column-color: #d97706; }
.kanban-column--cancelled { --column-color: #dc2626; }

.kanban-column.is-over {
  background: #eff6ff;
  border-color: #93c5fd;
}

.column-header,
.column-header > div,
.card-heading,
.card-meta,
.date-range,
.progress-row {
  display: flex;
  align-items: center;
}

.column-header {
  justify-content: space-between;
  height: 48px;
  padding: 0 12px;
  border-bottom: 1px solid #e2e8f0;
}

.column-header > div {
  gap: 8px;
}

.status-mark {
  width: 8px;
  height: 8px;
  background: var(--column-color);
  border-radius: 50%;
}

.column-header strong {
  color: #1e293b;
  font-size: 14px;
}

.column-count {
  min-width: 24px;
  padding: 2px 6px;
  color: #475569;
  text-align: center;
  background: #e2e8f0;
  border-radius: 4px;
  font-size: 12px;
}

.column-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 10px;
}

.plan-card {
  padding: 12px;
  cursor: pointer;
  background: #fff;
  border: 1px solid #dbe3ec;
  border-radius: 6px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.05);
}

.plan-card:hover,
.plan-card:focus-visible {
  border-color: #93c5fd;
  outline: none;
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.1);
}

.plan-card[draggable="true"] {
  cursor: grab;
}

.plan-card.is-saving {
  cursor: wait;
  opacity: 0.62;
}

.card-heading {
  justify-content: space-between;
  min-height: 28px;
}

.plan-card h3 {
  margin: 10px 0 6px;
  color: #0f172a;
  font-size: 14px;
  line-height: 1.45;
}

.plan-card p {
  display: -webkit-box;
  min-height: 36px;
  margin: 0 0 12px;
  overflow: hidden;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.card-meta {
  justify-content: space-between;
  gap: 6px;
}

.priority,
.deadline {
  font-size: 11px;
  line-height: 20px;
}

.priority {
  padding: 0 6px;
  border: 1px solid currentColor;
  border-radius: 4px;
}

.priority--1 { color: #64748b; }
.priority--2 { color: #2563eb; }
.priority--3 { color: #dc2626; }
.deadline { color: #64748b; }
.deadline--due-soon { color: #b45309; }
.deadline--overdue { color: #dc2626; font-weight: 600; }
.deadline--closed { color: #15803d; }

.date-range {
  gap: 5px;
  margin-top: 12px;
  color: #64748b;
  font-size: 11px;
}

.progress-row {
  gap: 8px;
  margin-top: 10px;
}

.progress-row .el-progress {
  flex: 1;
}

.progress-row strong {
  width: 32px;
  color: #334155;
  font-size: 11px;
  text-align: right;
}

.column-empty {
  display: grid;
  min-height: 100px;
  place-items: center;
  color: #94a3b8;
  border: 1px dashed #cbd5e1;
  border-radius: 6px;
  font-size: 12px;
}
</style>
