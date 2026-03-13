/**
 * 认证 API
 */
import request from './request'

export interface LoginVO {
  token: string
  userId: number
  username: string
  realName: string
  role: string
  companyId: number
}

export function login(username: string, password: string): Promise<LoginVO> {
  return request.post<LoginVO>('/auth/login', null, {
    params: { username, password },
  })
}
