import request from '../request'
import type { CourseCategoryTreeVO } from '@/types/content'

const BASE = '/course-categories'

export function getCategoryTree() {
  return request.get<CourseCategoryTreeVO[]>(`${BASE}/tree`)
}
