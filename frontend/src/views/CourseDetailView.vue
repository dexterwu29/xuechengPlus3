<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPublicCourseDetail, getPublicTeachplans } from '@/api/content/publicCourse'
import type { CourseDetailVO, TeachplanTreeVO } from '@/types/content'

const route = useRoute()
const router = useRouter()
const courseId = computed(() => Number(route.params.id))

const loading = ref(true)
const course = ref<CourseDetailVO | null>(null)
const teachplans = ref<TeachplanTreeVO[]>([])

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

function flattenTeachplans(nodes: TeachplanTreeVO[], level = 0): { node: TeachplanTreeVO; level: number }[] {
  const out: { node: TeachplanTreeVO; level: number }[] = []
  for (const n of nodes) {
    out.push({ node: n, level })
    if (n.children?.length) {
      out.push(...flattenTeachplans(n.children, level + 1))
    }
  }
  return out
}

const flatPlans = computed(() => flattenTeachplans(teachplans.value))

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
        <section class="section intro-section">
          <h2>课程介绍</h2>
          <div class="intro-content">{{ course.description || '暂无介绍' }}</div>
        </section>

        <section class="section outline-section">
          <h2>课程大纲</h2>
          <div v-if="flatPlans.length" class="teachplan-list">
            <div
              v-for="{ node, level } in flatPlans"
              :key="node.id"
              class="teachplan-item"
              :style="{ paddingLeft: level * 24 + 16 + 'px' }"
            >
              <span class="plan-name">{{ node.pname }}</span>
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

.teachplan-item {
  padding: var(--space-3) var(--space-4);
  border-bottom: 1px solid var(--color-border);
}

.teachplan-item:last-child {
  border-bottom: none;
}

.plan-name {
  font-size: 0.95rem;
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
