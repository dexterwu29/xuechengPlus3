import request from '../request'
import type { TeachplanDTO, TeachplanTreeVO } from '@/types/content'

const COURSES = '/courses'
const TEACHPLANS = '/teachplans'

export function listTeachplans(courseId: number) {
  return request.get<TeachplanTreeVO[]>(`${COURSES}/${courseId}/teachplans`)
}

export function createTeachplan(courseId: number, data: TeachplanDTO) {
  return request.post<number>(`${COURSES}/${courseId}/teachplans`, data)
}

export function updateTeachplan(id: number, data: TeachplanDTO) {
  return request.put(`${TEACHPLANS}/${id}`, data)
}

export function deleteTeachplan(id: number) {
  return request.delete(`${TEACHPLANS}/${id}`)
}

export function moveTeachplanUp(id: number) {
  return request.post(`${TEACHPLANS}/${id}/move-up`)
}

export function moveTeachplanDown(id: number) {
  return request.post(`${TEACHPLANS}/${id}/move-down`)
}
