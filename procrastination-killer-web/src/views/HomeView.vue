<template>
  <AppLayout>
    <div class="home-page">
      <!-- 页面头部 -->
      <div class="home-header">
        <div>
          <h2 class="home-title">我的目标</h2>
          <p class="home-sub">每一步都算数，行动起来</p>
        </div>
        <el-button type="primary" :icon="Plus" size="large" @click="showCreateDialog = true">
          新建目标
        </el-button>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-section">
        <el-skeleton :rows="3" animated />
      </div>

      <!-- 空状态 -->
      <el-empty
        v-else-if="goals.length === 0"
        description="还没有目标，点击上方按钮创建第一个吧！"
        :image-size="120"
      >
        <el-button type="primary" @click="showCreateDialog = true">创建我的第一个目标</el-button>
      </el-empty>

      <!-- 目标列表 -->
      <template v-else>
        <!-- 进行中的目标 -->
        <div v-if="activeGoals.length > 0" class="section">
          <div class="section-header">
            <span class="section-title">进行中</span>
            <span class="section-count">{{ activeGoals.length }}</span>
          </div>
          <GoalCard
            v-for="goal in activeGoals"
            :key="goal.id"
            :goal="goal"
          />
        </div>

        <!-- 已完成的目标 -->
        <div v-if="completedGoals.length > 0" class="section">
          <div class="section-header">
            <span class="section-title">已完成</span>
            <span class="section-count completed-count">{{ completedGoals.length }}</span>
          </div>
          <GoalCard
            v-for="goal in completedGoals"
            :key="goal.id"
            :goal="goal"
          />
        </div>
      </template>

      <!-- 新建目标对话框 -->
      <CreateGoalDialog
        v-model="showCreateDialog"
        @created="handleGoalCreated"
      />
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { getGoalList } from '@/api/goal'
import AppLayout from '@/components/AppLayout.vue'
import GoalCard from '@/components/GoalCard.vue'
import CreateGoalDialog from '@/components/CreateGoalDialog.vue'

// ========== 状态 ==========
const goals = ref([])
const loading = ref(true)
const showCreateDialog = ref(false)

// ========== 计算属性 ==========
const activeGoals = computed(() => goals.value.filter(g => g.status === '进行中'))
const completedGoals = computed(() => goals.value.filter(g => g.status === '已完成'))

// ========== 生命周期 ==========
onMounted(() => {
  fetchGoals()
})

// ========== 方法 ==========
async function fetchGoals() {
  loading.value = true
  try {
    const data = await getGoalList()
    goals.value = data || []
  } catch (err) {
    goals.value = []
  } finally {
    loading.value = false
  }
}

function handleGoalCreated() {
  fetchGoals()
}
</script>

<style scoped>
.home-page {
  /* 受 AppLayout 的 max-width 控制 */
}

.home-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 28px;
}

.home-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
}

.home-sub {
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.loading-section {
  padding: 16px 0;
}

.section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-secondary);
}

.section-count {
  background: #e6f0ff;
  color: #409eff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 600;
}

.section-count.completed-count {
  background: #e8f5e9;
  color: #67c23a;
}
</style>
