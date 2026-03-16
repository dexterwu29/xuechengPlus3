<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { PlayCircleOutlined, CaretDownOutlined, CaretRightOutlined } from '@ant-design/icons-vue'
import { getPublicCourseDetail, getPublicTeachplans } from '@/api/content/publicCourse'
import type { CourseDetailVO, TeachplanTreeVO, TeachplanMediaVO } from '@/types/content'

const route = useRoute()
const router = useRouter()
const courseId = computed(() => Number(route.params.id))

const loading = ref(true)
const course = ref<CourseDetailVO | null>(null)
const teachplans = ref<TeachplanTreeVO[]>([])
const expandedChapters = ref<Set<number>>(new Set([0])) // 默认展开第一章

async function load() {
  if (!courseId.value) return
  loading.value = true
  try {
    const [c, t] = await Promise.all([
      getPublicCourseDetail(courseId.value),
      getPublicTeachplans(courseId.value),
    ])
    course.value = c
    teachplans.value = t ?? []
    if ((t?.length ?? 0) > 0) expandedChapters.value.add(0)
  } catch (e) {
    console.error(e)
    router.push('/')
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

function isFree(node: TeachplanTreeVO): boolean {
  if (course.value?.charge === '201000') return true
  return node.isPreviewEnabled === '1'
}

function getMedia(node: TeachplanTreeVO): TeachplanMediaVO | Record<string, unknown> | undefined {
  return node.mediaList?.[0] ?? (node.teachplanMedia as TeachplanMediaVO | undefined)
}

function formatDuration(media?: TeachplanMediaVO | Record<string, unknown>): string {
  const sec = (media as { duration?: number })?.duration
  if (sec == null || sec <= 0) return '——'
  const m = Math.floor(sec / 60)
  const s = sec % 60
  return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}

function goPlay(planId: number) {
  router.push({ path: `/courses/${courseId.value}/play`, query: { teachplan: planId } })
}

onMounted(load)
</script>

<template>
  <div class="course-detail">
    <div v-if="loading" class="loading-wrap">
      <a-spin size="large" />
    </div>
    <template v-else-if="course">
      <div class="detail-header">
        <a-button class="btn-back" @click="router.push('/')">← 返回</a-button>
        <h1 class="course-title">{{ course.name }}</h1>
        <p v-if="course.users" class="course-users">适用人群：{{ course.users }}</p>
      </div>

      <div class="detail-body">
        <section v-if="(course.coverPicUrls?.length ?? 0) > 0" class="section cover-section">
          <div class="cover-gallery">
            <img
              v-for="(url, i) in course.coverPicUrls"
              :key="i"
              :src="url"
              :alt="`封面${i + 1}`"
              class="cover-img"
            />
          </div>
        </section>
        <section class="section intro-section">
          <h2>课程介绍</h2>
          <div class="intro-content">{{ course.description || '暂无介绍' }}</div>
        </section>

        <section class="section outline-section">
          <h2>课程目录</h2>
          <div v-if="teachplans.length" class="teachplan-list">
            <div
              v-for="(chapter, chIdx) in teachplans"
              :key="chapter.id"
              class="teachplan-chapter"
            >
              <div
                class="chapter-title"
                @click="toggleChapter(chIdx)"
              >
                <component :is="expandedChapters.has(chIdx) ? CaretDownOutlined : CaretRightOutlined" class="chapter-arrow" />
                <span>{{ chapter.pname }}</span>
              </div>
              <div v-if="expandedChapters.has(chIdx) && chapter.children?.length" class="chapter-lessons">
                <div
                  v-for="lesson in chapter.children"
                  :key="lesson.id"
                  class="teachplan-lesson"
                >
                  <button
                    v-if="getMedia(lesson)"
                    type="button"
                    class="btn-play"
                    title="播放"
                    @click.stop="goPlay(lesson.id)"
                  >
                    <PlayCircleOutlined />
                  </button>
                  <span v-else class="btn-play-placeholder" />
                  <span class="lesson-name">{{ lesson.pname }}</span>
                  <span v-if="isFree(lesson)" class="tag-free">免费</span>
                  <span class="lesson-duration">{{ formatDuration(getMedia(lesson)) }}</span>
                </div>
              </div>
            </div>
          </div>
          <a-empty v-else description="暂无大纲" />
        </section>

        <section class="section contact-section">
          <h2>联系销售</h2>
          <div class="contact-cards">
            <a-card v-if="course.qq" size="small" class="contact-card">
              <template #title>QQ</template>
              <a :href="`https://wpa.qq.com/msgrd?v=3&uin=${course.qq}&site=qq`" target="_blank" rel="noopener">
                {{ course.qq }}
              </a>
            </a-card>
            <a-card v-if="course.wechat" size="small" class="contact-card">
              <template #title>微信</template>
              <span>{{ course.wechat }}</span>
            </a-card>
            <a-card v-if="course.phone" size="small" class="contact-card">
              <template #title>电话</template>
              <a :href="`tel:${course.phone}`">{{ course.phone }}</a>
            </a-card>
          </div>
          <a-empty v-if="!course.qq && !course.wechat && !course.phone" description="暂无联系方式" />
        </section>
      </div>
    </template>
  </div>
</template>

<style scoped>
.course-detail {
  padding: var(--space-6) var(--space-4);
  max-width: 900px;
  margin: 0 auto;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: var(--space-8);
}

.btn-back {
  margin-bottom: var(--space-4);
}

.course-title {
  font-size: 1.75rem;
  font-weight: 600;
  margin-bottom: var(--space-2);
}

.course-users {
  color: var(--color-text-muted);
  margin-bottom: var(--space-6);
}

.detail-body {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.cover-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
}

.cover-img {
  width: 200px;
  height: 112px;
  object-fit: cover;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
}

.section h2 {
  font-size: 1.25rem;
  margin-bottom: var(--space-4);
  color: var(--color-text);
}

.intro-content {
  line-height: 1.8;
  color: var(--color-text);
}

.teachplan-list {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.teachplan-chapter {
  border-bottom: 1px solid var(--color-border);
}

.teachplan-chapter:last-child {
  border-bottom: none;
}

.chapter-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  font-weight: 600;
  cursor: pointer;
  transition: background var(--transition-base);
}

.chapter-title:hover {
  background: var(--color-bg);
}

.chapter-arrow {
  font-size: 14px;
  color: var(--color-text-muted);
}

.chapter-lessons {
  background: var(--color-bg);
}

.teachplan-lesson {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2) var(--space-4) var(--space-2) 2.5rem;
  font-size: 0.95rem;
  border-bottom: 1px solid var(--color-border);
}

.teachplan-lesson:last-child {
  border-bottom: none;
}

.btn-play {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  padding: 0;
  background: transparent;
  border: none;
  color: var(--color-text-muted);
  cursor: pointer;
  transition: color var(--transition-base);
}

.btn-play:hover {
  color: var(--color-accent);
}

.btn-play-placeholder {
  width: 28px;
  flex-shrink: 0;
}

.lesson-name {
  flex: 1;
  min-width: 0;
}

.tag-free {
  flex-shrink: 0;
  padding: 2px 8px;
  font-size: 0.75rem;
  background: #ff4d4f;
  color: white;
  border-radius: 4px;
}

.lesson-duration {
  flex-shrink: 0;
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.contact-cards {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-4);
}

.contact-card {
  min-width: 140px;
}

.contact-card a {
  color: var(--color-accent);
  text-decoration: none;
}

.contact-card a:hover {
  text-decoration: underline;
}
</style>
