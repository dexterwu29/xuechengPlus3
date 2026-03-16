/**
 * 权限组合式函数
 * 基于 authStore 提供角色与权限判断
 */
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { CourseStatus } from '@/types/content'

export function usePermission() {
  const authStore = useAuthStore()

  const role = computed(() => authStore.user?.role ?? '')
  const isSuperAdmin = computed(() => role.value === 'super_admin')
  const isTeacher = computed(() => role.value === 'teacher')
  const isVisitor = computed(() => role.value === 'visitor' || !role.value)

  /** 可编辑课程（teacher、super_admin） */
  const canEditCourse = computed(() => ['teacher', 'super_admin'].includes(role.value))

  /** 可审核课程（仅 super_admin） */
  const canAudit = computed(() => role.value === 'super_admin')

  /** 课程是否可删除（仅草稿 000） */
  function canDeleteCourse(courseStatus: string) {
    return courseStatus === CourseStatus.DRAFT
  }

  /** 课程是否可提交（仅草稿 000，且非 112 已下架） */
  function canSubmitCourse(courseStatus: string) {
    return courseStatus === CourseStatus.DRAFT
  }

  /** 课程是否可发布（仅 super_admin 对 100 待审核） */
  function canPublishCourse(courseStatus: string) {
    return isSuperAdmin.value && courseStatus === CourseStatus.PENDING
  }

  return {
    role,
    isSuperAdmin,
    isTeacher,
    isVisitor,
    canEditCourse,
    canAudit,
    canDeleteCourse,
    canSubmitCourse,
    canPublishCourse,
  }
}
