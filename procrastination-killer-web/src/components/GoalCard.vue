<template>
  <div class="goal-card card" @click="goDetail">
    <div class="goal-card-header">
      <h3 class="goal-title">{{ goal.title }}</h3>
      <el-tag
        :type="goal.status === '已完成' ? 'success' : 'warning'"
        size="small"
        effect="plain"
      >
        {{ goal.status }}
      </el-tag>
    </div>

    <div class="goal-progress">
      <el-progress
        :percentage="goal.progress"
        :color="progressColor"
        :stroke-width="8"
        :striped="goal.status !== '已完成'"
        :striped-flow="goal.status !== '已完成'"
      />
    </div>

    <div class="goal-card-footer">
      <span class="progress-text">
        {{ goal.taskCompleted }} / {{ goal.taskTotal }} 项任务
      </span>
      <span class="created-time">{{ formatDate(goal.createdAt) }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  goal: {
    type: Object,
    required: true
  }
})

const router = useRouter()

const progressColor = computed(() => {
  if (props.goal.progress >= 100) return '#67c23a'
  if (props.goal.progress >= 50) return '#409eff'
  return '#e6a23c'
})

function goDetail() {
  router.push(`/goal/${props.goal.id}`)
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${month}-${day}`
}
</script>

<style scoped>
.goal-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.goal-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0,0,0,0.12);
}

.goal-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.goal-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.5;
  flex: 1;
}

.goal-progress {
  margin-bottom: 12px;
}

.goal-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.progress-text {
  color: var(--text-secondary);
}

.created-time {
  color: #c0c4cc;
}
</style>
