import request from '../request'
import type { PageResult } from '@/types/content'

export interface ArchiveVO {
  id: number
  companyId: number
  archiveType: string
  name: string
  phone?: string
  wechat?: string
  qq?: string
  idCard?: string
  licenseNo?: string
  status: string
  createTime: string
  updateTime: string
}

export interface ArchiveDTO {
  name: string
  archiveType?: string
  phone?: string
  wechat?: string
  qq?: string
  idCard?: string
  licenseNo?: string
}

const BASE = '/archives'

export function pageArchives(params: {
  pageNo?: number
  pageSize?: number
  archiveType?: string
  status?: string
}) {
  return request.get<PageResult<ArchiveVO>>(BASE, { params })
}

export function getArchive(id: number) {
  return request.get<ArchiveVO>(`${BASE}/${id}`)
}

export function createArchive(data: ArchiveDTO) {
  return request.post<number>(BASE, data)
}

export function updateArchive(id: number, data: ArchiveDTO) {
  return request.put(`${BASE}/${id}`, data)
}

export function deleteArchive(id: number) {
  return request.delete(`${BASE}/${id}`)
}

export function approveArchive(id: number) {
  return request.post(`${BASE}/${id}/approve`)
}

export function rejectArchive(id: number) {
  return request.post(`${BASE}/${id}/reject`)
}
