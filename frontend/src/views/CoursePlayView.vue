<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPublicCourseDetail, getPublicTeachplans } from '@/api/content/publicCourse'
import type { CourseDetailVO, TeachplanTreeVO } from '@/types/content'

const route = useRoute()
const router = useRouter()
const courseId = computed(() => Number(route.params.id))
const teachplanId = computed(() => Number(route.query.teachplan))

const loading = ref(true)
const course = ref<CourseDetailVO | null>(null)
const teachplans = ref<TeachplanTreeVO[]>([])

function findLesson(nodes: TeachplanTreeVO[], id: number): TeachplanTreeVO | null {
  for (const n of nodes) {
    if (n.id === id) return n
    if (n.children?.length) {
      const found = findLesson(n.children, id)
      if (found) return found
    }
  }
  return null
}

const currentLesson = computed(() => {
  if (!teachplanId.value) return null
  return findLesson(teachplans.value, teachplanId.value)
})

const playUrl = computed(() => {
  if (!courseId.value) return ''
  return `/content/public/courses/${courseId.value}/preview?teachplan=${teachplanId.value}`
})

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
  } catch (e) {
    console.error(e)
    router.push('/')
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push(`/courses/${courseId.value}`)
}

onMounted(load)
</script>

<template>
  <div class="course-play">
    <div v-if="loading" class="loading-wrap">
      <a-spin size="large" />
    </div>
    <template v-else-if="course">
      <div class="play-header">
        <a-button class="btn-back" @click="goBack">← 返回课程</a-button>
        <h1 class="play-title">{{ course.name }}</h1>
        <p v-if="currentLesson" class="play-lesson">{{ currentLesson.pname }}</p>
      </div>

      <div class="play-body">
        <div class="video-container">
          <iframe
            v-if="teachplanId"
            :src="playUrl"
            class="video-iframe"
            title="课程播放"
            allowfullscreen
          />
          <div v-else class="video-placeholder">
            <p>请从课程目录选择要播放的章节</p>
            <a-button type="primary" @click="goBack">返回课程详情</a-button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.course-play {
  padding: var(--space-6) var(--space-4);
  max-width: 1000px;
  margin: 0 auto;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: var(--space-8);
}

.play-header {
  margin-bottom: var(--space-4);
}

.btn-back {
  margin-bottom: var(--space-2);
}

.play-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0 0 var(--space-1);
}

.play-lesson {
  font-size: 0.95rem;
  color: var(--color-text-muted);
  margin: 0;
}

.play-body {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.video-container {
  position: relative;
  aspect-ratio: 16/9;
  background: #000;
}

.video-iframe {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border: none;
}

.video-placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-4);
  color: var(--color-text-muted);
}
</style>
