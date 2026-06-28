<template>
  <el-dialog
    v-model="visible"
    title="新建目标"
    width="500px"
    :close-on-click-modal="false"
    destroy-on-close
    @closed="handleClosed"
  >
    <!-- 输入阶段 -->
    <template v-if="!generating">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleCreate"
      >
        <el-form-item prop="content" label="请输入你的目标">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="例如：我想在一个月内减重5公斤&#10;例如：我想在两周内学会 Vue 3&#10;越具体越好，AI 会帮你拆解成小任务"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
    </template>

    <!-- 生成中：骨架屏 / 加载动画 -->
    <template v-else>
      <div class="generating-section">
        <div class="generating-icon">
          <el-icon :size="48" class="is-loading" color="#409eff">
            <Loading />
          </el-icon>
        </div>
        <p class="generating-text">AI 正在帮你拆解目标...</p>
        <p class="generating-sub">这可能需要 10-30 秒，请耐心等待</p>

        <!-- 模拟进度条（纯视觉，无实际进度关联） -->
        <el-progress
          :percentage="fakeProgress"
          :show-text="false"
          :stroke-width="4"
          class="generating-bar"
        />
      </div>
    </template>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false" :disabled="generating">取消</el-button>
        <el-button
          type="primary"
          :loading="generating"
          @click="handleCreate"
        >
          {{ generating ? '生成中...' : '开始拆解' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { createGoal } from '@/api/goal'

// ========== Props / Emits ==========
const props = defineProps({
  modelValue: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'created'])

// ========== 状态 ==========
const visible = ref(false)
const generating = ref(false)
const fakeProgress = ref(0)
const formRef = ref(null)
let progressTimer = null

const form = reactive({
  content: ''
})

const rules = {
  content: [
    { required: true, message: '请输入你的目标', trigger: 'blur' },
    { min: 2, max: 200, message: '目标内容长度需在 2-200 个字符之间', trigger: 'blur' }
  ]
}

// ========== 双向绑定 v-model ==========
watch(() => props.modelValue, (val) => {
  visible.value = val
})
watch(visible, (val) => {
  emit('update:modelValue', val)
})

// ========== 方法 ==========

/**
 * 启动假进度条动画（纯视觉反馈）
 */
function startFakeProgress() {
  fakeProgress.value = 0
  progressTimer = setInterval(() => {
    if (fakeProgress.value < 90) {
      fakeProgress.value += Math.random() * 15 + 3
    }
  }, 800)
}

function stopFakeProgress() {
  clearInterval(progressTimer)
  fakeProgress.value = 100
}

async function handleCreate() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  generating.value = true
  startFakeProgress()

  try {
    const result = await createGoal({ content: form.content })
    stopFakeProgress()
    ElMessage.success(`目标已拆解为 ${result.taskTotal} 个小任务！`)
    emit('created', result)
    visible.value = false
  } catch (err) {
    stopFakeProgress()
    // 错误已在拦截器处理
  } finally {
    generating.value = false
    clearInterval(progressTimer)
  }
}

function handleClosed() {
  form.content = ''
  formRef.value?.resetFields()
  generating.value = false
  fakeProgress.value = 0
  clearInterval(progressTimer)
}

onBeforeUnmount(() => {
  clearInterval(progressTimer)
})
</script>

<style scoped>
.generating-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px 0;
}

.generating-icon {
  margin-bottom: 20px;
}

.generating-text {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.generating-sub {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 24px;
}

.generating-bar {
  width: 80%;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
