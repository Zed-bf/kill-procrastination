<template>
  <div class="task-list">
    <div
      v-for="task in tasks"
      :key="task.id"
      class="task-item"
      :class="{ completed: task.status === '已完成' }"
    >
      <el-checkbox
        :model-value="task.status === '已完成'"
        :disabled="updatingTaskId === task.id"
        :loading="updatingTaskId === task.id"
        size="large"
        @change="(checked) => handleToggle(task, checked)"
      />
      <div class="task-content">
        <span class="task-text">{{ task.content }}</span>
        <span v-if="task.completedAt" class="task-completed-time">
          完成于 {{ formatDateTime(task.completedAt) }}
        </span>
      </div>
      <span class="task-order">{{ task.sortOrder }}</span>
    </div>

    <el-empty
      v-if="tasks.length === 0"
      description="暂无任务"
      :image-size="80"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { completeTask, undoTask } from '@/api/task'

const props = defineProps({
  tasks: {
    type: Array,
    required: true
  }
})

const emit = defineEmits(['updated'])

const updatingTaskId = ref(null)

async function handleToggle(task, checked) {
  updatingTaskId.value = task.id

  try {
    if (checked) {
      await completeTask(task.id)
      ElMessage.success('任务已完成，进度已同步更新')
    } else {
      await undoTask(task.id)
      ElMessage.success('已撤销完成')
    }
    emit('updated')
  } catch (err) {
    // 错误已在拦截器处理
  } finally {
    updatingTaskId.value = null
  }
}

function formatDateTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hour}:${min}`
}
</script>

<style scoped>
.task-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.task-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 10px;
  background: #fafafa;
  transition: all 0.2s;
}

.task-item:hover {
  background: #f0f2f5;
}

.task-item.completed {
  background: #f0f9eb;
}

.task-item.completed .task-text {
  text-decoration: line-through;
  color: #b3e19d;
}

.task-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.task-text {
  font-size: 15px;
  color: var(--text-primary);
  line-height: 1.6;
  word-break: break-all;
}

.task-completed-time {
  font-size: 12px;
  color: #b3e19d;
}

.task-order {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e6e8eb;
  border-radius: 50%;
  font-size: 12px;
  color: #909399;
  font-weight: 600;
}
</style>
