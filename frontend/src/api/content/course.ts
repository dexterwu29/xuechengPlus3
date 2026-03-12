import request from '../request'
import type {
  CoursePageQueryDTO,
  CourseBaseVO,
  CourseDetailVO,
  CourseCreateDTO,
  CourseUpdateDTO,
  PageResult,
} from '@/types/content'

const BASE = '/courses'

export function pageCourses(params: CoursePageQueryDTO) {
  return request.post<PageResult<CourseBaseVO>>(`${BASE}/page`, params)
}

export function getCourse(id: number) {
  return request.get<CourseDetailVO>(`${BASE}/${id}`)
}

export function createCourse(data: CourseCreateDTO) {
  return request.post<number>(BASE, data)
}

export function updateCourse(id: number, data: CourseUpdateDTO) {
  return request.put(`${BASE}/${id}`, data)
}

export function deleteCourse(id: number) {
  return request.delete(`${BASE}/${id}`)
}

export function publishCourse(id: number) {
  return request.post(`${BASE}/${id}/publish`)
}
