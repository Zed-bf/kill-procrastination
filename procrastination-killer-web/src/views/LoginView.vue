<template>
  <div class="login-page">
    <div class="login-card">
      <!-- 品牌区域 -->
      <div class="brand-section">
        <el-icon :size="40" color="#409eff"><Sunny /></el-icon>
        <h1 class="brand-title">拖延症杀手</h1>
        <p class="brand-desc">把大目标拆成小行动，让拖延无处可逃</p>
      </div>

      <!-- 表单区域 -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleSubmit"
      >
        <el-form-item prop="username" label="用户名">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password" label="密码">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <!-- 注册模式额外字段 -->
        <template v-if="isRegister">
          <el-form-item prop="email" label="邮箱（选填）">
            <el-input
              v-model="form.email"
              placeholder="请输入邮箱"
              :prefix-icon="Message"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="nickname" label="昵称（选填）">
            <el-input
              v-model="form.nickname"
              placeholder="给自己起个名字吧"
              :prefix-icon="UserFilled"
              size="large"
            />
          </el-form-item>
        </template>

        <el-button
          type="primary"
          size="large"
          :loading="loading"
          class="submit-btn"
          native-type="submit"
        >
          {{ isRegister ? '注册' : '登录' }}
        </el-button>
      </el-form>

      <!-- 切换登录/注册 -->
      <div class="switch-mode">
        <span>{{ isRegister ? '已有账号？' : '还没有账号？' }}</span>
        <el-button link type="primary" @click="toggleMode">
          {{ isRegister ? '去登录' : '立即注册' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, UserFilled, Sunny } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// ========== 状态 ==========
const isRegister = ref(false)
const loading = ref(false)
const formRef = ref(null)

const form = reactive({
  username: '',
  password: '',
  email: '',
  nickname: ''
})

// ========== 表单校验规则 ==========
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度需在 3-50 个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度需在 6-100 个字符之间', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

// ========== 生命周期：进入登录页时清除过期登录态 ==========
onMounted(() => {
  // 如果当前有 token 但已经过期（能进入此页面说明路由守卫已判定需要重新登录），
  // 清除旧状态，避免过期 Token 污染注册/登录请求
  if (userStore.token) {
    userStore.logout()
  }
})

// ========== 方法 ==========
function toggleMode() {
  isRegister.value = !isRegister.value
  formRef.value?.resetFields()
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    if (isRegister.value) {
      await userStore.register(form)
      ElMessage.success('注册成功，请登录')
      toggleMode()
    } else {
      await userStore.login(form)
      ElMessage.success('登录成功')
      const redirect = route.query.redirect || '/home'
      router.push(redirect)
    }
  } catch (err) {
    // 错误已在 axios 拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 16px;
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 40px 32px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}

.brand-section {
  text-align: center;
  margin-bottom: 32px;
}

.brand-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 12px 0 8px;
}

.brand-desc {
  font-size: 14px;
  color: var(--text-secondary);
}

.submit-btn {
  width: 100%;
  margin-top: 8px;
}

.switch-mode {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: var(--text-secondary);
}
</style>
