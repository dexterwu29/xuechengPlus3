<script setup lang="ts">
import { ref, computed, h, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { HomeOutlined, AppstoreOutlined, BookOutlined, TeamOutlined, FileTextOutlined } from '@ant-design/icons-vue'
import type { MenuProps } from 'ant-design-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const navOpen = ref(false)

const isLoggedIn = computed(() => authStore.isLoggedIn)
const username = computed(() => authStore.user?.username ?? '')
const role = computed(() => authStore.user?.role ?? '')
/** 管理页面仅 teacher、super_admin 可见（已移除 admin 角色） */
const canManage = computed(() => ['teacher', 'super_admin'].includes(role.value))

const selectedKeys = ref<string[]>([])
const openKeys = ref<string[]>([])

function syncMenuState() {
  const p = route.path
  const sel: string[] = []
  const open: string[] = []
  if (p === '/') sel.push('home')
  else if (p.startsWith('/manage/courses')) { sel.push('manage-courses'); open.push('manage') }
  else if (p.startsWith('/manage/media')) { sel.push('manage-media'); open.push('manage') }
  else if (p.startsWith('/manage/archives')) { sel.push('manage-archives'); open.push('manage') }
  else if (p.startsWith('/manage/audit')) { sel.push('manage-audit'); open.push('manage') }
  else if (p.startsWith('/manage/messages')) { sel.push('manage-messages'); open.push('manage') }
  else if (p.startsWith('/courses/free')) { sel.push('courses-free'); open.push('courses') }
  else if (p.startsWith('/courses/featured')) { sel.push('courses-featured'); open.push('courses') }
  else if (p.startsWith('/courses/new')) { sel.push('courses-new'); open.push('courses') }
  else if (p.startsWith('/forum')) sel.push('forum')
  else if (p.startsWith('/about')) sel.push('about')
  selectedKeys.value = sel
  openKeys.value = open
}
syncMenuState()

const menuItems = computed<MenuProps['items']>(() => {
  const items: MenuProps['items'] = [
    { key: 'home', icon: () => h(HomeOutlined), label: '首页', title: '首页', onClick: () => router.push('/') },
  ]
  if (canManage.value) {
    const manageChildren: MenuProps['items'] = [
      { key: 'manage-courses', label: '课程管理', title: '课程管理', onClick: () => router.push('/manage/courses') },
      { key: 'manage-media', label: '媒资管理', title: '媒资管理', onClick: () => router.push('/manage/media') },
      { key: 'manage-archives', label: '备案管理', title: '机构/讲师/销售备案', onClick: () => router.push('/manage/archives') },
    ]
    if (role.value === 'super_admin') {
      manageChildren.push({ key: 'manage-audit', label: '课程审核', title: '课程审核', onClick: () => router.push('/manage/audit') })
    }
    manageChildren.push({ key: 'manage-messages', label: '站内信', title: '站内信', onClick: () => router.push('/manage/messages') })
    items.push({
      key: 'manage',
      icon: () => h(AppstoreOutlined),
      label: '管理页面',
      title: '管理页面',
      children: manageChildren,
    })
  }
  /* 文档第一 1.3：课程拆分为免费课、热门专题1(精选推荐)、热门专题2(新上架) */
  items.push({
    key: 'courses',
    icon: () => h(BookOutlined),
    label: '课程',
    title: '课程',
    children: [
      { key: 'courses-free', label: '免费课', title: '免费课（含试看）', onClick: () => router.push('/courses/free') },
      { key: 'courses-featured', label: '精选推荐', title: '热门专题1：精选推荐', onClick: () => router.push('/courses/featured') },
      { key: 'courses-new', label: '新上架', title: '热门专题2：新上架', onClick: () => router.push('/courses/new') },
    ],
  })
  items.push(
    { key: 'forum', icon: () => h(TeamOutlined), label: '分享论坛', title: '分享论坛', onClick: () => router.push('/forum') },
    { key: 'about', icon: () => h(FileTextOutlined), label: '关于我们', title: '关于我们', onClick: () => router.push('/about') }
  )
  return items
})

watch(() => route.path, syncMenuState)

function logout() {
  authStore.logout()
  router.push('/')
}
</script>

<template>
  <div class="layout">
    <header class="header">
      <div class="header-inner">
        <a class="logo" @click="router.push('/')">
          <span class="logo-text">学成在线</span>
        </a>
        <nav class="nav" :class="{ open: navOpen }">
          <a-menu
            v-model:selectedKeys="selectedKeys"
            v-model:openKeys="openKeys"
            mode="horizontal"
            :items="menuItems"
            class="nav-menu"
          />
        </nav>
        <div class="header-actions">
          <RouterLink v-if="!isLoggedIn" to="/login" class="btn btn-outline">登录</RouterLink>
          <template v-else>
            <span class="user-name">{{ username }}</span>
            <button type="button" class="btn btn-outline" @click="logout">退出</button>
          </template>
        </div>
        <button
          class="nav-toggle"
          aria-label="切换菜单"
          @click="navOpen = !navOpen"
        >
          <span></span>
          <span></span>
          <span></span>
        </button>
      </div>
    </header>

    <main class="main">
      <RouterView />
    </main>

    <footer class="footer">
      <div class="footer-inner">
        <p>© 2026 学成在线 · 专业在线教育平台</p>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  position: fixed;
  top: var(--space-4);
  left: var(--space-4);
  right: var(--space-4);
  z-index: 50;
  background: var(--color-bg-card);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3) var(--space-4);
  max-width: 1280px;
  margin: 0 auto;
}

.logo {
  cursor: pointer;
  text-decoration: none;
  color: var(--color-text);
  font-family: 'Source Serif 4', serif;
  font-size: 1.5rem;
  font-weight: 600;
}

.logo-text {
  background: linear-gradient(135deg, var(--color-accent), var(--color-accent-hover));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav {
  display: flex;
  align-items: center;
  flex: 1;
  margin: 0 var(--space-4);
}

.nav-menu {
  border: none !important;
  background: transparent !important;
  line-height: 40px !important;
}

.nav-menu :deep(.ant-menu-item),
.nav-menu :deep(.ant-menu-submenu-title) {
  color: var(--color-text-muted) !important;
}

.nav-menu :deep(.ant-menu-item:hover),
.nav-menu :deep(.ant-menu-submenu-title:hover),
.nav-menu :deep(.ant-menu-item-selected) {
  color: var(--color-accent) !important;
}

.btn {
  padding: var(--space-3) var(--space-5);
  font-size: 1rem;
  border-radius: var(--radius-md);
  font-weight: 600;
  text-decoration: none;
  transition: all var(--transition-base);
  cursor: pointer;
  border: none;
}

.btn-primary {
  background: var(--color-cta);
  color: white;
}

.btn-primary:hover {
  background: var(--color-cta-hover);
  box-shadow: var(--shadow-hover);
}

.btn-outline {
  background: transparent;
  border: 1px solid var(--color-border);
  color: var(--color-text);
}

.btn-outline:hover {
  border-color: var(--color-accent);
  color: var(--color-accent);
}

.user-name {
  font-size: 0.9rem;
  color: var(--color-text-muted);
  margin-right: var(--space-2);
}

.nav-toggle {
  display: none;
  flex-direction: column;
  gap: 2px;
  background: none;
  border: none;
  cursor: pointer;
  padding: var(--space-2);
}

.nav-toggle span {
  width: 24px;
  height: 2px;
  background: var(--color-text);
}

.main {
  flex: 1;
  padding-top: 100px;
}

.footer {
  padding: var(--space-6);
  text-align: center;
  color: var(--color-text-muted);
  font-size: 0.875rem;
}

@media (max-width: 768px) {
  .nav {
    display: none;
  }

  .nav.open {
    display: flex;
    flex-direction: column;
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    background: var(--color-bg-card);
    padding: var(--space-4);
    border-radius: 0 0 var(--radius-lg) var(--radius-lg);
  }

  .nav-toggle {
    display: flex;
  }
}
</style>
