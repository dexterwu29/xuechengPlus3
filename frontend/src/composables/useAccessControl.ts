/**
 * 访问控制组合式函数
 * 课程/媒资播放权限校验
 */
import { checkPlayAccess } from '@/api/content/media'
import type { MediaAccessResult } from '@/types/content'
import { CourseStatus } from '@/types/content'

/** 课程是否对外可见（仅 111 已发布） */
export function checkCourseAccess(courseStatus: string): boolean {
  return courseStatus === CourseStatus.PUBLISHED
}

/** 教学计划节是否可试看（需后端 is_preview_enabled） */
export function checkTeachplanPreview(teachplan: { isPreviewEnabled?: string }): boolean {
  return teachplan?.isPreviewEnabled === '1'
}

/**
 * 检查媒资播放权限
 * @param fileId 媒资文件 ID
 * @returns 权限结果
 */
export async function checkMediaPlayAccess(fileId: string): Promise<MediaAccessResult> {
  return checkPlayAccess(fileId)
}
