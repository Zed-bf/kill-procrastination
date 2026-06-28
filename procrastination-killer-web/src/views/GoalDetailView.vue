<template>
  <AppLayout>
    <div class="detail-page">
      <!-- 返回按钮 -->
      <div class="detail-back">
        <el-button link :icon="ArrowLeft" @click="goBack">返回目标列表</el-button>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="card">
        <el-skeleton :rows="5" animated />
      </div>

      <!-- 目标详情 -->
      <template v-else-if="goal">
        <!-- 目标概览卡片 -->
        <div class="card goal-overview">
          <div class="overview-header">
            <div class="overview-left">
              <h2 class="overview-title">{{ goal.title }}</h2>
              <el-tag
                :type="goal.status === '已完成' ? 'success' : 'warning'"
                size="small"
                effect="plain"
              >
                {{ goal.status }}
              </el-tag>
            </div>
            <div class="overview-progress-circle">
              <el-progress
                type="circle"
                :percentage="goal.progress"
                :width="72"
                :color="progressColor"
                :stroke-width="6"
              >
                <span class="circle-text">{{ goal.progress }}%</span>
              </el-progress>
            </div>
          </div>

          <!-- 目标描述 -->
          <div v-if="goal.description" class="overview-desc">
            <el-icon :size="16" color="#909399"><InfoFilled /></el-icon>
            <span>{{ goal.description }}</span>
          </div>

          <!-- 进度统计 -->
          <div class="overview-stats">
            <div class="stat-item">
              <span class="stat-value">{{ goal.taskCompleted }}</span>
              <span class="stat-label">已完成</span>
            </div>
            <div class="stat-divider" />
            <div class="stat-item">
              <span class="stat-value">{{ goal.taskTotal - goal.taskCompleted }}</span>
              <span class="stat-label">待完成</span>
            </div>
            <div class="stat-divider" />
            <div class="stat-item">
              <span class="stat-value">{{ goal.taskTotal }}</span>
              <span class="stat-label">总任务</span>
            </div>
          </div>
        </div>

        <!-- 任务列表 -->
        <div class="card">
          <div class="task-header">
            <h3 class="task-title">任务清单</h3>
            <span class="task-hint">勾选即完成，自动更新进度</span>
          </div>
          <TaskList :tasks="goal.tasks" @updated="fetchGoalDetail" />
        </div>
      </template>

      <!-- 空状态 -->
      <el-empty v-else description="目标不存在或已被删除" :image-size="100" />
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, InfoFilled } from '@element-plus/icons-vue'
import { getGoalDetail } from '@/api/goal'
import AppLayout from '@/components/AppLayout.vue'
import TaskList from '@/components/TaskList.vue'

const props = defineProps({
  id: {
    type: String,
    required: true
  }
})

const router = useRouter()
const route = useRoute()

// ========== 状态 ==========
const goal = ref(null)
const loading = ref(true)

// ========== 计算属性 ==========
const progressColor = computed(() => {
  if (!goal.value) return '#409eff'
  if (goal.value.progress >= 100) return '#67c23a'
  if (goal.value.progress >= 50) return '#409eff'
  return '#e6a23c'
})

// ========== 生命周期 ==========
onMounted(() => {
  fetchGoalDetail()
})

// 监听路由参数变化（同页面内切换目标时）
watch(() => route.params.id, (newId) => {
  if (newId) {
    fetchGoalDetail()
  }
})

// ========== 方法 ==========
async function fetchGoalDetail() {
  loading.value = true
  try {
    const data = await getGoalDetail(props.id || route.params.id)
    goal.value = data
  } catch (err) {
    goal.value = null
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/home')
}
</script>

<style scoped>
.detail-page {
  /* 受 AppLayout 的 max-width 控制 */
}

.detail-back {
  margin-bottom: 16px;
}

/* ========== 目标概览卡片 ========== */
.goal-overview {
  padding: 24px;
}

.overview-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}

.overview-left {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.overview-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.4;
  word-break: break-all;
}

.overview-progress-circle {
  flex-shrink: 0;
}

.circle-text {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

/* 目标描述 */
.overview-desc {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 16px;
}

/* 进度统计 */
.overview-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  padding: 12px 0;
  border-top: 1px solid #f0f0f0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.stat-divider {
  width: 1px;
  height: 36px;
  background: #e8e8e8;
}

/* ========== 任务区域 ========== */
.task-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 16px;
}

.task-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.task-hint {
  font-size: 12px;
  color: #c0c4cc;
}
</style>
