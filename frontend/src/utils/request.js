import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { TOKEN_KEY, USER_KEY, useUserStore } from '@/store/user'

const service = axios.create({
  baseURL: '/api',
  timeout: 10000
})

service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(TOKEN_KEY)
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response) => {
    const result = response.data
    if (result && typeof result.code !== 'undefined' && result.code !== 200) {
      ElMessage.error(result.message || '请求失败')
      return Promise.reject(new Error(result.message || '请求失败'))
    }
    return result
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络异常'
    if (status === 401) {
      const userStore = useUserStore()
      userStore.clearLoginInfo()
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
      ElMessage.warning('登录状态已失效，请重新登录')
      if (router.currentRoute.value.path !== '/login') {
        router.push({
          path: '/login',
          query: { redirect: router.currentRoute.value.fullPath }
        })
      }
    } else {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  }
)

export default service
