<template>
  <div class="login-page">
    <LoginParticles />
    <header class="portal-header">
      <div class="brand-heading">
        <div class="brand-mark" aria-hidden="true"><el-icon><OfficeBuilding /></el-icon></div>
        <div class="brand-name">
          <strong>企业信息管理</strong>
          <span>Enterprise Office</span>
        </div>
      </div>
      <span class="portal-entry"><el-icon><Lock /></el-icon>内部账号入口</span>
    </header>

    <main class="portal-main">
      <section class="login-brand" aria-labelledby="system-title">
        <div class="brand-statement">
          <h1 id="system-title">企业信息管理系统</h1>
          <p>计划进度、审批处理、会议安排，一处查看。</p>
        </div>

        <details class="business-demo">
          <summary>查看工作空间示例<el-icon><ArrowRight /></el-icon></summary>
        <div class="preview-selector" aria-label="选择功能示例">
          <button
            v-for="preview in previews"
            :key="preview.key"
            type="button"
            :class="{ 'is-selected': activePreview === preview.key }"
            :aria-pressed="activePreview === preview.key"
            aria-controls="workspace-preview"
            @click="activePreview = preview.key"
          >
            <el-icon><component :is="preview.icon" /></el-icon>
            {{ preview.label }}
          </button>
        </div>

        <div id="workspace-preview" class="workspace-preview" aria-label="功能示例">
          <div class="workspace-heading">
            <div class="workspace-heading__title">
              <span class="workspace-marker"></span>
              <strong>{{ selectedPreview.label }}</strong>
            </div>
            <span class="example-label">功能示例</span>
          </div>
          <div class="workspace-body" aria-live="polite" aria-atomic="true">
            <Transition name="preview" mode="out-in">
              <div v-if="activePreview === 'plan'" key="plan">
                <div class="demo-toolbar">
                  <span>已完成 {{ completedCount }} / {{ tasks.length }}</span>
                  <el-tooltip content="重置示例任务" placement="top">
                    <button type="button" class="demo-icon-button" aria-label="重置示例任务" @click="resetTasks">
                      <el-icon><RefreshLeft /></el-icon>
                    </button>
                  </el-tooltip>
                </div>
                <div class="plan-preview">
                  <div v-for="column in planColumns" :key="column.key" class="plan-column">
                    <div class="plan-column__heading">
                      <span class="stage-dot" :class="column.tone"></span>
                      <span>{{ column.title }}</span>
                      <span class="item-count">{{ column.tasks.length }}</span>
                    </div>
                    <TransitionGroup name="task" tag="div" class="plan-task-list">
                      <article v-for="task in column.tasks" :key="task.id" class="plan-item">
                        <span class="item-category">{{ task.category }}</span>
                        <h3>{{ task.task }}</h3>
                        <div class="plan-progress" role="progressbar" :aria-label="task.task + '完成进度'" :aria-valuenow="task.progress" aria-valuemin="0" aria-valuemax="100">
                          <span :style="{ width: task.progress + '%' }" :class="column.tone"></span>
                        </div>
                        <div class="plan-item__footer">
                          <span>{{ task.deadline }}</span>
                          <span class="employee-avatar" :class="column.tone">{{ task.owner }}</span>
                        </div>
                        <div class="task-action">
                          <span>{{ task.progress }}%</span>
                          <el-tooltip v-if="task.status !== 'done'" content="标记示例任务完成" placement="top">
                            <button type="button" class="demo-icon-button" :aria-label="'完成示例任务：' + task.task" @click="completeTask(task.id)">
                              <el-icon><Check /></el-icon>
                            </button>
                          </el-tooltip>
                          <el-icon v-else class="task-complete" aria-label="已完成"><CircleCheckFilled /></el-icon>
                        </div>
                      </article>
                    </TransitionGroup>
                    <p v-if="!column.tasks.length" class="column-empty">暂无任务</p>
                    <span class="column-note">{{ column.note }}</span>
                  </div>
                </div>
              </div>

              <div v-else-if="activePreview === 'approval'" key="approval" class="approval-preview">
                <div class="approval-summary">
                  <el-icon><Document /></el-icon>
                  <div><h3>办公用品采购申请</h3><span>行政部 · 示例申请</span></div>
                  <span class="state-label" :class="approvalStep === 3 ? 'state-label--green' : 'state-label--amber'">{{ approvalStep === 3 ? '已通过' : approvalRunning ? '审批中' : '待演示' }}</span>
                </div>
                <ol class="approval-timeline">
                  <li v-for="(step, index) in approvalExample" :key="step.title" :class="{ complete: index < approvalStep, current: index === approvalStep, 'is-running': approvalRunning && index === approvalStep }">
                    <span class="timeline-node">
                      <el-icon v-if="index < approvalStep"><Check /></el-icon>
                      <el-icon v-else-if="index === approvalStep"><Clock /></el-icon>
                      <span v-else></span>
                    </span>
                    <strong>{{ step.title }}</strong>
                    <small>{{ index < approvalStep ? '已完成' : index === approvalStep && approvalRunning ? '正在处理' : step.detail }}</small>
                  </li>
                </ol>
                <p class="approval-note"><el-icon><Clock /></el-icon>{{ approvalStep === 3 ? '审批完成，结果已通知申请人（示例）' : '当前节点：' + approvalExample[approvalStep].title }}</p>
                <el-button class="approval-play" :disabled="approvalRunning" :icon="approvalStep === 3 ? RefreshLeft : VideoPlay" @click="startApproval">
                  {{ approvalRunning ? '演示进行中' : approvalStep === 3 ? '重新演示' : '演示审批' }}
                </el-button>
              </div>

              <div v-else-if="activePreview === 'meeting'" key="meeting" class="meeting-preview">
                <div class="agenda-heading"><strong>会议日程</strong><span>2026 年 10 月 · 示例</span></div>
                <div class="demo-dates" aria-label="示例会议日期">
                  <button v-for="day in demoDays" :key="day.key" type="button" :aria-pressed="selectedDay === day.key" :class="{ 'is-selected': selectedDay === day.key }" @click="selectedDay = day.key">
                    <span>{{ day.label }}</span><small>{{ day.weekday }}</small>
                  </button>
                </div>
                <Transition name="preview" mode="out-in">
                  <div :key="selectedDay">
                    <details v-for="meeting in meetings" :key="meeting.id" class="meeting-details">
                      <summary class="agenda-item">
                        <time>{{ meeting.start }}<small>{{ meeting.end }}</small></time>
                        <div class="agenda-item__content">
                          <h3>{{ meeting.title }}</h3>
                          <span>{{ meeting.location }}</span>
                        </div>
                        <el-icon class="meeting-chevron"><ArrowRight /></el-icon>
                      </summary>
                      <div class="meeting-detail-body">
                        <dl>
                          <dt>时间</dt><dd>{{ meeting.start }} - {{ meeting.end }}</dd>
                          <dt>地点</dt><dd>{{ meeting.location }}</dd>
                          <dt>参会</dt><dd>{{ meeting.attendees }}</dd>
                          <dt>议题</dt><dd>{{ meeting.agenda }}</dd>
                        </dl>
                      </div>
                    </details>
                    <p v-if="!meetings.length" class="agenda-empty"><el-icon><Calendar /></el-icon>当天暂无会议安排</p>
                  </div>
                </Transition>
              </div>
            </Transition>
          </div>
          <div class="workspace-caption"><el-icon><InfoFilled /></el-icon>示例内容，非实际业务数据</div>
        </div>

        </details>

        <div class="brand-module-list" aria-label="系统模块">
          <span v-for="module in modules" :key="module.key" class="brand-module">
            <el-icon><component :is="module.icon" /></el-icon>{{ module.label }}
          </span>
        </div>
      </section>

      <section class="login-form-side" aria-labelledby="login-title">
        <div class="login-card">
          <div class="login-card__header">
            <span class="login-identity" aria-hidden="true"><el-icon><User /></el-icon></span>
            <div>
              <h2 id="login-title">账号登录</h2>
              <p>进入你的企业工作空间</p>
            </div>
          </div>

          <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="handleLogin">
            <el-form-item class="login-field" label="用户名" prop="username">
              <el-input
                id="login-username"
                v-model.trim="form.username"
                placeholder="请输入用户名"
                :prefix-icon="User"
                autocomplete="username"
                clearable
                data-testid="login-username"
                @focus="activeModule = 'dashboard'"
                @blur="resetActiveModule"
              />
            </el-form-item>
            <el-form-item class="login-field" label="密码" prop="password">
              <el-input
                id="login-password"
                v-model="form.password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                autocomplete="current-password"
                show-password
                type="password"
                clearable
                data-testid="login-password"
                @focus="activeModule = 'security'"
                @blur="resetActiveModule"
              />
            </el-form-item>

            <el-button
              class="login-button"
              type="primary"
              :loading="loading"
              data-testid="login-submit"
              @click="handleLogin"
            >
              {{ loading ? '正在进入工作台' : '登录工作台' }}
              <el-icon v-if="!loading"><ArrowRight /></el-icon>
            </el-button>
          </el-form>

          <div class="login-status" aria-live="polite">
            <span v-if="loading">正在验证账号，请稍候</span>
          </div>
          <div class="login-support">
            <p class="login-account-note"><el-icon><InfoFilled /></el-icon>账号由系统管理员统一分配</p>
            <div class="login-support__contact">
              <span>忘记密码或无法登录</span>
              <strong>联系管理员</strong>
            </div>
          </div>
        </div>
      </section>
    </main>

    <footer class="brand-footer">
      <p class="brand-copyright">内部管理系统 · 统一工作入口</p>
      <div class="brand-status" aria-live="polite">
        <el-icon><component :is="loading ? Clock : Lock" /></el-icon>
        <span>{{ loading ? '正在验证登录信息' : activeModuleLabel }}</span>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowRight,
  Bell,
  Calendar,
  Check,
  CircleCheckFilled,
  Checked,
  Clock,
  Document,
  InfoFilled,
  List,
  Lock,
  OfficeBuilding,
  Odometer,
  RefreshLeft,
  VideoPlay,
  User
} from '@element-plus/icons-vue'
import { login } from '@/api/auth'
import { useUserStore } from '@/store/user'
import { demoDays, useLoginDemo } from '@/utils/loginDemo'
import LoginParticles from '@/components/LoginParticles.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const activeModule = ref('dashboard')
const { activePreview, tasks, completeTask, resetTasks, approvalStep, approvalRunning, startApproval, selectedDay, meetings } = useLoginDemo()
const completedCount = computed(() => tasks.value.filter((task) => task.status === 'done').length)

const previews = [
  { key: 'plan', label: '计划看板', icon: List },
  { key: 'approval', label: '审批流程', icon: Checked },
  { key: 'meeting', label: '会议日程', icon: Calendar }
]
const selectedPreview = computed(() => previews.find((preview) => preview.key === activePreview.value))

const modules = [
  { key: 'dashboard', label: '工作台', icon: Odometer },
  { key: 'plan', label: '计划管理', icon: List },
  { key: 'approval', label: '审批流程', icon: Checked },
  { key: 'attendance', label: '考勤管理', icon: Clock },
  { key: 'office', label: '日常办公', icon: Calendar },
  { key: 'message', label: '统一消息', icon: Bell }
]

const planColumns = computed(() => [
  { key: 'pending', title: '待开始', tone: 'neutral', note: '明确下一步工作' },
  { key: 'active', title: '进行中', tone: 'blue', note: '跟进执行进度' },
  { key: 'done', title: '已完成', tone: 'green', note: '保留完成记录' }
].map((column) => ({ ...column, tasks: tasks.value.filter((task) => task.status === column.key) })))

const approvalExample = [
  { title: '提交申请', detail: '等待提交' },
  { title: '部门审核', detail: '等待负责人处理' },
  { title: '审批完成', detail: '结果统一通知' }
]

const form = reactive({
  username: 'admin',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const activeModuleLabel = computed(() => activeModule.value === 'security' ? '权限验证' : '企业账号登录')

function resetActiveModule() {
  if (!loading.value) {
    activeModule.value = 'dashboard'
  }
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  loading.value = true
  activeModule.value = 'security'
  try {
    const result = await login(form)
    const data = result.data || {}
    userStore.setLoginInfo(data.token, data.user)
    ElMessage.success('登录成功')
    router.replace(route.query.redirect || '/dashboard')
  } finally {
    loading.value = false
    activeModule.value = 'dashboard'
  }
}
</script>

<style scoped>
.login-page {
  --portal-ink: #202734;
  --portal-muted: #657080;
  --portal-line: #ccd6e3;
  --portal-blue: var(--color-brand);
  position: relative;
  isolation: isolate;
  display: flex;
  flex-direction: column;
  min-height: 100svh;
  padding: 0 48px;
  color: var(--portal-ink);
  background: var(--color-navigation-bg);
  letter-spacing: 0;
}

.portal-header,
.portal-main,
.brand-footer {
  position: relative;
  width: 100%;
  max-width: 1180px;
  margin: 0 auto;
}

.portal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  min-height: 100px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.14);
}

.brand-heading {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-mark {
  display: grid;
  place-items: center;
  flex: 0 0 38px;
  width: 38px;
  height: 38px;
  color: #fff;
  background: var(--portal-blue);
  border-radius: 8px;
  font-size: 22px;
}

.brand-name strong {
  color: #f8fafc;
  display: block;
  font-size: 17px;
  font-weight: 650;
  line-height: 1.5;
}

.brand-name span {
  display: block;
  color: var(--color-navigation-muted);
  font-size: 11px;
}

.portal-entry {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: #b7c5db;
  font-size: 12px;
}

.portal-main {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(300px, 1fr);
  align-items: center;
  align-content: center;
  flex: 1;
  gap: 48px;
  padding: 56px 0 64px;
}

.login-brand,
.login-form-side {
  min-width: 0;
}

.brand-statement h1 {
  max-width: 540px;
  color: #f8fafc;
  margin: 0;
  font-size: 44px;
  font-weight: 650;
  line-height: 1.35;
}

.brand-statement p {
  margin: 12px 0 30px;
  color: #b7c5db;
  font-size: 16px;
  line-height: 1.7;
}

.preview-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 22px;
  margin-bottom: 16px;
}

.preview-selector button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  min-height: 38px;
  padding: 0 0 9px;
  color: var(--portal-muted);
  background: transparent;
  border: 0;
  border-bottom: 2px solid transparent;
  font: inherit;
  font-size: 13px;
  cursor: pointer;
  transition: color 150ms ease, border-color 150ms ease;
}

.preview-selector button:hover,
.preview-selector button.is-selected {
  color: var(--portal-blue);
}

.preview-selector button.is-selected {
  border-bottom-color: var(--portal-blue);
}

.business-demo {
  margin-top: 36px;
}

.business-demo > summary {
  display: inline-flex;
  align-items: center;
  gap: 16px;
  min-height: 44px;
  color: #e0e8f6;
  border-bottom: 1px solid #53627b;
  list-style: none;
  cursor: pointer;
  font-size: 13px;
}

.business-demo > summary::-webkit-details-marker {
  display: none;
}

.business-demo > summary:focus-visible {
  outline: 2px solid var(--color-brand);
  outline-offset: 4px;
}

.business-demo[open] > summary {
  margin-bottom: 20px;
}

.business-demo[open] > summary .el-icon {
  transform: rotate(90deg);
}

.business-demo .preview-selector button {
  color: #b7c5db;
}

.business-demo .preview-selector button.is-selected {
  color: #fff;
}

.preview-selector button:focus-visible {
  outline: 2px solid var(--portal-blue);
  outline-offset: 4px;
}

.workspace-preview {
  overflow: hidden;
  background: #f3f5f7;
  border: 1px solid var(--portal-line);
  border-radius: 8px;
}

.workspace-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-height: 54px;
  padding: 12px 20px;
  color: #fff;
  background: var(--portal-blue);
  border-bottom: 1px solid var(--portal-blue);
}

.workspace-heading__title {
  display: flex;
  align-items: center;
  gap: 9px;
  font-size: 13px;
}

.workspace-marker {
  width: 4px;
  height: 15px;
  background: #fff;
  border-radius: 2px;
}

.example-label {
  color: #e3ecff;
  font-size: 11px;
}

.workspace-body {
  height: 400px;
  overflow-y: auto;
  scrollbar-gutter: stable;
  padding: 20px 18px;
}

.plan-preview {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.plan-column {
  min-width: 0;
}

.plan-column__heading {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 14px;
  font-size: 12px;
}

.stage-dot {
  flex: 0 0 6px;
  width: 6px;
  height: 6px;
  background: #929cab;
  border-radius: 50%;
}

.stage-dot.blue,
.plan-progress .blue {
  background: var(--portal-blue);
}

.stage-dot.green,
.plan-progress .green {
  background: #248260;
}

.item-count {
  margin-left: auto;
  color: var(--portal-muted);
  font-size: 11px;
}

.plan-item {
  padding: 14px 12px;
  background: #fff;
  border: 1px solid #e8ecf1;
  border-radius: 6px;
}

.item-category {
  color: var(--portal-muted);
  font-size: 10px;
}

.plan-item h3,
.approval-summary h3,
.agenda-item h3 {
  margin: 8px 0 20px;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.6;
  overflow-wrap: anywhere;
}

.plan-progress {
  height: 4px;
  overflow: hidden;
  margin-bottom: 14px;
  background: #edf0f4;
  border-radius: 2px;
}

.plan-progress span {
  display: block;
  height: 100%;
  transition: width 320ms ease, background-color 220ms ease;
}

.plan-item__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 6px;
  color: var(--portal-muted);
  font-size: 10px;
}

.employee-avatar {
  display: grid;
  place-items: center;
  width: 24px;
  height: 24px;
  color: #566477;
  background: #ecf0f5;
  border-radius: 50%;
  font-size: 11px;
}

.employee-avatar.blue {
  color: var(--portal-blue);
  background: #e8efff;
}

.employee-avatar.green {
  color: #248260;
  background: #e7f3ec;
}

.column-note {
  display: block;
  margin-top: 14px;
  color: #6b7583;
  font-size: 10px;
}

.workspace-caption {
  display: flex;
  align-items: center;
  gap: 6px;
  min-height: 35px;
  padding: 8px 18px;
  color: var(--portal-muted);
  border-top: 1px solid var(--portal-line);
  font-size: 10px;
}

.brand-module-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 20px;
  margin-top: 36px;
  max-width: 470px;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.14);
}

.brand-module {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #b7c5db;
  font-size: 11px;
}

.brand-module :deep(.el-icon) {
  color: var(--color-navigation-muted);
  font-size: 13px;
}

.approval-summary {
  display: flex;
  align-items: center;
  gap: 12px;
}

.approval-summary > .el-icon {
  display: grid;
  place-items: center;
  flex: 0 0 38px;
  width: 38px;
  height: 38px;
  color: var(--portal-blue);
  background: #e7edfa;
  border-radius: 6px;
  font-size: 22px;
}

.approval-summary h3 {
  margin: 0 0 3px;
}

.approval-summary span:not(.state-label) {
  color: var(--portal-muted);
  font-size: 11px;
}

.state-label {
  flex-shrink: 0;
  color: var(--portal-blue);
  font-size: 11px;
}

.state-label--amber {
  color: #97621c;
}

.state-label--gray {
  color: var(--portal-muted);
}

.approval-summary .state-label {
  margin-left: auto;
}

.approval-timeline {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  margin: 24px 0 18px;
  padding: 0;
  list-style: none;
}

.approval-timeline li {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  text-align: center;
}

.approval-timeline li:not(:last-child)::after {
  position: absolute;
  top: 13px;
  left: calc(50% + 20px);
  width: calc(100% - 40px);
  height: 1px;
  content: "";
  background: #cdd5df;
  transition: background-color 320ms ease;
}

.approval-timeline li.complete::after {
  background: #248260;
}

.timeline-node {
  display: grid;
  place-items: center;
  width: 28px;
  height: 28px;
  color: #919aa7;
  background: #e7ebf0;
  border-radius: 50%;
  transition: background-color 250ms ease, color 250ms ease, box-shadow 250ms ease;
}

.timeline-node > span {
  width: 6px;
  height: 6px;
  background: #919aa7;
  border-radius: 50%;
}

.complete .timeline-node {
  color: #248260;
  background: #e1f1e8;
}

.current .timeline-node {
  color: #fff;
  background: var(--portal-blue);
}

.approval-timeline strong {
  font-size: 12px;
  font-weight: 500;
}

.approval-timeline small {
  color: var(--portal-muted);
  font-size: 10px;
}

.approval-note {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0;
  padding-top: 12px;
  color: var(--portal-muted);
  border-top: 1px solid var(--portal-line);
  font-size: 11px;
}

.agenda-heading {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
  font-size: 12px;
}

.agenda-heading span {
  color: var(--portal-muted);
  font-size: 11px;
}

.agenda-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 0;
  border-bottom: 1px solid var(--portal-line);
}

.agenda-item:last-child {
  border-bottom: 0;
}

.agenda-item time {
  flex: 0 0 45px;
  font-size: 13px;
  font-variant-numeric: tabular-nums;
}

.agenda-item time small {
  display: block;
  margin-top: 2px;
  color: var(--portal-muted);
  font-size: 10px;
}

.agenda-item__content {
  flex: 1;
  min-width: 0;
  padding-left: 12px;
  border-left: 2px solid #b4c9f4;
}

.agenda-item h3 {
  margin: 0 0 2px;
  font-size: 12px;
}

.agenda-item__content > span {
  color: var(--portal-muted);
  font-size: 11px;
}

.login-form-side {
  --login-surface: color-mix(in srgb, var(--color-navigation-bg), white 5%);
  --portal-ink: #f1f5f9;
  --portal-muted: #b7c5db;
  --portal-line: #3d4a60;
  --el-color-danger: #fda4af;
  --el-text-color-regular: #b7c5db;
  --el-text-color-placeholder: #a3b1c7;
  --el-input-text-color: #f1f5f9;
  display: flex;
  justify-content: center;
  padding: 44px 36px 32px;
  color: #f1f5f9;
  background: var(--login-surface);
  border: 1px solid var(--portal-line);
  border-top: 3px solid var(--color-brand);
  animation: login-reveal 320ms ease-out;
}

.login-card {
  width: 100%;
  max-width: 340px;
}

.login-card__header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 36px;
}

.login-identity {
  display: grid;
  place-items: center;
  flex: 0 0 48px;
  width: 48px;
  height: 48px;
  color: var(--el-color-primary-light-5);
  background: rgba(var(--color-brand-rgb), 0.12);
  border: 1px solid rgba(var(--color-brand-rgb), 0.45);
  border-radius: 6px;
  font-size: 24px;
}

.login-card__header h2 {
  margin: 0;
  font-size: 26px;
  font-weight: 650;
  line-height: 1.4;
}

.login-card__header p {
  margin: 6px 0 0;
  color: var(--portal-muted);
  font-size: 13px;
}

.login-field {
  margin-bottom: 28px;
}

.login-field :deep(.el-form-item__label) {
  margin-bottom: 10px;
  padding: 0;
  color: var(--portal-ink);
  font-size: 13px;
  font-weight: 500;
  line-height: 1.5;
  transition: color 150ms ease;
}

.login-field:focus-within :deep(.el-form-item__label) {
  color: var(--el-color-primary-light-5);
}

.login-field:focus-within :deep(.el-input__prefix) {
  color: var(--el-color-primary-light-5);
}

.login-field :deep(.el-form-item__label::before) {
  color: var(--el-color-danger);
}

.login-field :deep(.el-input__wrapper) {
  min-height: 54px;
  padding: 0 14px;
  background: var(--color-navigation-bg);
  border-radius: 6px !important;
  box-shadow: 0 0 0 1px #46546c inset !important;
  transition: background-color 150ms ease, box-shadow 150ms ease;
}

.login-field :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #7a8da9 inset !important;
}

.login-field :deep(.el-input__wrapper.is-focus) {
  background: var(--color-navigation-bg);
  box-shadow: 0 0 0 1px var(--portal-blue) inset, 0 0 0 3px rgba(var(--color-brand-rgb), 0.2) !important;
}

.login-field :deep(.el-input__prefix) {
  align-self: center;
  height: 24px;
  margin-right: 14px;
  padding-right: 12px;
  color: #a3b1c7;
  border-right: 1px solid var(--portal-line);
  font-size: 17px;
  transition: color 150ms ease;
}

.login-field :deep(.el-input__inner) {
  color: inherit;
  font-size: 14px;
  caret-color: var(--el-color-primary-light-5);
}

.login-field :deep(.el-input__inner::placeholder) {
  color: #a3b1c7;
}

.login-field :deep(.el-input__inner:-webkit-autofill) {
  -webkit-text-fill-color: var(--portal-ink);
  box-shadow: 0 0 0 100px var(--color-navigation-bg) inset;
}

.login-field :deep(.el-input__suffix) {
  color: #a3b1c7;
}

.login-field :deep(.el-input__suffix .el-icon:hover) {
  color: var(--portal-ink);
}

.login-field.is-error :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--el-color-danger) inset !important;
}

.login-field :deep(.el-form-item__error) {
  padding-top: 7px;
  font-size: 12px;
}

.login-account-note {
  display: flex;
  align-items: center;
  align-items: flex-start;
  gap: 8px;
  margin: 0;
  color: var(--portal-muted);
  font-size: 12px;
}

.login-account-note > .el-icon {
  flex-shrink: 0;
  margin-top: 3px;
}

.login-button {
  --el-button-bg-color: var(--portal-blue);
  --el-button-border-color: var(--portal-blue);
  --el-button-hover-bg-color: var(--color-brand-hover);
  --el-button-hover-border-color: var(--color-brand-hover);
  --el-button-active-bg-color: var(--color-brand-active);
  --el-button-active-border-color: var(--color-brand-active);
  width: 100%;
  min-height: 54px;
  padding: 0 20px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
}

.login-button :deep(> span) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.login-button :deep(.el-icon:not(.is-loading)) {
  margin-left: 10px;
  transition: transform 150ms ease;
}

.login-button:hover :deep(.el-icon:not(.is-loading)) {
  transform: translateX(3px);
}

.login-button.is-loading :deep(> span) {
  width: auto;
}

.login-button:hover {
  transform: none;
}

.login-button:focus-visible {
  outline: 2px solid var(--portal-blue);
  outline-offset: 3px;
}

.login-status {
  min-height: 26px;
  margin-top: 10px;
  color: var(--portal-muted);
  font-size: 12px;
}

.login-support {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--portal-line);
  font-size: 12px;
}

.login-support span {
  color: var(--portal-muted);
}

.login-support strong {
  color: #f1f5f9;
  font-weight: 500;
}

.login-support__contact {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 6px 16px;
}

@keyframes login-reveal {
  from { opacity: 0; transform: translateY(6px); }
  to { opacity: 1; transform: translateY(0); }
}

.brand-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px 24px;
  min-height: 64px;
  padding: 18px 0;
  border-top: 1px solid rgba(255, 255, 255, 0.14);
}

.brand-copyright {
  margin: 0;
  color: #b7c5db;
  font-size: 11px;
}

.brand-status {
  display: flex;
  align-items: center;
  gap: 7px;
  color: #b7c5db;
  font-size: 11px;
}

.preview-enter-active,
.preview-leave-active {
  transition: opacity 120ms ease;
}

.preview-enter-from,
.preview-leave-to {
  opacity: 0;
}

.demo-toolbar,
.task-action {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  color: var(--portal-muted);
  font-size: 11px;
}

.demo-toolbar {
  margin-bottom: 12px;
}

.task-action {
  min-height: 32px;
  margin-top: 10px;
}

.demo-icon-button {
  display: inline-grid;
  place-items: center;
  flex: 0 0 32px;
  width: 32px;
  height: 32px;
  padding: 0;
  color: var(--portal-blue);
  background: #e8efff;
  border: 1px solid #c5d5f9;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 150ms ease;
}

.demo-icon-button:hover {
  background: #d7e4ff;
}

.demo-icon-button:focus-visible,
.demo-dates button:focus-visible,
.meeting-details summary:focus-visible {
  outline: 2px solid var(--portal-blue);
  outline-offset: 2px;
}

.plan-task-list {
  position: relative;
  display: grid;
  gap: 10px;
}

.column-empty {
  margin: 0;
  padding: 20px 4px;
  color: var(--portal-muted);
  border: 1px dashed var(--portal-line);
  border-radius: 6px;
  text-align: center;
  font-size: 11px;
}

.task-complete,
.state-label--green {
  color: #248260;
}

.task-enter-active,
.task-leave-active,
.task-move {
  transition: transform 280ms ease, opacity 220ms ease;
}

.task-enter-from {
  opacity: 0;
  transform: translateX(-12px);
}

.task-leave-to {
  opacity: 0;
  transform: translateX(12px);
}

.task-leave-active {
  position: absolute;
  width: 100%;
  pointer-events: none;
}

.approval-timeline .is-running .timeline-node {
  animation: approval-pulse 1s ease-in-out infinite;
}

.approval-play {
  margin-top: 24px;
}

.demo-dates {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  margin: 16px 0 12px;
}

.demo-dates button {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  min-height: 58px;
  color: var(--portal-muted);
  background: #fff;
  border: 1px solid var(--portal-line);
  border-radius: 6px;
  font: inherit;
  font-size: 12px;
  cursor: pointer;
  transition: color 150ms ease, background-color 150ms ease;
}

.demo-dates button.is-selected {
  color: #fff;
  background: var(--portal-blue);
  border-color: var(--portal-blue);
}

.demo-dates small {
  font-size: 10px;
}

.meeting-details summary {
  list-style: none;
  cursor: pointer;
}

.meeting-details summary::-webkit-details-marker {
  display: none;
}

.meeting-details summary:hover {
  color: var(--portal-blue);
}

.meeting-chevron {
  flex-shrink: 0;
  transition: transform 180ms ease;
}

.meeting-details[open] .meeting-chevron {
  transform: rotate(90deg);
}

.meeting-details[open] .meeting-detail-body {
  animation: detail-reveal 180ms ease-out;
}

.meeting-detail-body dl {
  display: grid;
  grid-template-columns: 30px minmax(0, 1fr);
  gap: 7px 12px;
  margin: 0 0 12px;
  padding: 12px;
  background: #e9eef5;
  font-size: 11px;
}

.meeting-detail-body dt {
  color: var(--portal-muted);
}

.meeting-detail-body dd {
  margin: 0;
  overflow-wrap: anywhere;
}

.agenda-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 150px;
  color: var(--portal-muted);
  font-size: 13px;
}

@keyframes approval-pulse {
  50% { box-shadow: 0 0 0 5px rgba(var(--color-brand-rgb), 0.12); }
}

@keyframes detail-reveal {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 1100px) {
  .portal-main {
    gap: 40px;
  }

  .login-form-side {
    padding: 32px 24px;
  }
}

@media (max-width: 900px) {
  .login-page {
    padding: 0 24px;
  }

  .portal-header {
    min-height: 82px;
  }

  .portal-main {
    grid-template-columns: minmax(0, 1fr);
    gap: 48px;
    max-width: 640px;
    padding: 36px 0 48px;
  }

  .login-form-side {
    padding: 32px;
  }

  .login-card {
    max-width: 440px;
  }
}

@media (max-width: 480px) {
  .login-form-side {
    padding: 28px 20px;
  }

  .login-page {
    padding: 0 20px;
  }

  .portal-entry {
    font-size: 11px;
  }

  .brand-name strong {
    font-size: 15px;
  }

  .brand-name span {
    font-size: 10px;
  }

  .brand-statement h1 {
    font-size: 26px;
  }

  .brand-statement p {
    font-size: 13px;
  }

  .preview-selector {
    gap: 18px;
  }

  .preview-selector button {
    font-size: 12px;
  }

  .workspace-heading {
    padding: 12px;
  }

  .workspace-body {
    padding: 16px 10px;
  }

  .plan-preview {
    gap: 7px;
  }

  .plan-column__heading {
    gap: 4px;
    font-size: 11px;
  }

  .plan-item {
    padding: 12px 8px;
  }

  .plan-item h3 {
    min-height: 40px;
    font-size: 12px;
  }

  .column-note {
    font-size: 10px;
  }

  .approval-summary {
    gap: 8px;
  }

  .approval-summary h3 {
    font-size: 12px;
  }

  .agenda-item {
    gap: 8px;
  }

  .agenda-item__content {
    padding-left: 8px;
  }

  .portal-main {
    gap: 32px;
  }

  .brand-module-list {
    gap: 10px 16px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .login-form-side {
    animation: none;
  }

  .login-field :deep(.el-input__prefix),
  .login-button :deep(.el-icon:not(.is-loading)) {
    transition: none;
  }

  .login-button:hover :deep(.el-icon:not(.is-loading)) {
    transform: none;
  }

  .task-enter-active,
  .task-leave-active,
  .task-move,
  .plan-progress span,
  .timeline-node,
  .approval-timeline li::after,
  .demo-icon-button,
  .demo-dates button,
  .meeting-chevron,
  .login-field :deep(.el-form-item__label),
  .preview-enter-active,
  .preview-leave-active,
  .preview-selector button,
  .login-button,
  .login-field :deep(.el-input__wrapper) {
    transition: none;
  }

  .approval-timeline .is-running .timeline-node,
  .meeting-details[open] .meeting-detail-body {
    animation: none;
  }
}
</style>
