/**
 * 公开课程 API（无需登录）
 */
import axios from 'axios'
import type { CourseBaseVO, CourseDetailVO, PageResult } from '@/types/content'
import type { TeachplanTreeVO } from '@/types/content'

const request = axios.create({
  baseURL: '/content',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
})

// 公开接口不需要 Token，但需要处理 RestResponse 包装
request.interceptors.response.use(
  (res) => {
    const body = res.data as { code?: number; msg?: string; data?: unknown }
    if (body.code !== 0) {
      return Promise.reject(new Error(body.msg || '请求失败'))
    }
    return body.data as never
  },
  (err) => Promise.reject(err)
)

const BASE = '/public/courses'

export function listPublicCourses(params?: {
  pageNo?: number
  pageSize?: number
  courseName?: string
  mt?: string
  st?: string
}) {
  return request.get<PageResult<CourseBaseVO>>(BASE, { params })
}

export function getPublicCourseDetail(id: number) {
  return request.get<CourseDetailVO>(`${BASE}/${id}`)
}

export function getPublicTeachplans(id: number) {
  return request.get<TeachplanTreeVO[]>(`${BASE}/${id}/teachplans`)
}
