import request from '../request'
import type { TeachplanMediaDTO } from '@/types/content'

const BASE = '/teachplans'

export function bindMedia(teachplanId: number, data: TeachplanMediaDTO) {
  return request.post(`${BASE}/${teachplanId}/media`, data)
}

export function unbindMedia(teachplanId: number) {
  return request.delete(`${BASE}/${teachplanId}/media`)
}
