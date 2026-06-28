import request from './request'

/**
 * 目标管理 API
 */
export function createGoal(data) {
  return request.post('/goal/create', data)
}

export function getGoalList() {
  return request.get('/goal/list')
}

export function getGoalDetail(goalId) {
  return request.get(`/goal/${goalId}`)
}
