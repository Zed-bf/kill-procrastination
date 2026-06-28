import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

/**
 * Axios 实例 —— 统一封装
 * - 请求拦截器：自动添加 JWT Token 到 Header（注册/登录接口除外）
 * - 响应拦截器：统一异常处理 + Token 过期跳转登录
 */

// 无需携带 Token 的公开接口
const PUBLIC_PATHS = ['/user/register', '/user/login']

const request = axios.create({
  baseURL: '/api',
  timeout: 120000
})

// ==================== 请求拦截器 ====================
request.interceptors.request.use(
  (config) => {
    // 公开接口不附加 Token，避免过期 Token 污染注册/登录请求
    const isPublicPath = PUBLIC_PATHS.some(path => config.url?.includes(path))
    if (!isPublicPath) {
      const userStore = useUserStore()
      if (userStore.token) {
        config.headers.Authorization = `Bearer ${userStore.token}`
      }
    }
    return config
  },
  (error) => Promise.reject(error)
)

// ==================== 响应拦截器 ====================
request.interceptors.response.use(
  (response) => {
    const res = response.data

    // 后端直接返回数据（非统一格式）
    if (res.code === undefined) return res

    // 成功
    if (res.code === 200) return res.data

    // 401 / 403 → Token 过期或无效
    if (res.code === 401 || res.code === 403) {
      ElMessage.error('登录已过期，请重新登录')
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
      return Promise.reject(new Error(res.message || '认证失败'))
    }

    // 其他业务错误（如"用户名已被注册"等）
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    if (error.response) {
      const status = error.response.status
      if (status === 401 || status === 403) {
        ElMessage.error('登录已过期，请重新登录')
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      } else if (status >= 500) {
        ElMessage.error('服务器异常，请稍后重试')
      } else {
        // 400 等业务错误，尝试从响应体提取消息
        const msg = error.response.data?.message || error.response.data?.error || '请求失败'
        ElMessage.error(msg)
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请稍后重试')
    } else {
      ElMessage.error('网络异常，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request
