import request from '../request'
import type { TeachplanMediaDTO, TeachplanMediaVO } from '@/types/content'

const BASE = '/teachplans'

export function bindMedia(teachplanId: number, data: TeachplanMediaDTO) {
  return request.post(`${BASE}/${teachplanId}/media`, data)
}

export function unbindMedia(teachplanId: number) {
  return request.delete(`${BASE}/${teachplanId}/media`)
}

export function unbindMediaByFileId(teachplanId: number, fileId: string) {
  return request.delete(`${BASE}/${teachplanId}/media/${fileId}`)
}

export function listMedia(teachplanId: number) {
  return request.get<TeachplanMediaVO[]>(`${BASE}/${teachplanId}/media`)
}

/**
 * 31.4：更新视频时长
 * @param teachplanId 教学计划ID
 * @param fileId 媒资文件ID
 * @param duration 视频时长（秒）
 */
export function updateMediaDuration(teachplanId: number, fileId: string, duration: number) {
  return request.put(`${BASE}/${teachplanId}/media/${fileId}/duration`, { duration })
}
