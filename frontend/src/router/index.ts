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
      /* 文档第一：管理页面仅 teacher、admin 可见 */
      {
        path: 'manage/courses',
        name: 'ManageCourses',
        component: () => import('@/views/CoursesView.vue'),
        meta: { title: '课程管理', roles: ['teacher', 'super_admin'] },
      },
      {
        path: 'manage/media',
        name: 'ManageMedia',
        component: () => import('@/views/ManageMediaView.vue'),
        meta: { title: '媒资管理', roles: ['teacher', 'super_admin'] },
      },
      {
        path: 'manage/archives',
        name: 'ManageArchives',
        component: () => import('@/views/ArchiveManageView.vue'),
        meta: { title: '备案管理', roles: ['teacher', 'super_admin'] },
      },
      {
        path: 'manage/audit',
        name: 'ManageAudit',
        component: () => import('@/views/AuditView.vue'),
        meta: { title: '课程审核', roles: ['super_admin'] },
      },
      {
        path: 'manage/messages',
        name: 'ManageMessages',
        component: () => import('@/views/MessageCenterView.vue'),
        meta: { title: '站内信', roles: ['teacher', 'super_admin'] },
      },
      {
        path: 'courses/add',
        name: 'CourseAdd',
        component: () => import('@/views/CourseEditView.vue'),
        meta: { title: '添加课程', roles: ['teacher', 'super_admin'] },
      },
      {
        path: 'courses/:id/edit',
        name: 'CourseEdit',
        component: () => import('@/views/CourseEditView.vue'),
        meta: { title: '编辑课程', roles: ['teacher', 'super_admin'] },
      },
      {
        path: 'courses/:tab(free|featured|new)',
        name: 'CourseListPublic',
        component: () => import('@/views/CourseListPublicView.vue'),
        meta: { title: '课程', public: true },
      },
      {
        path: 'courses/:id/play',
        name: 'CoursePlay',
        component: () => import('@/views/CoursePlayView.vue'),
        meta: { title: '课程播放', public: true },
      },
      {
        path: 'courses/:id/preview',
        name: 'CoursePreview',
        component: () => import('@/views/CoursePreviewView.vue'),
        meta: { title: '课程预览', roles: ['teacher', 'super_admin'] },
      },
      {
        path: 'courses/:id',
        name: 'CourseDetail',
        component: () => import('@/views/CourseDetailView.vue'),
        meta: { title: '课程详情', public: true },
      },
      {
        path: 'forum',
        name: 'Forum',
        component: () => import('@/views/ForumView.vue'),
        meta: { title: '分享论坛', public: true },
      },
      {
        path: 'about',
        name: 'About',
        component: () => import('@/views/AboutView.vue'),
        meta: { title: '关于我们', public: true },
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
  const roles = to.meta.roles as string[] | undefined
  if (roles?.length) {
    const userRole = authStore.user?.role
    const allowed = userRole && roles.includes(userRole)
    if (!authStore.isLoggedIn) {
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
    if (!allowed) {
      next({ path: '/' })
      return
    }
  } else if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  next()
})

export default router
