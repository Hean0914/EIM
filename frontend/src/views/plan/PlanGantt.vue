<template>
  <section class="gantt-panel">
    <header class="gantt-toolbar">
      <div>
        <h2>{{ monthLabel }}</h2>
        <span>{{ visiblePlans.length }} 项计划覆盖本月</span>
      </div>
      <div class="month-actions">
        <el-button :icon="ArrowLeft" aria-label="上个月" @click="changeMonth(-1)" />
        <el-button @click="goToday">今天</el-button>
        <el-button :icon="ArrowRight" aria-label="下个月" @click="changeMonth(1)" />
      </div>
    </header>

    <div v-if="visiblePlans.length" class="gantt-scroll">
      <div class="gantt-content" :style="timelineWidthStyle">
        <div class="gantt-header-row">
          <div class="plan-info plan-info--header">计划</div>
          <div class="timeline-grid timeline-grid--header" :style="timelineGridStyle">
            <div
              v-for="day in days"
              :key="day.key"
              :class="['day-cell', { 'is-weekend': day.weekend, 'is-today': day.key === today }]"
            >
              <strong>{{ day.day }}</strong>
              <span>{{ weekdayNames[day.weekday] }}</span>
            </div>
          </div>
        </div>

        <div v-for="item in visiblePlans" :key="item.plan.id" class="gantt-row">
          <button class="plan-info plan-info--button" type="button" @click="emit('open', item.plan)">
            <strong>{{ item.plan.title }}</strong>
            <span>{{ typeMap[item.plan.planType] || '计划' }} · {{ item.plan.progress || 0 }}%</span>
          </button>
          <div class="timeline-grid timeline-grid--body" :style="timelineGridStyle">
            <span
              v-for="day in days"
              :key="day.key"
              :class="['day-background', { 'is-weekend': day.weekend, 'is-today': day.key === today }]"
            />
            <button
              :class="['gantt-bar', `gantt-bar--${item.plan.status}`, { 'is-overdue': item.segment.overdue }]"
              :style="barStyle(item.segment)"
              type="button"
              :title="`${item.plan.title}：${item.plan.startDate} 至 ${item.plan.endDate}`"
              @click="emit('open', item.plan)"
            >
              <span class="gantt-progress" :style="{ width: `${item.segment.progressPercent}%` }" />
              <strong>{{ item.plan.title }}</strong>
            </button>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-else description="当前月份没有计划" :image-size="90" />
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { buildMonthDays, getGanttSegment } from '@/utils/plan-view'

const props = defineProps({
  plans: { type: Array, required: true },
  monthValue: { type: String, required: true },
  today: { type: String, required: true },
  typeMap: { type: Object, required: true }
})

const emit = defineEmits(['open', 'month-change'])
const weekdayNames = ['日', '一', '二', '三', '四', '五', '六']
const days = computed(() => buildMonthDays(props.monthValue))
const monthLabel = computed(() => {
  const [year, month] = props.monthValue.split('-')
  return `${year} 年 ${Number(month)} 月`
})
const timelineGridStyle = computed(() => ({ gridTemplateColumns: `repeat(${days.value.length}, 34px)` }))
const timelineWidthStyle = computed(() => ({ minWidth: `${220 + days.value.length * 34}px` }))
const visiblePlans = computed(() => props.plans.flatMap((plan) => {
  const segment = getGanttSegment(plan, props.monthValue, props.today)
  return segment ? [{ plan, segment }] : []
}))

function barStyle(segment) {
  return { gridColumn: `${segment.startColumn} / span ${segment.span}` }
}

function changeMonth(offset) {
  const [year, month] = props.monthValue.split('-').map(Number)
  const date = new Date(Date.UTC(year, month - 1 + offset, 1))
  emit('month-change', formatMonth(date))
}

function goToday() {
  emit('month-change', `${props.today.slice(0, 7)}-01`)
}

function formatMonth(date) {
  const year = date.getUTCFullYear()
  const month = String(date.getUTCMonth() + 1).padStart(2, '0')
  return `${year}-${month}-01`
}
</script>

<style scoped>
.gantt-panel {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
}

.gantt-toolbar,
.month-actions {
  display: flex;
  align-items: center;
}

.gantt-toolbar {
  justify-content: space-between;
  min-height: 64px;
  padding: 0 16px;
  border-bottom: 1px solid #e2e8f0;
}

.gantt-toolbar h2 {
  margin: 0;
  color: #0f172a;
  font-size: 16px;
}

.gantt-toolbar span {
  display: block;
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}

.month-actions {
  gap: 6px;
}

.gantt-scroll {
  overflow: auto;
  max-height: 620px;
}

.gantt-content {
  position: relative;
}

.gantt-header-row,
.gantt-row {
  display: grid;
  grid-template-columns: 220px 1fr;
}

.gantt-header-row {
  position: sticky;
  top: 0;
  z-index: 4;
  height: 50px;
}

.gantt-row {
  min-height: 54px;
  border-top: 1px solid #edf2f7;
}

.plan-info {
  position: sticky;
  left: 0;
  z-index: 3;
  width: 220px;
  padding: 0 14px;
  background: #fff;
  border: 0;
  border-right: 1px solid #e2e8f0;
  text-align: left;
}

.plan-info--header {
  display: flex;
  align-items: center;
  color: #475569;
  background: #f8fafc;
  font-size: 12px;
  font-weight: 600;
}

.plan-info--button {
  display: flex;
  flex-direction: column;
  justify-content: center;
  cursor: pointer;
}

.plan-info--button:hover,
.plan-info--button:focus-visible {
  background: #f8fafc;
  outline: none;
}

.plan-info strong,
.plan-info span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.plan-info strong {
  color: #1e293b;
  font-size: 13px;
}

.plan-info span {
  margin-top: 4px;
  color: #64748b;
  font-size: 11px;
}

.timeline-grid {
  display: grid;
  min-width: 0;
}

.timeline-grid--header {
  background: #f8fafc;
}

.day-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #64748b;
  border-right: 1px solid #e2e8f0;
}

.day-cell strong {
  font-size: 12px;
}

.day-cell span {
  margin-top: 2px;
  font-size: 10px;
}

.day-cell.is-weekend,
.day-background.is-weekend {
  background: #f1f5f9;
}

.day-cell.is-today {
  color: #1d4ed8;
  background: #dbeafe;
}

.timeline-grid--body {
  position: relative;
  align-items: center;
}

.day-background {
  grid-row: 1;
  align-self: stretch;
  border-right: 1px solid #f1f5f9;
}

.day-background.is-today {
  background: rgba(37, 99, 235, 0.08);
  border-left: 1px solid #60a5fa;
}

.gantt-bar {
  position: relative;
  z-index: 2;
  grid-row: 1;
  align-self: center;
  height: 28px;
  margin: 0 2px;
  overflow: hidden;
  color: #fff;
  cursor: pointer;
  background: #64748b;
  border: 0;
  border-radius: 4px;
  text-align: left;
}

.gantt-bar--1 { background: #2563eb; }
.gantt-bar--2 { background: #16a34a; }
.gantt-bar--3 { background: #d97706; }
.gantt-bar--4 { background: #dc2626; }

.gantt-bar.is-overdue {
  box-shadow: inset 0 0 0 2px #991b1b;
}

.gantt-progress {
  position: absolute;
  inset: 0 auto 0 0;
  background: rgba(15, 23, 42, 0.2);
}

.gantt-bar strong {
  position: relative;
  z-index: 1;
  display: block;
  padding: 0 8px;
  overflow: hidden;
  font-size: 11px;
  line-height: 28px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
