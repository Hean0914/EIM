<template>
  <el-container class="app-layout">
    <el-aside
      :width="asideWidth"
      class="app-aside"
      :class="{ 'is-collapsed': asideCollapsed, 'is-mobile-open': mobileMenuOpen }"
    >
      <div class="app-logo">
        <div class="app-logo__mark">E</div>
        <div v-if="!asideCollapsed">
          <strong>&#x4f01;&#x4e1a;&#x4fe1;&#x606f;&#x7ba1;&#x7406;</strong>
          <span>Enterprise Office</span>
        </div>
      </div>

      <el-menu
        router
        :collapse="asideCollapsed"
        :default-active="activePath"
        :default-openeds="openMenuKeys"
        class="app-menu"
        @select="handleMenuSelect"
      >
        <template v-if="!asideCollapsed">
          <div class="menu-section-label">工作空间</div>
        </template>
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>工作台</span>
        </el-menu-item>

        <template v-if="!asideCollapsed">
          <div class="menu-section-label">组织与流程</div>
        </template>
        <el-sub-menu v-if="canManageOrganization" index="organization-menu">
          <template #title>
            <el-icon><OfficeBuilding /></el-icon>
            <span>组织架构</span>
          </template>
          <el-menu-item index="/departments">部门管理</el-menu-item>
          <el-menu-item index="/positions">职位管理</el-menu-item>
          <el-menu-item v-if="canManageRoles" index="/roles">用户与岗位角色</el-menu-item>
        </el-sub-menu>
        <el-menu-item v-if="canManageEmployees" index="/employees">
          <el-icon><UserFilled /></el-icon>
          <span>员工档案</span>
        </el-menu-item>
        <el-sub-menu index="approval-menu">
          <template #title>
            <el-icon><Checked /></el-icon>
            <span>审批流程</span>
          </template>
          <el-menu-item index="/approvals">审批中心</el-menu-item>
          <el-menu-item index="/leaves">请假申请</el-menu-item>
          <el-menu-item index="/outs">外出登记</el-menu-item>
        </el-sub-menu>

        <template v-if="!asideCollapsed">
          <div class="menu-section-label">办公协作</div>
        </template>
        <el-menu-item index="/attendance">
          <el-icon><Clock /></el-icon>
          <span>考勤管理</span>
        </el-menu-item>
        <el-sub-menu index="office-menu">
          <template #title>
            <el-icon><Calendar /></el-icon>
            <span>日常办公</span>
          </template>
          <el-menu-item index="/notices">企业公告</el-menu-item>
          <el-menu-item v-if="canManageMeetingRooms" index="/meeting-rooms">会议室管理</el-menu-item>
          <el-menu-item index="/meeting-calendar">会议室日历</el-menu-item>
          <el-menu-item index="/meeting-bookings">会议室预定</el-menu-item>
          <el-menu-item index="/address-books">通讯录</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/plans">
          <el-icon><List /></el-icon>
          <span>计划管理</span>
        </el-menu-item>
        <el-menu-item index="/messages">
          <el-icon><Bell /></el-icon>
          <span>统一消息</span>
        </el-menu-item>

        <template v-if="!asideCollapsed">
          <div class="menu-section-label">系统管理</div>
        </template>
        <el-menu-item index="/ai-assistant">
          <el-icon><Cpu /></el-icon>
          <span>企业办公助手</span>
        </el-menu-item>
        <el-menu-item v-if="canViewAuditLogs" index="/audit-logs">
          <el-icon><DocumentChecked /></el-icon>
          <span>审计日志</span>
        </el-menu-item>
      </el-menu>

      <div class="aside-footer">
        <div class="aside-health">
          <span class="aside-health__dot"></span>
          <span v-if="!asideCollapsed">系统运行正常</span>
        </div>
        <div v-if="!asideCollapsed" class="aside-account">
          <el-avatar :size="28" :src="userAvatar" fit="cover" @error="handleAvatarError">{{ userInitial }}</el-avatar>
          <div>
            <strong>{{ userStore.user?.realName || userStore.user?.username || defaultUserName }}</strong>
            <span>{{ userStore.roleName }}</span>
          </div>
        </div>
      </div>
    </el-aside>

    <button
      v-if="mobileMenuOpen"
      class="mobile-menu-backdrop"
      type="button"
      aria-label="关闭导航菜单"
      @click="mobileMenuOpen = false"
    ></button>

    <el-container>
      <el-header class="app-header">
        <div class="header-title">
          <button
            class="sidebar-toggle"
            type="button"
            :aria-label="sidebarToggleLabel"
            :title="sidebarToggleLabel"
            @click="toggleSidebar"
          >
            <el-icon><Menu v-if="isMobileViewport" /><Expand v-else-if="asideCollapsed" /><Fold v-else /></el-icon>
          </button>
          <div>
          <h2>{{ route.meta.title || defaultTitle }}</h2>
          <span>&#x524d;&#x540e;&#x7aef;&#x5206;&#x79bb;&#x540e;&#x53f0;&#x7ba1;&#x7406;&#x5e73;&#x53f0;</span>
          </div>
        </div>
        <div class="header-actions">
          <el-badge :value="unreadCount" :hidden="!unreadCount" :max="99" class="message-badge">
            <button class="icon-entry" type="button" title="统一消息" @click="router.push('/messages')">
              <el-icon><Bell /></el-icon>
            </button>
          </el-badge>
          <el-dropdown @command="handleCommand">
          <button class="user-entry" type="button">
            <el-avatar :size="32" :src="userAvatar" fit="cover" @error="handleAvatarError">{{ userInitial }}</el-avatar>
            <span>{{ userStore.user?.realName || userStore.user?.username || defaultUserName }}</span>
            <em>{{ userStore.roleName }}</em>
            <el-icon><ArrowDown /></el-icon>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">&#x4e2a;&#x4eba;&#x4fe1;&#x606f;</el-dropdown-item>
              <el-dropdown-item divided command="logout">&#x9000;&#x51fa;&#x767b;&#x5f55;</el-dropdown-item>
            </el-dropdown-menu>
          </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  ArrowDown,
  Bell,
  Calendar,
  Checked,
  Clock,
  Cpu,
  Expand,
  Fold,
  List,
  Menu,
  Odometer,
  OfficeBuilding,
  UserFilled,
  DocumentChecked
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getMessageSummary, listMessagePage } from '@/api/message'
import {
  createMessageNotifier,
  dispatchMessageUpdated,
  MESSAGE_UPDATED_EVENT,
  showBrowserNotification
} from '@/utils/message-notifier'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const defaultTitle = '\u4f01\u4e1a\u4fe1\u606f\u7ba1\u7406\u7cfb\u7edf'
const defaultUserName = '\u7528\u6237'

const activePath = computed(() => route.path)
const asideCollapsed = ref(false)
const mobileMenuOpen = ref(false)
const asideWidth = computed(() => (asideCollapsed.value ? '72px' : '232px'))
const canManageOrganization = computed(() => userStore.canManageOrganization)
const canManageEmployees = computed(() => userStore.canManageEmployees)
const canManageMeetingRooms = computed(() => userStore.canManageMeetingRooms)
const canManageRoles = computed(() => userStore.isGeneralManager)
const canViewAuditLogs = computed(() => userStore.canManagePeople)
const userInitial = computed(() => (userStore.user?.realName || userStore.user?.username || defaultUserName).slice(0, 1))
const avatarFailed = ref(false)
const userAvatar = computed(() => (avatarFailed.value ? '' : userStore.user?.avatar || ''))
const unreadCount = ref(0)
const messageNotifier = ref(null)
const isMobileViewport = ref(false)
const sidebarToggleLabel = computed(() => {
  if (isMobileViewport.value) {
    return mobileMenuOpen.value ? '关闭导航菜单' : '打开导航菜单'
  }
  return asideCollapsed.value ? '展开导航菜单' : '收起导航菜单'
})
const openMenuKeys = computed(() => {
  const path = route.path
  const keys = []
  if (['/departments', '/positions', '/roles'].includes(path)) {
    keys.push('organization-menu')
  }
  if (['/approvals', '/leaves', '/outs'].includes(path)) {
    keys.push('approval-menu')
  }
  if (['/notices', '/meeting-rooms', '/meeting-calendar', '/meeting-bookings', '/address-books'].includes(path)) {
    keys.push('office-menu')
  }
  return keys
})

watch(() => userStore.user?.avatar, () => {
  avatarFailed.value = false
})
watch(() => route.path, () => {
  mobileMenuOpen.value = false
})

// 头像地址失效时回退为姓名首字母
function handleAvatarError() {
  avatarFailed.value = true
}

onMounted(() => {
  updateViewportState()
  window.addEventListener('resize', updateViewportState)
  startMessageNotifier()
})
onBeforeUnmount(stopMessageNotifier)
onBeforeUnmount(() => {
  window.removeEventListener('resize', updateViewportState)
})

function updateViewportState() {
  isMobileViewport.value = window.matchMedia('(max-width: 1023px)').matches
  if (!isMobileViewport.value) {
    mobileMenuOpen.value = false
  }
}

function toggleSidebar() {
  if (isMobileViewport.value) {
    mobileMenuOpen.value = !mobileMenuOpen.value
    return
  }
  asideCollapsed.value = !asideCollapsed.value
}

function handleMenuSelect() {
  if (isMobileViewport.value) {
    mobileMenuOpen.value = false
  }
}

async function fetchMessageSnapshot() {
  const [summaryResult, pageResult] = await Promise.all([
    getMessageSummary(),
    listMessagePage({ current: 1, size: 50, readStatus: 0 })
  ])
  return {
    unreadCount: Number(summaryResult.data?.unread || 0),
    messages: pageResult.data?.records || []
  }
}

function shouldShowBrowserNotification() {
  return route.path !== '/messages' || document.visibilityState === 'hidden'
}

function handleMessageUpdated(event) {
  if (event?.detail?.source === 'poll') {
    return
  }
  void messageNotifier.value?.refresh()
}

function handleVisibilityChange() {
  if (document.visibilityState === 'visible') {
    void messageNotifier.value?.refresh()
  }
}

function startMessageNotifier() {
  const userId = userStore.user?.userId || userStore.user?.id || 'current'
  messageNotifier.value = createMessageNotifier({
    userId,
    fetchSnapshot: fetchMessageSnapshot,
    shouldNotify: shouldShowBrowserNotification,
    showNotification: (message) => showBrowserNotification(message, {
      onClick: (clickedMessage) => router.push(clickedMessage?.targetPath || '/messages')
    }),
    onNewMessages: (messages) => {
      dispatchMessageUpdated({ source: 'poll', messages })
    },
    onUnreadCountChange: (nextUnreadCount) => {
      unreadCount.value = Number(nextUnreadCount || 0)
    }
  })
  window.addEventListener(MESSAGE_UPDATED_EVENT, handleMessageUpdated)
  document.addEventListener('visibilitychange', handleVisibilityChange)
  void messageNotifier.value.start()
}

function stopMessageNotifier() {
  messageNotifier.value?.stop()
  window.removeEventListener(MESSAGE_UPDATED_EVENT, handleMessageUpdated)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  messageNotifier.value = null
}

async function handleCommand(command) {
  if (command === 'profile') {
    router.push('/profile')
    return
  }
  if (command !== 'logout') {
    return
  }
  await ElMessageBox.confirm('\u786e\u8ba4\u9000\u51fa\u5f53\u524d\u8d26\u53f7\uff1f', '\u9000\u51fa\u767b\u5f55', {
    type: 'warning',
    confirmButtonText: '\u9000\u51fa',
    cancelButtonText: '\u53d6\u6d88'
  })
  stopMessageNotifier()
  userStore.clearLoginInfo()
  router.replace('/login')
}
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  color: var(--gray-800);
  background: var(--gray-50);
}

.app-aside {
  color: #dbe4f4;
  background: var(--color-navigation-bg);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
}

.app-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 64px;
  padding: 0 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.app-logo__mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  color: #fff;
  background: var(--color-brand);
  border-radius: 8px;
  font-weight: 800;
}

.app-logo strong,
.app-logo span {
  display: block;
}

.app-logo strong {
  color: #fff;
  font-size: 15px;
}

.app-logo span {
  margin-top: 2px;
  color: #aab7cf;
  font-size: 12px;
}

.app-menu {
  border-right: 0;
  background: transparent;
}

.app-menu :deep(.el-menu) {
  background: #0f172a;
}

.app-menu :deep(.el-menu-item),
.app-menu :deep(.el-sub-menu__title) {
  color: #cbd5e1;
  height: 42px;
  margin: 4px 10px;
  border-radius: 6px;
  line-height: 42px;
  transition: background-color 0.16s ease, color 0.16s ease;
}

.app-menu :deep(.el-sub-menu .el-menu-item) {
  color: #dbeafe;
  min-width: 0;
  height: 38px;
  margin: 3px 10px 3px 36px;
  padding-left: 14px !important;
  background: transparent;
  border-radius: 6px;
  line-height: 38px;
}

.app-menu :deep(.el-menu-item.is-active) {
  color: #fff;
  background: var(--color-brand);
}

.app-menu :deep(.el-sub-menu .el-menu-item.is-active) {
  color: #fff;
  background: var(--color-brand);
}

.app-menu :deep(.el-menu-item:hover),
.app-menu :deep(.el-sub-menu__title:hover) {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

.app-menu :deep(.el-sub-menu .el-menu-item:hover) {
  color: #fff;
  background: rgba(59, 130, 246, 0.18);
}

.app-menu :deep(.el-menu-item:active),
.app-menu :deep(.el-sub-menu__title:active) {
  background: rgba(255, 255, 255, 0.14);
}

.app-menu :deep(.el-sub-menu .el-menu-item:active) {
  background: rgba(59, 130, 246, 0.26);
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid var(--gray-200);
}

.app-header h2 {
  margin: 0;
  color: var(--gray-900);
  font-size: 18px;
  font-weight: 650;
  line-height: 1.35;
}

.app-header span {
  display: block;
  margin-top: 4px;
  color: var(--gray-600);
  font-size: 12px;
}

.user-entry {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 40px;
  padding: 0 10px;
  color: var(--gray-800);
  cursor: pointer;
  background: var(--gray-50);
  border: 1px solid var(--gray-200);
  border-radius: 8px;
  transition: background-color 0.16s ease, border-color 0.16s ease;
}

.user-entry:hover {
  background: #fff;
  border-color: var(--gray-300);
}

.user-entry:active {
  background: var(--gray-100);
}

.user-entry em {
  display: inline-flex;
  align-items: center;
  height: 20px;
  padding: 0 6px;
  color: var(--color-brand);
  background: #eef2ff;
  border: 1px solid #c7d2fe;
  border-radius: 4px;
  font-size: 12px;
  font-style: normal;
  line-height: 20px;
}

.header-actions {
  display: inline-flex;
  align-items: center;
  gap: 12px;
}

.icon-entry {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  color: var(--gray-700);
  cursor: pointer;
  background: #fff;
  border: 1px solid var(--gray-200);
  border-radius: 8px;
}

.icon-entry:hover {
  color: var(--color-brand);
  border-color: #c7d2fe;
  background: #eef2ff;
}

.message-badge :deep(.el-badge__content) {
  top: 2px;
  right: 2px;
}

.app-main {
  min-height: calc(100vh - 64px);
  padding: 24px;
  background: var(--gray-50);
}

/* Login-page-aligned navigation treatment */
.app-aside {
  position: relative;
  display: flex;
  flex: 0 0 auto;
  flex-direction: column;
  min-height: 100vh;
  overflow: hidden;
  background: var(--color-navigation-bg);
  transition: width 0.2s ease, transform 0.2s ease;
}

.app-logo {
  flex: 0 0 72px;
  height: 72px;
  padding: 0 18px;
  border-bottom-color: rgba(255, 255, 255, 0.1);
}

.app-aside.is-collapsed .app-logo {
  justify-content: center;
  padding: 0;
}

.app-logo__mark {
  flex: 0 0 36px;
  width: 36px;
  height: 36px;
  background: var(--color-brand);
  border-radius: 8px;
}

.app-menu {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  padding: 12px 10px;
  background: transparent;
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.18) transparent;
}

.app-menu::-webkit-scrollbar {
  width: 5px;
}

.app-menu::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.18);
  border-radius: 4px;
}

.app-menu :deep(.el-menu) {
  background: transparent;
}

.menu-section-label {
  padding: 16px 12px 6px;
  color: #71809b;
  font-size: 10px;
  font-weight: 650;
  letter-spacing: 0.08em;
  line-height: 1.4;
}

.menu-section-label:first-child {
  padding-top: 4px;
}

.app-menu :deep(.el-menu-item),
.app-menu :deep(.el-sub-menu__title) {
  position: relative;
  height: 40px;
  margin: 3px 0;
  padding: 0 12px !important;
  color: #cbd5e1;
  border-radius: 6px;
  line-height: 40px;
  transition: background-color 0.16s ease, color 0.16s ease;
}

.app-menu :deep(.el-menu-item .el-icon),
.app-menu :deep(.el-sub-menu__title .el-icon) {
  color: #8f9db6;
  font-size: 16px;
  transition: color 0.16s ease;
}

.app-menu :deep(.el-menu-item:hover),
.app-menu :deep(.el-sub-menu__title:hover) {
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
}

.app-menu :deep(.el-menu-item:hover .el-icon),
.app-menu :deep(.el-sub-menu__title:hover .el-icon) {
  color: #c7d2fe;
}

.app-menu :deep(.el-menu-item.is-active) {
  color: #fff;
  background: var(--color-brand);
}

.app-menu :deep(.el-menu-item.is-active)::before {
  position: absolute;
  top: 8px;
  bottom: 8px;
  left: 0;
  width: 3px;
  content: "";
  background: #c7d2fe;
  border-radius: 0 3px 3px 0;
}

.app-menu :deep(.el-menu-item.is-active .el-icon) {
  color: #fff;
}

.app-menu :deep(.el-sub-menu.is-opened > .el-sub-menu__title) {
  color: #fff;
  background: rgba(255, 255, 255, 0.06);
}

.app-menu :deep(.el-sub-menu.is-opened > .el-sub-menu__title .el-icon) {
  color: #c7d2fe;
}

.app-menu :deep(.el-sub-menu .el-menu-item) {
  min-width: 0;
  height: 36px;
  margin: 2px 0 2px 14px;
  padding-left: 38px !important;
  color: #aab7cf;
  background: transparent;
  border-radius: 5px;
  line-height: 36px;
}

.app-menu :deep(.el-sub-menu .el-menu-item.is-active) {
  color: #fff;
  background: var(--color-brand);
}

.app-menu :deep(.el-menu--collapse) {
  padding-right: 0;
  padding-left: 0;
}

.app-menu :deep(.el-menu--collapse > .el-menu-item),
.app-menu :deep(.el-menu--collapse > .el-sub-menu > .el-sub-menu__title) {
  justify-content: center;
  padding: 0 !important;
}

.app-menu :deep(.el-menu--collapse > .el-menu-item .el-icon),
.app-menu :deep(.el-menu--collapse > .el-sub-menu > .el-sub-menu__title .el-icon) {
  margin: 0;
}

.aside-footer {
  flex: 0 0 auto;
  padding: 12px 16px 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.app-aside.is-collapsed .aside-footer {
  padding-right: 0;
  padding-left: 0;
}

.aside-health {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #8f9db6;
  font-size: 11px;
}

.app-aside.is-collapsed .aside-health {
  justify-content: center;
}

.aside-health__dot {
  width: 7px;
  height: 7px;
  background: #34d399;
  border-radius: 50%;
  box-shadow: 0 0 0 4px rgba(52, 211, 153, 0.1);
}

.aside-account {
  display: flex;
  align-items: center;
  gap: 9px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.aside-account strong,
.aside-account span {
  display: block;
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.aside-account strong {
  color: #f8fafc;
  font-size: 12px;
  font-weight: 500;
}

.aside-account span {
  margin-top: 2px;
  color: #71809b;
  font-size: 11px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.sidebar-toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  color: var(--gray-600);
  cursor: pointer;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 6px;
  transition: background-color 0.16s ease, color 0.16s ease, border-color 0.16s ease;
}

.sidebar-toggle:hover,
.sidebar-toggle:focus-visible {
  color: var(--color-brand);
  background: #eef2ff;
  border-color: #c7d2fe;
}

.mobile-menu-backdrop {
  position: fixed;
  inset: 0;
  z-index: 20;
  padding: 0;
  cursor: default;
  background: rgba(17, 24, 39, 0.4);
  border: 0;
}

@media (max-width: 1023px) {
  .app-aside {
    position: fixed;
    inset: 0 auto 0 0;
    z-index: 30;
    width: 232px !important;
    transform: translateX(-100%);
    box-shadow: 12px 0 28px rgba(17, 24, 39, 0.16);
  }

  .app-aside.is-mobile-open {
    transform: translateX(0);
  }

  .app-layout > .el-container {
    width: 100%;
  }

  .app-header {
    padding: 0 16px;
  }

  .app-header > .header-actions {
    gap: 6px;
  }

  .user-entry > span,
  .user-entry > em {
    display: none;
  }

  .app-main {
    padding: 16px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .app-aside,
  .app-menu :deep(.el-menu-item),
  .app-menu :deep(.el-sub-menu__title),
  .app-menu :deep(.el-menu-item .el-icon),
  .app-menu :deep(.el-sub-menu__title .el-icon),
  .sidebar-toggle {
    transition: none;
  }
}
</style>
