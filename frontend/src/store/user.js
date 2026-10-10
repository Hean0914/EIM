import { defineStore } from 'pinia'

const TOKEN_KEY = 'enterprise_token'
const USER_KEY = 'enterprise_user'

export const ROLE_CODES = Object.freeze({
  GENERAL_MANAGER: 'GENERAL_MANAGER',
  HR_MANAGER: 'HR_MANAGER',
  RND_ENGINEER: 'RND_ENGINEER',
  SALES_SPECIALIST: 'SALES_SPECIALIST',
  ACCOUNTANT: 'ACCOUNTANT'
})

export const MANAGEMENT_ROLE_CODES = Object.freeze([
  ROLE_CODES.GENERAL_MANAGER,
  ROLE_CODES.HR_MANAGER
])

export const SELF_SERVICE_ROLE_CODES = Object.freeze([
  ROLE_CODES.RND_ENGINEER,
  ROLE_CODES.SALES_SPECIALIST,
  ROLE_CODES.ACCOUNTANT
])

const ROLE_NAMES = Object.freeze({
  [ROLE_CODES.GENERAL_MANAGER]: '总经理',
  [ROLE_CODES.HR_MANAGER]: '人事经理',
  [ROLE_CODES.RND_ENGINEER]: '研发工程师',
  [ROLE_CODES.SALES_SPECIALIST]: '销售专员',
  [ROLE_CODES.ACCOUNTANT]: '会计'
})

function normalizeRoleCode(roleCode) {
  return String(roleCode || '').trim().toUpperCase()
}

function readJson(key) {
  const value = localStorage.getItem(key)
  if (!value) {
    return null
  }
  try {
    return JSON.parse(value)
  } catch {
    localStorage.removeItem(key)
    return null
  }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: readJson(USER_KEY)
  }),
  getters: {
    isLogin: (state) => Boolean(state.token),
    roleCode: (state) => normalizeRoleCode(state.user?.roleCode),
    roleName: (state) => ROLE_NAMES[normalizeRoleCode(state.user?.roleCode)] || '员工',
    isGeneralManager: (state) => normalizeRoleCode(state.user?.roleCode) === ROLE_CODES.GENERAL_MANAGER,
    isHrManager: (state) => normalizeRoleCode(state.user?.roleCode) === ROLE_CODES.HR_MANAGER,
    isSelfServiceRole: (state) => SELF_SERVICE_ROLE_CODES.includes(normalizeRoleCode(state.user?.roleCode)),
    canManageOrganization: (state) => MANAGEMENT_ROLE_CODES.includes(normalizeRoleCode(state.user?.roleCode)),
    canManageEmployees: (state) => MANAGEMENT_ROLE_CODES.includes(normalizeRoleCode(state.user?.roleCode)),
    canViewAllAttendance: (state) => MANAGEMENT_ROLE_CODES.includes(normalizeRoleCode(state.user?.roleCode)),
    canApprove: (state) => MANAGEMENT_ROLE_CODES.includes(normalizeRoleCode(state.user?.roleCode)),
    canManageOffice: (state) => MANAGEMENT_ROLE_CODES.includes(normalizeRoleCode(state.user?.roleCode)),
    canManageMeetingRooms: (state) => MANAGEMENT_ROLE_CODES.includes(normalizeRoleCode(state.user?.roleCode)),
    canManageAllPlans: (state) => MANAGEMENT_ROLE_CODES.includes(normalizeRoleCode(state.user?.roleCode))
  },
  actions: {
    setLoginInfo(token, user) {
      this.token = token
      this.user = user
      localStorage.setItem(TOKEN_KEY, token)
      localStorage.setItem(USER_KEY, JSON.stringify(user || {}))
    },
    clearLoginInfo() {
      this.token = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    }
  }
})

export { TOKEN_KEY, USER_KEY }
