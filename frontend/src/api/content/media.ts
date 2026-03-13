/**
 * 媒资上传与查询 API
 * 文档：17-课程媒资与发布-后端实现说明.md
 */
import axios from 'axios'
import request from '../request'

const BASE = '/media'

/** 用于 FormData 的请求实例（不统一解包 data，媒体接口返回 Map） */
const uploadRequest = axios.create({
  baseURL: '/content',
  timeout: 60000,
})
uploadRequest.interceptors.request.use((config) => {
  const companyId = localStorage.getItem('companyId') || '1'
  config.headers['X-Company-Id'] = companyId
  const token = localStorage.getItem('token')
  if (token) config.headers['Authorization'] = `Bearer ${token}`
  return config
})

/** 小文件直传（<5MB），返回 fileId */
export function uploadSimple(file: File): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)
  return uploadRequest
    .post<{ fileId?: string }>(`${BASE}/upload`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    .then((res) => {
      const d = res.data
      if (d?.fileId) return d.fileId
      throw new Error((res.data as { msg?: string })?.msg || '上传失败')
    })
}

/** 检查 MD5 是否已存在（媒体接口返回原始 Map，非 RestResponse） */
export function checkByMd5(md5: string): Promise<{ exists: boolean; fileId: string }> {
  return uploadRequest
    .post<{ exists: boolean; fileId: string }>(`${BASE}/upload/check`, null, {
      params: { md5 },
    })
    .then((res) => res.data)
}

/** 初始化分片上传 */
export function initChunkUpload(
  fileMd5: string,
  fileName: string,
  fileSize: number,
  chunkSize?: number
): Promise<string> {
  return uploadRequest
    .post<{ uploadId: string }>(`${BASE}/upload/init`, null, {
      params: { fileMd5, fileName, fileSize, chunkSize: chunkSize ?? 0 },
    })
    .then((res) => res.data?.uploadId ?? '')
}

/** 查询已上传分片 */
export function getUploadStatus(uploadId: string): Promise<number[]> {
  return uploadRequest
    .get<{ uploadedChunks: number[] }>(`${BASE}/upload/status/${uploadId}`)
    .then((res) => res.data?.uploadedChunks ?? [])
}

/** 上传单个分片 */
export function uploadChunk(
  uploadId: string,
  chunkIndex: number,
  chunk: Blob
): Promise<void> {
  const formData = new FormData()
  formData.append('chunk', chunk)
  return uploadRequest.post(`${BASE}/upload/chunk/${uploadId}/${chunkIndex}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }).then(() => {})
}

/** 完成分片上传 */
export function completeChunkUpload(uploadId: string): Promise<string> {
  return uploadRequest
    .post<{ fileId: string }>(`${BASE}/upload/complete/${uploadId}`)
    .then((res) => res.data?.fileId ?? '')
}

/** 获取文件信息（含预签名 URL） */
export interface MediaFileVO {
  fileId: string
  fileName: string
  fileSize: number
  fileType: string
  contentType: string
  url?: string
}

export function getFileInfo(fileId: string): Promise<MediaFileVO> {
  return uploadRequest.get<MediaFileVO>(`${BASE}/${fileId}`).then((res) => res.data)
}
