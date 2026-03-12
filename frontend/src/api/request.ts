/**
 * Axios 请求封装
 * 文档要求：统一响应 { code, msg, data }，分页 { items, total, pageNo, pageSize }
 * 除分类树外，所有接口需 X-Company-Id
 */
import axios from 'axios'
import type { ApiResponse } from '@/types/content'

const request = axios.create({
  baseURL: '/content',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
})

// 请求拦截：注入 X-Company-Id（后续从 Token 解析）
request.interceptors.request.use((config) => {
  const companyId = localStorage.getItem('companyId') || '1'
  config.headers['X-Company-Id'] = companyId
  return config
})

// 响应拦截：统一处理 code !== 0，直接返回 data（业务层拿到的是 data）
request.interceptors.response.use(
  (res) => {
    const body = res.data as ApiResponse<unknown>
    const { code, msg, data } = body
    if (code !== 0) {
      return Promise.reject(new Error(msg || '请求失败'))
    }
    return data as never
  },
  (err) => Promise.reject(err)
)

export default request
