import request from './request'

/**
 * 用户认证 API
 */
export function register(data) {
  return request.post('/user/register', data)
}

export function login(data) {
  return request.post('/user/login', data)
}
