import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import Layout from '@/layouts/Layout.vue'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '登录', public: true },
  },
  {
    path: '/',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/HomeView.vue'),
        meta: { title: '首页', public: true },
      },
      {
        path: 'courses',
        name: 'Courses',
        component: () => import('@/views/CoursesView.vue'),
        meta: { title: '课程列表', requiresAuth: true },
      },
      {
        path: 'courses/add',
        name: 'CourseAdd',
        component: () => import('@/views/CourseEditView.vue'),
        meta: { title: '添加课程', requiresAuth: true },
      },
      {
        path: 'courses/:id/edit',
        name: 'CourseEdit',
        component: () => import('@/views/CourseEditView.vue'),
        meta: { title: '编辑课程', requiresAuth: true },
      },
      {
        path: 'courses/:id',
        name: 'CourseDetail',
        component: () => import('@/views/CourseDetailView.vue'),
        meta: { title: '课程详情', public: true },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 学成在线` : '学成在线'
  const authStore = useAuthStore()
  if (to.meta.public) {
    next()
    return
  }
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  next()
})

export default router
