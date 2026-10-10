<template>
  <el-drawer
    :model-value="modelValue"
    title="审批详情"
    size="540px"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
  >
    <template v-if="application">
      <div class="drawer-heading">
        <div>
          <div class="heading-tags">
            <el-tag effect="plain">{{ application.kindLabel }}</el-tag>
            <el-tag effect="plain">{{ application.typeLabel }}</el-tag>
            <el-tag :type="application.statusType">{{ application.statusLabel }}</el-tag>
          </div>
          <h2>{{ application.title }}</h2>
          <span>{{ application.applyNo || '申请单' }}</span>
        </div>
        <div class="drawer-actions">
          <el-button v-if="canApprove && application.status === 0" type="primary" @click="emit('approve', application)">
            审批
          </el-button>
          <el-button v-if="canCancel && application.status === 0" type="warning" plain @click="emit('cancel', application)">
            撤销
          </el-button>
        </div>
      </div>

      <section class="detail-section">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请人">
            {{ application.employeeName || (application.employeeId ? `员工 ${application.employeeId}` : '-') }}
          </el-descriptions-item>
          <el-descriptions-item label="申请类型">{{ application.typeLabel }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ application.startTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ application.endTime || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="application.kind === 'leave'" label="请假天数">
            {{ application.leaveDays || '-' }} 天
          </el-descriptions-item>
          <el-descriptions-item v-if="application.kind === 'out'" label="外出地点">
            {{ application.destination || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </section>

      <section class="detail-section">
        <h3>申请说明</h3>
        <p class="reason">{{ application.reason || '未填写申请说明' }}</p>
      </section>

      <section class="detail-section">
        <div class="section-heading">
          <h3>流程时间线</h3>
          <el-button v-if="error" link type="primary" @click="emit('retry')">重试</el-button>
        </div>
        <div v-loading="loading" class="timeline-area">
          <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
          <el-timeline v-else-if="timeline.length">
            <el-timeline-item
              v-for="item in timeline"
              :key="item.key"
              :timestamp="item.time || '时间待确认'"
              :type="item.type"
              placement="top"
            >
              <div class="timeline-entry">
                <strong>{{ item.label }}</strong>
                <span>{{ item.actor }}</span>
                <p v-if="item.comment">{{ item.comment }}</p>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else-if="!loading" description="暂无流程记录" :image-size="72" />
        </div>
      </section>
    </template>
  </el-drawer>
</template>

<script setup>
defineProps({
  modelValue: { type: Boolean, required: true },
  application: { type: Object, default: null },
  timeline: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' },
  canApprove: { type: Boolean, default: false },
  canCancel: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'approve', 'cancel', 'retry'])
</script>

<style scoped>
.drawer-heading,
.heading-tags,
.drawer-actions,
.section-heading,
.timeline-entry {
  display: flex;
}

.drawer-heading {
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 18px;
  border-bottom: 1px solid #e2e8f0;
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

.reason {
  margin: 0;
  color: #475569;
  font-size: 13px;
  line-height: 1.75;
  white-space: pre-wrap;
}

.section-heading {
  align-items: center;
  justify-content: space-between;
}

.timeline-area {
  min-height: 150px;
}

.timeline-entry {
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  background: #f8fafc;
  border-left: 3px solid #cbd5e1;
  border-radius: 4px;
}

.timeline-entry strong {
  color: #1e293b;
  font-size: 13px;
}

.timeline-entry span,
.timeline-entry p {
  color: #64748b;
  font-size: 12px;
}

.timeline-entry p {
  margin: 0;
  line-height: 1.6;
}

@media (max-width: 720px) {
  .drawer-heading {
    flex-direction: column;
  }
}
</style>
