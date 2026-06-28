import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

/**
 * 路由配置 + 全局路由守卫
 *
 * 守卫逻辑：
 * 1. 访问需要认证的页面 → 检查是否已登录 → 未登录则重定向到 /login
 * 2. 已登录用户访问 /login → 直接重定向到 /home
 */
const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { requiresGuest: true }
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/HomeView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/goal/:id',
    name: 'GoalDetail',
    component: () => import('@/views/GoalDetailView.vue'),
    meta: { requiresAuth: true },
    props: true
  },
  {
    // 404 兜底
    path: '/:pathMatch(.*)*',
    redirect: '/home'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// ==================== 全局路由守卫 ====================
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 需要认证的页面 → 检查登录态
  if (to.meta.requiresAuth) {
    if (!userStore.isLoggedIn) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
    } else {
      next()
    }
  }
  // 仅允许未登录用户访问（如 /login）→ 已登录则跳首页
  else if (to.meta.requiresGuest) {
    if (userStore.isLoggedIn) {
      next({ name: 'Home' })
    } else {
      next()
    }
  }
  // 无限制的页面直接放行
  else {
    next()
  }
})

export default router
