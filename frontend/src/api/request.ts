/**
 * Axios 请求封装
 * 文档要求：统一响应 { code, msg, data }，分页 { items, total, pageNo, pageSize }
 * 除分类树外，所有接口需 X-Company-Id
 */
import axios from 'axios'
import type { ApiResponse } from '@/types/content'
import router from '@/router'

const request = axios.create({
  baseURL: '/content',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
})

// 请求拦截：注入 X-Company-Id、Authorization
request.interceptors.request.use((config) => {
  let companyId = localStorage.getItem('companyId') || '1'
  if (companyId === 'null' || companyId === 'undefined' || !companyId.trim()) companyId = '1'
  config.headers['X-Company-Id'] = companyId
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
})

// 响应拦截：统一处理 code !== 0，401 跳转登录
request.interceptors.response.use(
  (res) => {
    const body = res.data as ApiResponse<unknown>
    const { code, msg, data } = body
    if (code !== 0) {
      return Promise.reject(new Error(msg || '请求失败'))
    }
    return data as never
  },
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('companyId')
      localStorage.removeItem('user')
      const to = router.currentRoute.value
      if (to.path !== '/login') {
        router.replace({ path: '/login', query: { redirect: to.fullPath } })
      }
    }
    const msg = err.response?.data?.msg ?? err.message
    return Promise.reject(new Error(msg))
  }
)

export default request
