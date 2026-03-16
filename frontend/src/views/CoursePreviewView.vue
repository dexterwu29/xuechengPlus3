<script setup lang="ts">
/**
 * 课程预览页 - 供编辑者预览课程效果（需登录，使用管理端 API）
 * 解决原 /content/courses/:id/preview 后端 Thymeleaf 403 问题
 */
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CaretDownOutlined, CaretRightOutlined } from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import { getCourse } from '@/api/content/course'
import { listTeachplans } from '@/api/content/teachplan'
import { listTeachers } from '@/api/content/courseTeacher'
import { getFileInfo, getFileContent, isUnsupportedVideoFormat } from '@/api/content/media'
import { marked } from 'marked'
import type { CourseDetailVO, TeachplanTreeVO, TeachplanMediaVO, CourseTeacherVO } from '@/types/content'

const route = useRoute()
const router = useRouter()
const courseId = computed(() => Number(route.params.id))

const loading = ref(true)
const course = ref<CourseDetailVO | null>(null)
const teachplans = ref<TeachplanTreeVO[]>([])
const teachers = ref<CourseTeacherVO[]>([])
const expandedChapters = ref<Set<number>>(new Set([0]))
const previewFileId = ref<string | null>(null)
const previewUrl = ref<string | null>(null)
const previewLoading = ref(false)
const previewMediaItem = ref<{ mediaFileName?: string; mediaType?: string } | null>(null)
const previewMdHtml = ref<string | null>(null)

async function load() {
  if (!courseId.value) return
  loading.value = true
  try {
    const [c, t] = await Promise.all([
      getCourse(courseId.value),
      listTeachplans(courseId.value),
    ])
    course.value = c
    teachplans.value = t ?? []
    if ((t?.length ?? 0) > 0) expandedChapters.value.add(0)

    // 加载授课教师信息
    const teachList = await listTeachers(courseId.value)
    teachers.value = teachList ?? []
  } catch (e) {
    console.error(e)
    router.push({ path: '/manage/courses' })
  } finally {
    loading.value = false
  }
}

function toggleChapter(idx: number) {
  const s = new Set(expandedChapters.value)
  if (s.has(idx)) s.delete(idx)
  else s.add(idx)
  expandedChapters.value = s
}

function getMediaList(node: TeachplanTreeVO): (TeachplanMediaVO & { mediaFileName?: string; duration?: number })[] {
  if (node.mediaList?.length) return node.mediaList
  const single = node.teachplanMedia as { mediaFileName?: string; fileId?: string; duration?: number } | undefined
  return single ? [single] : []
}

/** 计算媒资总时长（秒），多视频时求和 */
function getTotalDurationSec(mediaList: { duration?: number }[]): number {
  if (!mediaList?.length) return 0
  return mediaList.reduce((sum, m) => sum + (m.duration ?? 0), 0)
}

/** 格式化时长为 MM:SS */
function formatDuration(sec: number): string {
  if (sec <= 0) return '—'
  const m = Math.floor(sec / 60)
  const s = Math.floor(sec % 60)
  return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}

function goBack() {
  router.push({ path: '/manage/courses' })
}

function goEdit() {
  router.push({ path: `/courses/${courseId.value}/edit` })
}

function isVideoFile(m: { mediaFileName?: string; mediaType?: string }): boolean {
  const ext = (m.mediaFileName ?? '').split('.').pop()?.toLowerCase()
  const videoExts = ['mp4', 'avi', 'mov', 'webm', 'mkv']
  return videoExts.includes(ext || '') || (m.mediaType ?? '').toLowerCase() === 'video'
}

function isPdfFile(m: { mediaFileName?: string }): boolean {
  return (m.mediaFileName ?? '').toLowerCase().endsWith('.pdf')
}

function isMdFile(m: { mediaFileName?: string }): boolean {
  const ext = (m.mediaFileName ?? '').split('.').pop()?.toLowerCase()
  return ext === 'md' || ext === 'markdown'
}

async function openPreview(fileId: string, mediaItem: { mediaFileName?: string; mediaType?: string }) {
  // 31.4：检测不支持的格式（如AVI）
  if (isUnsupportedVideoFormat(mediaItem.mediaFileName || '')) {
    const ext = (mediaItem.mediaFileName || '').split('.').pop()?.toLowerCase()
    Modal.warning({
      title: '无法预览',
      content: `${ext?.toUpperCase()} 格式视频无法在浏览器中直接播放。请前往编辑页将其转码为 MP4 格式后再预览。`,
    })
    return
  }

  previewMediaItem.value = mediaItem
  previewFileId.value = fileId
  previewUrl.value = null
  previewMdHtml.value = null
  previewLoading.value = true
  try {
    const info = await getFileInfo(fileId)
    previewUrl.value = info?.url ?? null
    if (!previewUrl.value) {
      console.warn('暂无预览地址')
      return
    }
    if (mediaItem && isMdFile(mediaItem)) {
      try {
        const text = await getFileContent(fileId)
        previewMdHtml.value = text ? (marked.parse(text) as string) : null
      } catch (e) {
        console.warn('MD content load failed:', e)
        previewMdHtml.value = null
      }
    }
  } catch (e) {
    console.error(e)
    closePreview()
  } finally {
    previewLoading.value = false
  }
}

function closePreview() {
  previewFileId.value = null
  previewUrl.value = null
  previewMediaItem.value = null
  previewMdHtml.value = null
}

/** 打开小节第一个媒资的预览 */
function openSectionPreview(section: TeachplanTreeVO) {
  const list = getMediaList(section)
  const first = list[0]
  const fid = first?.fileId ?? first?.mediaId
  if (fid) openPreview(fid, first)
}

onMounted(load)
</script>

<template>
  <div class="course-preview">
    <div v-if="loading" class="loading-wrap">
      <a-spin size="large" />
    </div>
    <template v-else-if="course">
      <div class="preview-header">
        <a-button class="btn-back" @click="goBack">← 返回</a-button>
        <a-button type="primary" @click="goEdit">编辑课程</a-button>
      </div>
      <h1 class="course-title">{{ course.name }}</h1>
      <div class="meta">
        <span v-if="course.companyName">机构：{{ course.companyName }}</span>
        <span v-if="course.users">适用：{{ course.users }}</span>
        <span v-if="course.grade">等级：{{ course.grade }}</span>
        <!-- 修复收费规则显示：201000免费，201001收费 -->
        <a-tag v-if="course.charge === '201000'" color="green">免费</a-tag>
        <a-tag v-else-if="course.charge === '201001' && course.price" color="red">¥{{ course.price }}</a-tag>
      </div>

      <div v-if="course.description" class="description">{{ course.description }}</div>

      <!-- 授课教师信息 -->
      <section v-if="teachers.length" class="teachers">
        <h2>授课教师</h2>
        <div class="teacher-list">
          <div v-for="t in teachers" :key="t.id" class="teacher-item">
            <div class="teacher-name">{{ t.teacherName }}</div>
            <div v-if="t.position" class="teacher-position">{{ t.position }}</div>
            <div v-if="t.description" class="teacher-desc">{{ t.description }}</div>
          </div>
        </div>
      </section>

      <section class="teachplan">
        <h2>教学大纲</h2>
        <div v-if="teachplans.length" class="teachplan-list">
          <div
            v-for="(chapter, chIdx) in teachplans"
            :key="chapter.id"
            class="teachplan-chapter"
          >
            <div class="chapter-title" @click="toggleChapter(chIdx)">
              <component :is="expandedChapters.has(chIdx) ? CaretDownOutlined : CaretRightOutlined" class="chapter-arrow" />
              <span>{{ chapter.pname }}</span>
            </div>
            <div v-if="expandedChapters.has(chIdx) && chapter.children?.length" class="chapter-lessons">
              <div
                v-for="section in chapter.children"
                :key="section.id"
                class="section"
              >
                <div class="section-row">
                  <button
                    v-if="getMediaList(section).length"
                    type="button"
                    class="section-play-btn"
                    title="预览"
                    @click="openSectionPreview(section)"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
                  </button>
                  <span v-else class="section-play-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
                  </span>
                  <span class="section-title">{{ section.pname }}</span>
                  <span v-if="getMediaList(section).length" class="section-duration">{{ formatDuration(getTotalDurationSec(getMediaList(section))) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <a-empty v-else description="暂无大纲" />
      </section>

      <div class="contact">
        <h3>报名咨询</h3>
        <p v-if="course.qq">QQ：{{ course.qq }}</p>
        <p v-if="course.wechat">微信：{{ course.wechat }}</p>
        <p v-if="course.phone">电话：{{ course.phone }}</p>
      </div>

      <!-- 31.3：同页面预览弹窗 -->
      <a-modal
        :open="!!previewFileId"
        title="媒体预览"
        :width="800"
        :footer="null"
        @cancel="closePreview"
      >
        <div v-if="previewLoading" class="preview-loading">加载中...</div>
        <template v-else-if="previewUrl && previewFileId">
          <div v-if="previewMediaItem && isVideoFile(previewMediaItem)" class="preview-modal-body">
            <video :src="previewUrl" controls class="preview-video-full" />
          </div>
          <div v-else-if="previewMediaItem && isPdfFile(previewMediaItem)" class="preview-modal-body">
            <div class="preview-doc-header">
              <span>PDF 文档</span>
              <a :href="previewUrl" target="_blank" rel="noopener">在新窗口打开</a>
            </div>
            <iframe :src="previewUrl" class="preview-iframe-full" title="PDF预览" />
          </div>
          <div v-else-if="previewMediaItem && isMdFile(previewMediaItem)" class="preview-modal-body">
            <div class="preview-doc-header">
              <span>Markdown 文档</span>
              <a :href="previewUrl" target="_blank" rel="noopener">在新窗口打开</a>
            </div>
            <div v-if="previewMdHtml" class="preview-md-content" v-html="previewMdHtml" />
            <div v-else class="preview-md-fallback">
              <p>无法在弹窗内加载预览，请点击上方「在新窗口打开」查看。</p>
            </div>
          </div>
          <div v-else class="preview-modal-body">
            <a :href="previewUrl" target="_blank" rel="noopener">在新窗口打开</a>
          </div>
        </template>
      </a-modal>
    </template>
  </div>
</template>

<style scoped>
.course-preview {
  max-width: 900px;
  margin: 0 auto;
  padding: var(--space-6) var(--space-4);
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: var(--space-8);
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}

.course-title {
  font-size: 1.75rem;
  margin-bottom: var(--space-4);
}

.meta {
  color: var(--color-text-muted);
  font-size: 0.9rem;
  margin-bottom: var(--space-6);
}

.meta span,
.meta :deep(.ant-tag) {
  margin-right: var(--space-4);
}

.description {
  margin: var(--space-6) 0;
  padding: var(--space-4);
  background: var(--color-bg);
  border-radius: var(--radius-md);
  white-space: pre-wrap;
}

.teachplan h2 {
  font-size: 1.25rem;
  margin-bottom: var(--space-4);
}

.teachplan-list {
  margin-top: var(--space-4);
}

.teachplan-chapter {
  margin: var(--space-4) 0;
  padding: var(--space-3);
  background: var(--color-bg);
  border-radius: var(--radius-md);
}

.chapter-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-weight: 600;
  cursor: pointer;
}

.chapter-arrow {
  font-size: 14px;
  color: var(--color-text-muted);
}

.chapter-lessons {
  margin-top: var(--space-2);
  margin-left: var(--space-6);
}

.section {
  margin: var(--space-2) 0;
}

.section-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.section-play-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4px;
  border: none;
  background: none;
  color: var(--color-accent);
  cursor: pointer;
  flex-shrink: 0;
  border-radius: 4px;
}

.section-play-btn:hover {
  background: rgba(214, 158, 46, 0.15);
}

.section-play-icon {
  display: flex;
  color: var(--color-text-muted);
  flex-shrink: 0;
}

.section-title {
  flex: 1;
  font-weight: 500;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.section-duration {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  flex-shrink: 0;
}

.teachers h2 {
  font-size: 1.25rem;
  margin-bottom: var(--space-4);
}

.teacher-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  margin-top: var(--space-4);
}

.teacher-item {
  padding: var(--space-4);
  background: var(--color-bg);
  border-radius: var(--radius-md);
}

.teacher-name {
  font-weight: 600;
  font-size: 1rem;
  color: var(--color-text);
  margin-bottom: var(--space-1);
}

.teacher-position {
  font-size: 0.875rem;
  color: var(--color-accent);
  margin-bottom: var(--space-1);
}

.teacher-desc {
  font-size: 0.9rem;
  color: var(--color-text-muted);
  line-height: 1.5;
}

.contact {
  margin-top: var(--space-8);
  padding: var(--space-6);
  background: #e8f4fd;
  border-radius: var(--radius-md);
}

.contact h3 {
  margin-bottom: var(--space-3);
  font-size: 1.1rem;
}

.contact p {
  margin: 4px 0;
}

/* 31.3：预览弹窗 */
.preview-modal-body {
  min-height: 400px;
}

.preview-loading {
  padding: var(--space-6);
  text-align: center;
  color: var(--color-text-muted);
}

.preview-video-full {
  width: 100%;
  max-height: 70vh;
  display: block;
}

.preview-doc-header {
  margin-bottom: var(--space-2);
}

.preview-doc-header a {
  color: var(--color-accent);
}

.preview-iframe-full {
  width: 100%;
  height: 70vh;
  min-height: 400px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
}

.preview-md-content {
  width: 100%;
  max-height: 70vh;
  min-height: 400px;
  overflow: auto;
  padding: var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: #fff;
}

.preview-md-content :deep(h1) { font-size: 1.5rem; margin: 0.5em 0; }
.preview-md-content :deep(h2) { font-size: 1.25rem; margin: 0.5em 0; }
.preview-md-content :deep(h3) { font-size: 1.1rem; margin: 0.5em 0; }
.preview-md-content :deep(p) { margin: 0.5em 0; line-height: 1.6; }
.preview-md-content :deep(ul), .preview-md-content :deep(ol) { margin: 0.5em 0; padding-left: 1.5em; }
.preview-md-content :deep(code) { background: #f5f5f5; padding: 2px 6px; border-radius: 4px; font-size: 0.9em; }
.preview-md-content :deep(pre) { background: #f5f5f5; padding: var(--space-3); overflow-x: auto; border-radius: 4px; }
.preview-md-content :deep(pre code) { background: none; padding: 0; }
.preview-md-content :deep(blockquote) { border-left: 4px solid var(--color-accent); margin: 0.5em 0; padding-left: 1em; color: #666; }
.preview-md-content :deep(a) { color: var(--color-accent); }

.preview-md-fallback {
  padding: var(--space-6);
  text-align: center;
  color: var(--color-text-muted);
}
</style>
