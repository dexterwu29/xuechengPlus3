<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { RouterLink } from 'vue-router'
import { listPublicCourses } from '@/api/content/publicCourse'
import type { CourseBaseVO } from '@/types/content'

const route = useRoute()
const tab = computed(() => (route.params.tab as string) || 'free')

const courses = ref<CourseBaseVO[]>([])
const loading = ref(false)

const tabTitle = computed(() => {
  const m: Record<string, string> = {
    free: '免费课（含试看）',
    featured: '精选推荐',
    new: '新上架',
  }
  return m[tab.value] || '课程'
})

async function loadCourses() {
  loading.value = true
  try {
    const res = await listPublicCourses({
      pageNo: 1,
      pageSize: 24,
      mt: (route.query.mt as string) || undefined,
    })
    courses.value = res?.items ?? []
    // TODO: 按 tab 筛选：free=免费+试看，featured=精选，new=新上架（需后端支持）
  } catch {
    courses.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => loadCourses())
</script>

<template>
  <div class="course-list-public">
    <div class="container">
      <h1 class="page-title">{{ tabTitle }}</h1>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="courses.length" class="course-grid">
        <RouterLink
          v-for="c in courses"
          :key="c.id"
          :to="`/courses/${c.id}`"
          class="course-card"
        >
          <div class="course-pic">
            <img v-if="c.pic?.startsWith('http')" :src="c.pic" :alt="c.name" />
            <div v-else class="course-pic-placeholder">课程封面</div>
            <div class="course-play-overlay">
              <span class="play-icon" aria-hidden="true">
                <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
              </span>
            </div>
          </div>
          <div class="course-info">
            <h3 class="course-name">{{ c.name }}</h3>
            <p v-if="c.users" class="course-users">{{ c.users }}</p>
          </div>
        </RouterLink>
      </div>
      <div v-else class="empty">暂无课程</div>
    </div>
  </div>
</template>

<style scoped>
.course-list-public {
  padding: var(--space-6) var(--space-4);
}
.container {
  max-width: 1200px;
  margin: 0 auto;
}
.page-title {
  font-size: 1.5rem;
  margin-bottom: var(--space-6);
}
.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: var(--space-4);
}
.course-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  text-decoration: none;
  color: inherit;
  transition: all var(--transition-base);
}
.course-card:hover {
  border-color: var(--color-accent);
  box-shadow: var(--shadow-hover);
}
.course-pic {
  position: relative;
  aspect-ratio: 16/9;
  background: var(--color-bg);
  overflow: hidden;
}

.course-play-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0,0,0,0.3);
  opacity: 0;
  transition: opacity var(--transition-base);
}

.course-card:hover .course-play-overlay {
  opacity: 1;
}

.play-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  background: rgba(255,255,255,0.9);
  border-radius: 50%;
  color: var(--color-accent);
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
  color: var(--color-text-muted);
  font-size: 0.875rem;
}
.course-info {
  padding: var(--space-4);
}
.course-name {
  font-size: 1.125rem;
  font-weight: 600;
  margin-bottom: var(--space-2);
  color: var(--color-text);
}
.course-users {
  font-size: 0.875rem;
  color: var(--color-text-muted);
}
.loading,
.empty {
  text-align: center;
  padding: var(--space-8);
  color: var(--color-text-muted);
}
</style>
