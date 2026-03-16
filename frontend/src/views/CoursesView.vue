<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePermission } from '@/composables/usePermission'
import { pageCourses, deleteCourse, publishCourse } from '@/api/content/course'
import { getCategoryTree } from '@/api/content/category'
import { getFileInfo } from '@/api/content/media'
import type { CourseBaseVO, CourseCategoryTreeVO } from '@/types/content'
import { CourseStatus } from '@/types/content'
import StatusBadge from '@/components/StatusBadge.vue'

const route = useRoute()
const router = useRouter()
const { isSuperAdmin } = usePermission()
const courses = ref<CourseBaseVO[]>([])
const coursePicUrls = ref<Map<number, string>>(new Map())
const total = ref(0)
const pageNo = ref(1)
const pageSize = ref(12)
const loading = ref(false)
const categoryTree = ref<CourseCategoryTreeVO[]>([])

// 搜索条件（与 route.query 同步，支持 mt 分类筛选）
const searchCourseName = ref('')
const searchCourseStatus = ref('')

const COURSE_STATUS_OPTIONS = [
  { code: '', desc: '全部' },
  { code: CourseStatus.DRAFT, desc: '草稿' },
  { code: CourseStatus.PENDING, desc: '待审核' },
  { code: CourseStatus.PUBLISHED, desc: '已发布' },
  { code: CourseStatus.BANNED, desc: '已下架' },
]

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

/**
 * 将 media:fileId 格式转换为实际URL
 */
function extractFileId(pic: string): string | null {
  if (!pic || !pic.startsWith('media:')) return null
  return pic.slice(6) // 移除 "media:" 前缀
}

/**
 * 获取课程封面图片URL
 */
async function getCoursePicUrl(course: CourseBaseVO): Promise<string> {
  if (!course.pic) return ''
  // 如果已经是完整URL（http开头），直接返回
  if (course.pic.startsWith('http')) return course.pic

  // 如果是 media:fileId 格式，获取实际URL
  const fileId = extractFileId(course.pic)
  if (!fileId) return course.pic

  try {
    const info = await getFileInfo(fileId)
    return info?.url || course.pic
  } catch {
    return course.pic
  }
}

/**
 * 批量加载课程封面URL
 */
async function loadCoursePicUrls() {
  const urlPromises = courses.value.map(async (c) => {
    const url = await getCoursePicUrl(c)
    return { id: c.id, url }
  })

  const results = await Promise.all(urlPromises)
  const newMap = new Map<number, string>()
  results.forEach(r => newMap.set(r.id!, r.url))
  coursePicUrls.value = newMap
}

async function loadCourses() {
  loading.value = true
  try {
    const mt = (route.query.mt as string) || undefined
    const res = await pageCourses({
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      courseName: searchCourseName.value || undefined,
      courseStatus: searchCourseStatus.value || undefined,
      mt,
    })
    courses.value = res?.items ?? []
    total.value = Number(res?.total ?? 0)
    // 加载完成后，批量获取封面URL
    if (courses.value.length > 0) {
      await loadCoursePicUrls()
    }
  } catch {
    courses.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    categoryTree.value = (await getCategoryTree()) ?? []
  } catch {
    categoryTree.value = []
  }
}

function doSearch() {
  pageNo.value = 1
  loadCourses()
}

function goAdd() {
  router.push('/courses/add')
}

function goEdit(id: number) {
  router.push(`/courses/${id}/edit`)
}

async function handleDelete(c: CourseBaseVO) {
  if (!c.id || !confirm(`确定删除课程「${c.name}」吗？`)) return
  try {
    await deleteCourse(c.id)
    await loadCourses()
  } catch (e) {
    alert((e as Error)?.message || '删除失败')
  }
}

async function handlePublish(c: CourseBaseVO) {
  if (!c.id || !confirm('课程发布后将在网站公开，是否继续？')) return
  try {
    await publishCourse(c.id)
    await loadCourses()
  } catch (e) {
    alert((e as Error)?.message || '发布失败')
  }
}

onMounted(() => {
  loadCategories()
  loadCourses()
})

watch(
  () => [route.query.mt, pageNo.value],
  () => loadCourses()
)
</script>

<template>
  <div class="courses-page">
    <div class="container">
      <div class="header">
        <h1 class="page-title">
          <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/><path d="M12 11h4"/><path d="M12 7h4"/></svg>
          课程管理
        </h1>
        <button class="btn-add" @click="goAdd">
          <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14"/><path d="M5 12h14"/></svg>
          添加课程
        </button>
      </div>

      <!-- 搜索栏：一行水平对齐，Ant Design Vue 风格 -->
      <div class="search-bar">
        <div class="search-input-wrap">
          <span class="search-icon" aria-hidden="true">
            <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
          </span>
          <input
            v-model="searchCourseName"
            class="search-input"
            placeholder="请输入课程名称搜索"
            @keyup.enter="doSearch"
          />
        </div>
        <span class="filter-icon" aria-hidden="true">
          <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/></svg>
        </span>
        <div class="filter-item">
          <span class="filter-label-inline">课程状态</span>
          <select v-model="searchCourseStatus" class="search-select">
            <option v-for="o in COURSE_STATUS_OPTIONS" :key="o.code" :value="o.code">{{ o.desc }}</option>
          </select>
        </div>
        <button class="btn-search" @click="doSearch">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
          搜索
        </button>
      </div>

      <!-- 分类侧边栏 + 主内容 -->
      <div class="main">
        <div v-if="categoryTree.length" class="sidebar">
          <h3>
            <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><path d="M22 6-12 12 2 6"/></svg>
            课程分类
          </h3>
          <RouterLink
            :to="`/manage/courses`"
            class="cat-link"
            :class="{ active: !route.query.mt }"
          >
            全部
          </RouterLink>
          <RouterLink
            v-for="cat in categoryTree"
            :key="cat.id"
            :to="`/manage/courses?mt=${cat.id}`"
            class="cat-link"
            :class="{ active: route.query.mt === cat.id }"
          >
            {{ cat.name }}
          </RouterLink>
        </div>

        <div class="content">
          <div v-if="loading" class="loading">加载中...</div>
          <div v-else-if="courses.length" class="course-list">
            <div v-for="c in courses" :key="c.id" class="course-row">
              <div class="course-pic">
                <img v-if="coursePicUrls.get(c.id!)" :src="coursePicUrls.get(c.id!)" :alt="c.name" />
                <div v-else class="course-pic-placeholder">
                  {{ c.name?.charAt(0) || '课' }}
                </div>
              </div>
              <div class="course-info">
                <h3 class="course-name">{{ c.name }}</h3>
                <p class="course-meta">
                  {{ c.companyName }} · <StatusBadge :code="c.courseStatus" />
                </p>
              </div>
              <div class="course-actions">
                <button class="btn-action" @click="goEdit(c.id!)" title="编辑">
                  <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/></svg>
                  编辑
                </button>
                <button
                  class="btn-action btn-danger"
                  :disabled="c.courseStatus !== '000'"
                  @click="handleDelete(c)"
                  title="删除"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/><line x1="10" x2="10" y1="11" y2="17"/><line x1="14" x2="14" y1="11" y2="17"/></svg>
                  删除
                </button>
                <button
                  class="btn-action btn-primary"
                  :disabled="c.courseStatus !== '100' || !isSuperAdmin"
                  @click="handlePublish(c)"
                  title="发布（仅 super_admin 可操作待审核课程）"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 19l7-7 3 3-7 7-3-3z"/><path d="M18 13l-1.5-7.5L2 2l3.5 14.5L13 18l5-5z"/><path d="M2 2l7.586 7.586"/><path d="M13 18l-5-5"/></svg>
                  发布
                </button>
              </div>
            </div>
          </div>
          <div v-else class="empty">
            <div class="empty-inner">
              <p class="empty-text">暂无课程</p>
              <p class="empty-hint">点击下方按钮创建您的第一门课程</p>
              <button class="btn-add empty-cta" @click="goAdd">
                <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14"/><path d="M5 12h14"/></svg>
                添加课程
              </button>
            </div>
          </div>

          <div v-if="total > pageSize" class="pagination">
            <button :disabled="pageNo <= 1" @click="pageNo--">上一页</button>
            <span>{{ pageNo }} / {{ totalPages }}</span>
            <button :disabled="pageNo >= totalPages" @click="pageNo++">下一页</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.courses-page {
  padding: var(--space-6) var(--space-4);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}

.page-title {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, sans-serif;
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0;
  color: var(--color-text);
}

.page-title svg {
  color: var(--color-accent);
  flex-shrink: 0;
}

.btn-add {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-5);
  background: var(--color-accent);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  transition: all var(--transition-base);
}

.btn-add:hover {
  background: var(--color-accent-hover);
}


/* 搜索栏：一行水平对齐，Ant Design Vue 风格，护眼易辨析 */
.search-bar {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin-bottom: var(--space-5);
  flex-wrap: wrap;
}

.filter-icon {
  display: flex;
  align-items: center;
  color: var(--color-text);
  flex-shrink: 0;
}

.search-input {
  height: 40px;
  padding: 0 var(--space-4) 0 2.75rem;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-bg-card);
  color: var(--color-text);
  font-size: 15px;
  line-height: 1.5;
}

.search-input-wrap {
  position: relative;
  flex: 1;
  min-width: 220px;
}

.search-input-wrap .search-icon {
  position: absolute;
  left: var(--space-4);
  top: 50%;
  transform: translateY(-50%);
  z-index: 1;
  color: var(--color-text-muted);
}

/* 筛选项：标签与下拉同一行，与搜索框水平对齐 */
.filter-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.filter-label-inline {
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text);
  white-space: nowrap;
}

.search-select {
  height: 40px;
  padding: 0 var(--space-4);
  min-width: 120px;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-bg-card);
  color: var(--color-text);
  font-size: 15px;
  line-height: 1.5;
}

.btn-search {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  height: 40px;
  padding: 0 var(--space-5);
  background: var(--color-accent);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-base);
}

.btn-search:hover {
  background: var(--color-accent-hover);
}

.main {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: var(--space-6);
}

.sidebar {
  padding: var(--space-4);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  height: fit-content;
}

.sidebar h3 {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: var(--space-4);
  color: var(--color-text);
}

.sidebar h3 svg {
  color: var(--color-accent);
  flex-shrink: 0;
}

.cat-link {
  display: block;
  padding: var(--space-3) 0;
  color: var(--color-text);
  text-decoration: none;
  transition: color var(--transition-base);
}

.cat-link:hover,
.cat-link.active {
  color: var(--color-accent);
}

.content {
  min-height: 400px;
}

.course-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.course-row {
  display: flex;
  align-items: center;
  gap: var(--space-5);
  padding: var(--space-4);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  transition: border-color var(--transition-base);
}

.course-row:hover {
  border-color: var(--color-accent);
}

.course-pic {
  width: 120px;
  height: 68px;
  flex-shrink: 0;
  background: var(--color-primary-light);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.course-pic img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.course-pic-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: var(--color-accent);
}

.course-info {
  flex: 1;
  min-width: 0;
}

.course-name {
  font-size: 1.05rem;
  font-weight: 500;
  margin: 0 0 var(--space-1);
  color: var(--color-text);
}

.course-meta {
  font-size: 0.9rem;
  color: var(--color-text-muted);
  margin: 0;
}

.course-actions {
  display: flex;
  gap: var(--space-3);
  flex-shrink: 0;
}

.btn-action {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  font-size: 15px;
  font-weight: 500;
  background: transparent;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  color: var(--color-text);
  cursor: pointer;
  transition: all var(--transition-base);
}

.btn-action:hover:not(:disabled) {
  border-color: var(--color-accent);
  color: var(--color-accent);
}

.btn-action:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-action.btn-primary:hover:not(:disabled) {
  background: var(--color-accent);
  color: white;
  border-color: var(--color-accent);
}

.btn-action.btn-danger:hover:not(:disabled) {
  border-color: #dc3545;
  color: #dc3545;
}

.loading {
  text-align: center;
  padding: var(--space-8);
  color: var(--color-text-muted);
}

.empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 280px;
  padding: var(--space-8);
}

.empty-inner {
  text-align: center;
  max-width: 360px;
}

.empty-text {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-text);
  margin: 0 0 var(--space-2);
}

.empty-hint {
  font-size: 0.95rem;
  color: var(--color-text-muted);
  margin: 0 0 var(--space-5);
  line-height: 1.5;
}

.empty-cta {
  margin: 0 auto;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-4);
  margin-top: var(--space-6);
}

.pagination button {
  padding: var(--space-3) var(--space-5);
  font-size: 1rem;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  color: var(--color-text);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-base);
}

.pagination button:hover:not(:disabled) {
  border-color: var(--color-accent);
  color: var(--color-accent);
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .main {
    grid-template-columns: 1fr;
  }

  .course-row {
    flex-wrap: wrap;
  }

  .course-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
