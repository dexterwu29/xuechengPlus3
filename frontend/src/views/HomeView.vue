<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { RouterLink } from 'vue-router'
import { getCategoryTree } from '@/api/content/category'
import { listPublicCourses } from '@/api/content/publicCourse'
import type { CourseCategoryTreeVO, CourseBaseVO } from '@/types/content'

// Banner 轮播 - Light 主题
const banners = [
  {
    id: 1,
    title: '开启学习之旅',
    subtitle: '海量精品课程，助力职业成长',
    cta: '立即探索',
    image: 'https://placehold.co/1920x600/2c5282/d69e2e?text=Banner+1',
  },
  {
    id: 2,
    title: '名师领航',
    subtitle: '一线大厂讲师，实战经验倾囊相授',
    cta: '查看课程',
    image: 'https://placehold.co/1920x600/1a3a5c/d69e2e?text=Banner+2',
  },
  {
    id: 3,
    title: '终身学习',
    subtitle: '一次付费，持续更新，学无止境',
    cta: '了解更多',
    image: 'https://placehold.co/1920x600/2d3748/d69e2e?text=Banner+3',
  },
]

const currentBanner = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

function nextBanner() {
  currentBanner.value = (currentBanner.value + 1) % banners.length
}

function prevBanner() {
  currentBanner.value = (currentBanner.value - 1 + banners.length) % banners.length
}

function goToBanner(i: number) {
  currentBanner.value = i
}

function startAutoPlay() {
  timer = setInterval(nextBanner, 5000)
}

function stopAutoPlay() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

onMounted(() => {
  startAutoPlay()
})

onUnmounted(() => {
  stopAutoPlay()
})

// 分类树（可选展示）
const categoryTree = ref<CourseCategoryTreeVO[]>([])
const loadingCategories = ref(false)

async function loadCategories() {
  loadingCategories.value = true
  try {
    categoryTree.value = (await getCategoryTree()) || []
  } catch {
    categoryTree.value = []
  } finally {
    loadingCategories.value = false
  }
}

// 已发布课程
const publishedCourses = ref<CourseBaseVO[]>([])
const loadingCourses = ref(false)

async function loadPublishedCourses() {
  loadingCourses.value = true
  try {
    const res = await listPublicCourses({ pageNo: 1, pageSize: 8 })
    publishedCourses.value = res?.items ?? []
  } catch {
    publishedCourses.value = []
  } finally {
    loadingCourses.value = false
  }
}

onMounted(() => {
  loadCategories()
  loadPublishedCourses()
})
</script>

<template>
  <div class="home">
    <!-- Banner 轮播 - 参考 ai.codefather.cn 大图 + 渐变遮罩 + 文案居中 -->
    <section class="banner-section" @mouseenter="stopAutoPlay" @mouseleave="startAutoPlay">
      <div class="banner-track" :style="{ transform: `translateX(-${currentBanner * 100}%)` }">
        <div
          v-for="(b, i) in banners"
          :key="b.id"
          class="banner-slide"
          :class="{ active: i === currentBanner }"
        >
          <img :src="b.image" :alt="b.title" class="banner-img" />
          <div class="banner-overlay"></div>
          <div class="banner-content">
            <h1 class="banner-title">{{ b.title }}</h1>
            <p class="banner-subtitle">{{ b.subtitle }}</p>
            <RouterLink :to="'/courses/free'" class="banner-cta">{{ b.cta }}</RouterLink>
          </div>
        </div>
      </div>

      <!-- 左右箭头 -->
      <button
        class="banner-arrow banner-arrow-prev"
        aria-label="上一张"
        @click="prevBanner"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M15 18l-6-6 6-6"/>
        </svg>
      </button>
      <button
        class="banner-arrow banner-arrow-next"
        aria-label="下一张"
        @click="nextBanner"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 18l6-6-6-6"/>
        </svg>
      </button>

      <!-- 指示点 -->
      <div class="banner-dots">
        <button
          v-for="(_, i) in banners"
          :key="i"
          class="banner-dot"
          :class="{ active: i === currentBanner }"
          :aria-label="`切换到第 ${i + 1} 张`"
          @click="goToBanner(i)"
        />
      </div>
    </section>

    <!-- 已发布课程 - 首页展示 -->
    <section v-if="publishedCourses.length" class="section courses-section">
      <div class="container">
        <h2 class="section-title">精选课程</h2>
        <div class="course-grid">
          <RouterLink
            v-for="c in publishedCourses"
            :key="c.id"
            :to="`/courses/${c.id}`"
            class="course-card"
          >
            <div class="course-pic">
              <img
                v-if="c.pic?.startsWith('http')"
                :src="c.pic"
                :alt="c.name"
              />
              <div v-else class="course-pic-placeholder">课程封面</div>
            </div>
            <div class="course-info">
              <h3 class="course-name">{{ c.name }}</h3>
              <p v-if="c.users" class="course-users">{{ c.users }}</p>
            </div>
          </RouterLink>
        </div>
      </div>
    </section>

    <!-- 课程分类快捷入口 - UI-UX-Pro-Max: Bento Grid 风格 -->
    <section class="section categories-section">
      <div class="container">
        <h2 class="section-title">课程分类</h2>
        <div v-if="loadingCategories" class="loading">加载中...</div>
        <div v-else-if="categoryTree.length" class="category-grid">
          <RouterLink
            v-for="cat in categoryTree.slice(0, 8)"
            :key="cat.id"
            :to="`/courses/free?mt=${cat.id}`"
            class="category-card"
          >
            <span class="category-name">{{ cat.name }}</span>
          </RouterLink>
        </div>
        <div v-else class="category-placeholder">
          <p>暂无分类数据，请先启动后端服务</p>
        </div>
      </div>
    </section>

    <!-- 价值主张 - 转化导向文案 -->
    <section class="section value-section">
      <div class="container">
        <h2 class="section-title">为什么选择学成在线</h2>
        <div class="value-grid">
          <div class="value-card">
            <div class="value-icon">📚</div>
            <h3>精品课程</h3>
            <p>精选优质内容，覆盖主流技术栈与职场技能</p>
          </div>
          <div class="value-card">
            <div class="value-icon">👨‍🏫</div>
            <h3>名师授课</h3>
            <p>一线大厂讲师，实战经验倾囊相授</p>
          </div>
          <div class="value-card">
            <div class="value-icon">🔄</div>
            <h3>持续更新</h3>
            <p>课程内容持续迭代，紧跟技术前沿</p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA 区块 -->
    <section class="section cta-section">
      <div class="container cta-inner">
        <h2 class="cta-title">开启你的学习之旅</h2>
        <p class="cta-desc">海量课程等你探索，投资自己，收获未来</p>
        <RouterLink to="/courses/free" class="btn btn-cta">立即开始学习</RouterLink>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home {
  min-height: 100vh;
}

/* Banner - 参考 ai.codefather.cn 大图 + 渐变 + 居中文案 */
.banner-section {
  position: relative;
  width: 100%;
  height: 70vh;
  min-height: 400px;
  max-height: 600px;
  overflow: hidden;
  border-radius: 0 0 var(--radius-xl) var(--radius-xl);
}

.banner-track {
  display: flex;
  width: 100%;
  height: 100%;
  transition: transform var(--transition-slow);
}

.banner-slide {
  flex: 0 0 100%;
  position: relative;
  height: 100%;
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to bottom,
    rgba(45, 55, 72, 0.3) 0%,
    rgba(26, 58, 92, 0.75) 100%
  );
}

.banner-content {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-6);
  text-align: center;
}

.banner-title {
  font-family: 'Source Serif 4', serif;
  font-size: clamp(2rem, 5vw, 3.5rem);
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--space-3);
}

.banner-subtitle {
  font-size: clamp(1rem, 2vw, 1.25rem);
  color: var(--color-text-muted);
  margin-bottom: var(--space-6);
}

.banner-cta {
  display: inline-block;
  padding: var(--space-3) var(--space-6);
  background: var(--color-cta);
  color: white;
  font-weight: 600;
  border-radius: var(--radius-md);
  text-decoration: none;
  transition: all var(--transition-base);
}

.banner-cta:hover {
  background: var(--color-cta-hover);
  box-shadow: var(--shadow-hover);
}

.banner-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid var(--color-border);
  color: var(--color-text);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-base);
}

.banner-arrow:hover {
  background: rgba(233, 180, 76, 0.2);
  color: var(--color-accent);
}

.banner-arrow-prev {
  left: var(--space-4);
}

.banner-arrow-next {
  right: var(--space-4);
}

.banner-dots {
  position: absolute;
  bottom: var(--space-4);
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: var(--space-2);
}

.banner-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  border: none;
  cursor: pointer;
  transition: all var(--transition-base);
}

.banner-dot:hover,
.banner-dot.active {
  background: var(--color-accent);
  transform: scale(1.2);
}

/* Section 通用 */
.section {
  padding: var(--space-8) var(--space-4);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.section-title {
  font-family: 'Source Serif 4', serif;
  font-size: 2rem;
  font-weight: 600;
  text-align: center;
  margin-bottom: var(--space-6);
  color: var(--color-text);
}

/* 分类网格 - Bento Grid 风格 */
.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: var(--space-4);
}

.category-card {
  padding: var(--space-4);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  text-align: center;
  text-decoration: none;
  color: var(--color-text);
  transition: all var(--transition-base);
}

.category-card:hover {
  border-color: var(--color-accent);
  box-shadow: var(--shadow-hover);
}

.category-name {
  font-weight: 500;
}

.loading,
.category-placeholder {
  text-align: center;
  color: var(--color-text-muted);
  padding: var(--space-6);
}

/* 课程卡片 */
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
  aspect-ratio: 16/9;
  background: var(--color-bg);
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

/* 价值主张 */
.value-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: var(--space-6);
}

.value-card {
  padding: var(--space-6);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  text-align: center;
  transition: all var(--transition-base);
}

.value-card:hover {
  border-color: var(--color-accent);
  box-shadow: var(--shadow-hover);
}

.value-icon {
  font-size: 2.5rem;
  margin-bottom: var(--space-3);
}

.value-card h3 {
  font-size: 1.25rem;
  margin-bottom: var(--space-2);
  color: var(--color-text);
}

.value-card p {
  color: var(--color-text-muted);
  font-size: 0.9rem;
}

/* CTA 区块 */
.cta-section {
  background: linear-gradient(135deg, var(--color-primary-light), var(--color-primary));
  border-radius: var(--radius-xl);
  margin: 0 var(--space-4) var(--space-6);
}

.cta-inner {
  text-align: center;
}

.cta-title {
  font-family: 'Source Serif 4', serif;
  font-size: 2rem;
  margin-bottom: var(--space-3);
  color: white;
}

.cta-desc {
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: var(--space-6);
}

.btn-cta {
  display: inline-block;
  padding: var(--space-3) var(--space-6);
  background: var(--color-cta);
  color: white;
  font-weight: 600;
  border-radius: var(--radius-md);
  text-decoration: none;
  transition: all var(--transition-base);
}

.btn-cta:hover {
  background: var(--color-cta-hover);
  box-shadow: var(--shadow-hover);
}
</style>
