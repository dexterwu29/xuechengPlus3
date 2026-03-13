<script setup lang="ts">
import { ref, computed } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const navOpen = ref(false)

const isLoggedIn = computed(() => authStore.isLoggedIn)
const username = computed(() => authStore.user?.username ?? '')

function logout() {
  authStore.logout()
  router.push('/')
}
</script>

<template>
  <div class="layout">
    <!-- 顶部导航 - UI-UX-Pro-Max: 浮动导航 + 玻璃态 -->
    <header class="header">
      <div class="header-inner">
        <RouterLink to="/" class="logo">
          <span class="logo-text">学成在线</span>
        </RouterLink>
        <nav class="nav" :class="{ open: navOpen }">
          <RouterLink to="/" class="nav-link">首页</RouterLink>
          <RouterLink to="/courses" class="nav-link">课程</RouterLink>
          <RouterLink to="/courses" class="nav-link">关于</RouterLink>
        </nav>
        <div class="header-actions">
          <RouterLink v-if="isLoggedIn" to="/courses" class="btn btn-primary">课程管理</RouterLink>
          <RouterLink v-else to="/" class="btn btn-primary">立即学习</RouterLink>
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
  gap: var(--space-6);
}

.nav-link {
  color: var(--color-text-muted);
  text-decoration: none;
  font-weight: 500;
  transition: color var(--transition-base);
}

.nav-link:hover,
.nav-link.router-link-active {
  color: var(--color-accent);
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
