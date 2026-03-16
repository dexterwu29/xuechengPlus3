/**
 * 课程审核 API（super_admin）
 */
import request from '../request'
import type { CourseBaseVO, CourseDetailVO, TeachplanTreeVO } from '@/types/content'

const BASE = '/admin/courses'

/** 待审核课程列表（course_status=100） */
export function listPending() {
  return request.get<CourseBaseVO[]>(BASE + '/pending')
}

/** 获取课程详情（用于审核） */
export function getForAudit(id: number) {
  return request.get<CourseDetailVO>(`${BASE}/${id}`)
}

/** 获取课程教学计划树（审核用） */
export function listTeachplansForAudit(id: number) {
  return request.get<TeachplanTreeVO[]>(`${BASE}/${id}/teachplans`)
}

/** 审核课程 */
export function audit(id: number, action: string, opinion?: string) {
  return request.post(`${BASE}/${id}/audit`, { action, opinion })
}
