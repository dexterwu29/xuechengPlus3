import request from '../request'
import type { CourseMarketDTO } from '@/types/content'

const BASE = '/courses'

export function getCourseMarket(courseId: number) {
  return request.get<CourseMarketDTO>(`${BASE}/${courseId}/market`)
}

export function saveCourseMarket(courseId: number, data: CourseMarketDTO) {
  return request.put(`${BASE}/${courseId}/market`, data)
}
