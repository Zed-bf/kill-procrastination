import request from './request'

/**
 * 任务管理 API
 */
export function completeTask(taskId) {
  return request.put(`/task/${taskId}/complete`)
}

export function undoTask(taskId) {
  return request.put(`/task/${taskId}/undo`)
}
