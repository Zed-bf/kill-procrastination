import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi } from '@/api/user'

/**
 * 用户状态管理（Pinia setup 风格）
 *
 * 职责：
 * 1. 存储 JWT Token 和用户基本信息
 * 2. 提供登录/注册/登出操作
 * 3. 持久化到 localStorage（刷新不丢失登录态）
 * 4. 暴露 isLoggedIn 计算属性供路由守卫使用
 */
export const useUserStore = defineStore('user', () => {
  // ========== State ==========
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(Number(localStorage.getItem('userId')) || null)
  const username = ref(localStorage.getItem('username') || '')
  const nickname = ref(localStorage.getItem('nickname') || '')

  // ========== Getters ==========
  const isLoggedIn = computed(() => !!token.value)

  // ========== Actions ==========

  /**
   * 用户注册
   */
  async function register(form) {
    const res = await registerApi({
      username: form.username,
      password: form.password,
      email: form.email || undefined,
      nickname: form.nickname || form.username
    })
    // 注册成功返回用户信息（不含 token）
    return res
  }

  /**
   * 用户登录
   * 成功后存储 Token 和用户信息
   */
  async function login(form) {
    const res = await loginApi({
      username: form.username,
      password: form.password
    })

    // 持久化存储
    token.value = res.token
    userId.value = res.userId
    username.value = res.username
    nickname.value = res.nickname

    localStorage.setItem('token', res.token)
    localStorage.setItem('userId', res.userId)
    localStorage.setItem('username', res.username)
    localStorage.setItem('nickname', res.nickname)

    return res
  }

  /**
   * 登出 —— 清除所有状态和本地存储
   */
  function logout() {
    token.value = ''
    userId.value = null
    username.value = ''
    nickname.value = ''

    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('nickname')
  }

  return {
    // state
    token,
    userId,
    username,
    nickname,
    // getters
    isLoggedIn,
    // actions
    register,
    login,
    logout
  }
})
