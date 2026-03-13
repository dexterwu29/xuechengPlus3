/**
 * 课程管理模块 - 类型定义
 * 与后端 xc-plus-content-service 接口保持一致
 */

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
  auditStatus?: string
  publishStatus?: string
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
  createTime: string
  updateTime: string
  auditStatus: string
  publishStatus: string
}

export interface CourseDetailVO extends CourseBaseVO {
  charge?: string
  price?: number
  originalPrice?: number
  qq?: string
  wechat?: string
  phone?: string
  validDays?: number
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
