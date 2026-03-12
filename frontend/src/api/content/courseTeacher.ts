import request from '../request'
import type { CourseTeacherDTO, CourseTeacherVO } from '@/types/content'

const BASE = '/courses'

export function listTeachers(courseId: number) {
  return request.get<CourseTeacherVO[]>(`${BASE}/${courseId}/teachers`)
}

export function getTeacher(courseId: number, teacherId: number) {
  return request.get<CourseTeacherVO>(`${BASE}/${courseId}/teachers/${teacherId}`)
}

export function createTeacher(courseId: number, data: CourseTeacherDTO) {
  return request.post<number>(`${BASE}/${courseId}/teachers`, data)
}

export function updateTeacher(courseId: number, teacherId: number, data: CourseTeacherDTO) {
  return request.put(`${BASE}/${courseId}/teachers/${teacherId}`, data)
}

export function deleteTeacher(courseId: number, teacherId: number) {
  return request.delete(`${BASE}/${courseId}/teachers/${teacherId}`)
}
