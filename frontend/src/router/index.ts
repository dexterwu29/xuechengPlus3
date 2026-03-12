import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import Layout from '@/layouts/Layout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/HomeView.vue'),
        meta: { title: '首页' },
      },
      {
        path: 'courses',
        name: 'Courses',
        component: () => import('@/views/CoursesView.vue'),
        meta: { title: '课程列表' },
      },
      {
        path: 'courses/add',
        name: 'CourseAdd',
        component: () => import('@/views/CourseEditView.vue'),
        meta: { title: '添加课程' },
      },
      {
        path: 'courses/:id/edit',
        name: 'CourseEdit',
        component: () => import('@/views/CourseEditView.vue'),
        meta: { title: '编辑课程' },
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
  next()
})

export default router
