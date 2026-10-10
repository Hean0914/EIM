<template>
  <el-drawer
    :model-value="modelValue"
    title="计划详情"
    size="520px"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
  >
    <template v-if="plan">
      <div class="drawer-heading">
        <div>
          <div class="heading-tags">
            <el-tag effect="plain">{{ typeMap[plan.planType] || '计划' }}</el-tag>
            <el-tag :type="statusTypeMap[plan.status]">{{ statusMap[plan.status] || '未知状态' }}</el-tag>
          </div>
          <h2>{{ plan.title }}</h2>
          <span>{{ plan.planNo }}</span>
        </div>
        <div class="drawer-actions">
          <el-button v-if="canUpdateProgress" :icon="TrendCharts" @click="emit('progress', plan)">提交进度</el-button>
          <el-button v-if="canManage" type="primary" :icon="Edit" @click="emit('edit', plan)">编辑</el-button>
        </div>
      </div>

      <section class="detail-section">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="优先级">{{ priorityMap[plan.priority] || '中' }}</el-descriptions-item>
          <el-descriptions-item label="完成进度">{{ plan.progress || 0 }}%</el-descriptions-item>
          <el-descriptions-item label="开始日期">{{ plan.startDate }}</el-descriptions-item>
          <el-descriptions-item label="截止日期">{{ plan.endDate }}</el-descriptions-item>
        </el-descriptions>
        <div class="detail-progress">
          <el-progress :percentage="plan.progress || 0" :stroke-width="10" />
          <span :class="`deadline--${deadline.kind}`">{{ deadline.label }}</span>
        </div>
      </section>

      <section class="detail-section">
        <h3>计划内容</h3>
        <p class="plan-content">{{ plan.content }}</p>
      </section>

      <section class="detail-section">
        <div class="section-heading">
          <h3>进展记录</h3>
          <el-button v-if="progressError" link type="primary" :icon="Refresh" @click="emit('retry')">重试</el-button>
        </div>
        <div v-loading="progressLoading" class="timeline-area">
          <el-alert v-if="progressError" :title="progressError" type="error" :closable="false" show-icon />
          <el-timeline v-else-if="progressItems.length">
            <el-timeline-item
              v-for="item in progressItems"
              :key="item.id"
              :timestamp="item.createTime"
              placement="top"
            >
              <div class="progress-entry">
                <strong>进度更新至 {{ item.progress }}%</strong>
                <p>{{ item.content }}</p>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else-if="!progressLoading" description="暂无进展记录" :image-size="72" />
        </div>
      </section>

      <div v-if="canManage" class="danger-zone">
        <el-button type="danger" plain :icon="Delete" @click="emit('delete', plan)">删除计划</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import { computed } from 'vue'
import { Delete, Edit, Refresh, TrendCharts } from '@element-plus/icons-vue'
import { getDeadlineState } from '@/utils/plan-view'

const props = defineProps({
  modelValue: { type: Boolean, required: true },
  plan: { type: Object, default: null },
  progressItems: { type: Array, default: () => [] },
  progressLoading: { type: Boolean, default: false },
  progressError: { type: String, default: '' },
  today: { type: String, required: true },
  typeMap: { type: Object, required: true },
  statusMap: { type: Object, required: true },
  statusTypeMap: { type: Object, required: true },
  canManage: { type: Boolean, default: false },
  canUpdateProgress: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'edit', 'progress', 'delete', 'retry'])
const priorityMap = { 1: '低', 2: '中', 3: '高' }
const deadline = computed(() => getDeadlineState(props.plan, props.today))
</script>

<style scoped>
.drawer-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 18px;
  border-bottom: 1px solid #e2e8f0;
}

.heading-tags,
.drawer-actions,
.section-heading,
.detail-progress {
  display: flex;
  align-items: center;
}

.heading-tags,
.drawer-actions {
  gap: 8px;
}

.drawer-heading h2 {
  margin: 10px 0 4px;
  color: #0f172a;
  font-size: 20px;
  line-height: 1.4;
}

.drawer-heading > div > span {
  color: #64748b;
  font-size: 12px;
}

.detail-section {
  padding: 20px 0;
  border-bottom: 1px solid #e2e8f0;
}

.detail-section h3 {
  margin: 0 0 12px;
  color: #1e293b;
  font-size: 15px;
}

.detail-progress {
  gap: 12px;
  margin-top: 16px;
}

.detail-progress .el-progress {
  flex: 1;
}

.detail-progress span {
  min-width: 84px;
  color: #64748b;
  font-size: 12px;
  text-align: right;
}

.detail-progress .deadline--due-soon { color: #b45309; }
.detail-progress .deadline--overdue { color: #dc2626; font-weight: 600; }
.detail-progress .deadline--closed { color: #15803d; }

.plan-content {
  margin: 0;
  color: #475569;
  font-size: 13px;
  line-height: 1.75;
  white-space: pre-wrap;
}

.section-heading {
  justify-content: space-between;
}

.timeline-area {
  min-height: 140px;
}

.progress-entry {
  padding: 10px 12px;
  background: #f8fafc;
  border-left: 3px solid #2563eb;
  border-radius: 4px;
}

.progress-entry strong {
  color: #1e293b;
  font-size: 13px;
}

.progress-entry p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 12px;
  line-height: 1.6;
}

.danger-zone {
  padding-top: 20px;
  text-align: right;
}

@media (max-width: 720px) {
  .drawer-heading {
    flex-direction: column;
  }
}
</style>
