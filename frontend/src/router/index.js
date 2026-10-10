import { createRouter, createWebHistory } from 'vue-router'
import { MANAGEMENT_ROLE_CODES, ROLE_CODES, useUserStore } from '@/store/user'
import Layout from '@/layout/Layout.vue'
import Login from '@/views/Login.vue'
import Dashboard from '@/views/Dashboard.vue'
import EmployeeIndex from '@/views/employee/index.vue'
import DepartmentIndex from '@/views/department/index.vue'
import PositionIndex from '@/views/position/index.vue'
import RoleIndex from '@/views/role/index.vue'
import AttendanceIndex from '@/views/attendance/index.vue'
import ApprovalIndex from '@/views/approval/index.vue'
import LeaveIndex from '@/views/approval/leave.vue'
import OutIndex from '@/views/approval/out.vue'
import NoticeIndex from '@/views/office/notices.vue'
import MeetingRoomIndex from '@/views/office/meetingRooms.vue'
import MeetingBookingIndex from '@/views/office/meetingBookings.vue'
import MeetingCalendarIndex from '@/views/office/meetingCalendar.vue'
import AddressBookIndex from '@/views/office/addressBook.vue'
import PlanIndex from '@/views/plan/index.vue'
import ProfileIndex from '@/views/profile/index.vue'
import MessageIndex from '@/views/message/index.vue'
import AuditLogIndex from '@/views/audit/index.vue'
import AiAssistantIndex from '@/views/ai-assistant/index.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { public: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { title: '\u5de5\u4f5c\u53f0' }
      },
      {
        path: 'employees',
        name: 'Employees',
        component: EmployeeIndex,
        meta: { title: '\u5458\u5de5\u6863\u6848\u7ba1\u7406', roles: MANAGEMENT_ROLE_CODES }
      },
      {
        path: 'departments',
        name: 'Departments',
        component: DepartmentIndex,
        meta: { title: '部门管理', roles: MANAGEMENT_ROLE_CODES }
      },
      {
        path: 'positions',
        name: 'Positions',
        component: PositionIndex,
        meta: { title: '职位管理', roles: MANAGEMENT_ROLE_CODES }
      },
      {
        path: 'roles',
        name: 'Roles',
        component: RoleIndex,
        meta: { title: '用户与岗位角色管理', roles: [ROLE_CODES.GENERAL_MANAGER] }
      },
      {
        path: 'attendance',
        name: 'Attendance',
        component: AttendanceIndex,
        meta: { title: '考勤管理' }
      },
      {
        path: 'approvals',
        name: 'Approvals',
        component: ApprovalIndex,
        meta: { title: '审批中心' }
      },
      {
        path: 'leaves',
        name: 'Leaves',
        component: LeaveIndex,
        meta: { title: '请假申请' }
      },
      {
        path: 'outs',
        name: 'Outs',
        component: OutIndex,
        meta: { title: '外出登记' }
      },
      {
        path: 'notices',
        name: 'Notices',
        component: NoticeIndex,
        meta: { title: '企业公告' }
      },
      {
        path: 'meeting-rooms',
        name: 'MeetingRooms',
        component: MeetingRoomIndex,
        meta: { title: '会议室管理', roles: MANAGEMENT_ROLE_CODES }
      },
      {
        path: 'meeting-calendar',
        name: 'MeetingCalendar',
        component: MeetingCalendarIndex,
        meta: { title: '会议室日历' }
      },
      {
        path: 'meeting-bookings',
        name: 'MeetingBookings',
        component: MeetingBookingIndex,
        meta: { title: '会议室预定' }
      },
      {
        path: 'address-books',
        name: 'AddressBooks',
        component: AddressBookIndex,
        meta: { title: '通讯录' }
      },
      {
        path: 'plans',
        name: 'Plans',
        component: PlanIndex,
        meta: { title: '计划管理' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: ProfileIndex,
        meta: { title: '个人信息' }
      },
      {
        path: 'messages',
        name: 'Messages',
        component: MessageIndex,
        meta: { title: '统一消息' }
      },
      {
        path: 'ai-assistant',
        name: 'AiAssistant',
        component: AiAssistantIndex,
        meta: { title: '企业办公助手' }
      },
      {
        path: 'audit-logs',
        name: 'AuditLogs',
        component: AuditLogIndex,
        meta: { title: '审计日志', roles: MANAGEMENT_ROLE_CODES }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  if (!to.meta.public && !userStore.isLogin) {
    return {
      path: '/login',
      query: { redirect: to.fullPath }
    }
  }
  if (to.path === '/login' && userStore.isLogin) {
    return '/dashboard'
  }
  const allowedRoles = to.meta.roles
  if (Array.isArray(allowedRoles) && !allowedRoles.includes(userStore.roleCode)) {
    return '/dashboard'
  }
  return true
})

export default router
