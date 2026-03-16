/**
 * 课程管理模块 - 类型定义
 * 与后端 xc-plus-content-service 接口保持一致
 */

/** 课程状态（三位码）000草稿/100待审核/111已发布/112已下架 */
export const CourseStatus = {
  DRAFT: '000',
  PENDING: '100',
  PUBLISHED: '111',
  BANNED: '112',
} as const
export type CourseStatusCode = (typeof CourseStatus)[keyof typeof CourseStatus]

/** 审核动作 approve/reject/ban */
export const AuditAction = {
  APPROVE: 'approve',
  REJECT: 'reject',
  BAN: 'ban',
} as const
export type AuditActionType = (typeof AuditAction)[keyof typeof AuditAction]

/** 站内信类型 */
export const MessageType = {
  COURSE_SUBMIT: 'COURSE_SUBMIT',
  AUDIT_APPROVED: 'AUDIT_APPROVED',
  AUDIT_REJECTED: 'AUDIT_REJECTED',
  AUDIT_BANNED: 'AUDIT_BANNED',
} as const

export interface ApiResponse<T> {
  code: number
  msg: string
  data: T | null
}

export interface PageResult<T> {
  items: T[]
  total: number
  pageNo: number
  pageSize: number
}


export interface CourseCategoryTreeVO {
  id: string
  name: string
  label: string
  parentId: string
  isVisible?: number
  orderBy?: number
  isLeaf?: number
  childrenTreeNodes: CourseCategoryTreeVO[]
}

export interface CoursePageQueryDTO {
  pageNo?: number
  pageSize?: number
  courseName?: string
  courseStatus?: string  // 000/100/111/112
  mt?: string  // 大分类ID
  st?: string  // 小分类ID
}

export interface CourseBaseVO {
  id: number
  companyId: number
  companyName: string
  name: string
  users?: string
  tags?: string
  mt?: string
  st?: string
  grade?: string
  teachMode?: string
  description?: string
  pic?: string
  coverPics?: string  // JSON array of "media:fileId"
  defaultCoverIndex?: number
  createTime: string
  updateTime: string
  courseStatus: string  // 000/100/111/112
}

export interface CourseDetailVO extends CourseBaseVO {
  charge?: string
  price?: number
  originalPrice?: number
  qq?: string
  wechat?: string
  phone?: string
  validDays?: number
  /** 封面图URL数组（公开接口返回） */
  coverPicUrls?: string[]
}

export interface CourseCreateDTO {
  name: string
  users?: string
  tags?: string
  mt?: string
  st?: string
  grade?: string
  teachMode?: string
  description?: string
  pic?: string
  coverPics?: string  // JSON array of "media:fileId", max 3
  defaultCoverIndex?: number
}

export interface CourseUpdateDTO extends CourseCreateDTO {}

export interface CourseMarketDTO {
  charge?: string
  price?: number
  originalPrice?: number
  qq?: string
  wechat?: string
  phone?: string
  validDays?: number
}

export interface TeachplanDTO {
  pname: string
  parentId?: number
  grade?: number
  mediaType?: string
  orderBy?: number
  isPreviewEnabled?: string
}

export interface TeachplanTreeVO {
  id: number
  pname: string
  parentId: number
  grade: number
  mediaType?: string
  orderBy?: number
  courseId?: number
  status?: number
  isPreviewEnabled?: string
  children?: TeachplanTreeVO[]
  teachplanMedia?: { mediaId?: string; fileId?: string; mediaFileName?: string }
  mediaList?: TeachplanMediaVO[]
}

export interface TeachplanMediaDTO {
  fileId?: string
  mediaId?: string
  mediaFileName: string
  mediaType?: string
  orderBy?: number
}

export interface TeachplanMediaVO {
  mediaId?: string
  fileId?: string
  mediaFileName?: string
  mediaType?: string
  orderBy?: number
  duration?: number  // 视频时长（秒），后端媒资表可扩展
}

export interface CourseTeacherDTO {
  teacherName: string
  position?: string
  description?: string
  photograph?: string
}

export interface CourseTeacherVO {
  id: number
  courseId: number
  teacherName: string
  position?: string
  description?: string
  photograph?: string
}

/** 站内信 VO */
export interface SystemMessageVO {
  id: number
  type: string
  title: string
  content: string
  courseId?: number
  courseName?: string
  fromUserId?: number
  fromUserName?: string
  toUserId?: number
  toRole?: string
  isRead: boolean
  createTime: string
  readTime?: string
}

/** 媒资播放权限检查结果 */
export interface MediaAccessResult {
  allowed: boolean
  reason?: 'not_logged_in' | 'not_purchased' | 'course_not_published'
  playUrl?: string
}
