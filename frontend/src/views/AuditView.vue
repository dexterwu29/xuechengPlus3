<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { listPending, getForAudit, listTeachplansForAudit, audit } from '@/api/content/audit'
import StatusBadge from '@/components/StatusBadge.vue'
import type { CourseBaseVO, CourseDetailVO, TeachplanTreeVO } from '@/types/content'
import { AuditAction } from '@/types/content'

const loading = ref(false)
const pendingList = ref<CourseBaseVO[]>([])
const selectedId = ref<number | null>(null)
const courseDetail = ref<CourseDetailVO | null>(null)
const teachplans = ref<TeachplanTreeVO[]>([])
const detailLoading = ref(false)
const auditOpinion = ref('')
const auditAction = ref<AuditActionType>('approve')
const submitting = ref(false)

type AuditActionType = (typeof AuditAction)[keyof typeof AuditAction]

async function loadPending() {
  loading.value = true
  try {
    pendingList.value = (await listPending()) ?? []
  } catch (e) {
    message.error((e as Error)?.message || '加载失败')
    pendingList.value = []
  } finally {
    loading.value = false
  }
}

async function selectCourse(id: number) {
  selectedId.value = id
  courseDetail.value = null
  teachplans.value = []
  detailLoading.value = true
  try {
    const [c, t] = await Promise.all([
      getForAudit(id),
      listTeachplansForAudit(id),
    ])
    courseDetail.value = c ?? null
    teachplans.value = t ?? []
  } catch (e) {
    message.error((e as Error)?.message || '加载详情失败')
  } finally {
    detailLoading.value = false
  }
}

async function doAudit() {
  if (!selectedId.value) return
  submitting.value = true
  try {
    await audit(selectedId.value, auditAction.value, auditOpinion.value || undefined)
    message.success(auditAction.value === 'approve' ? '已通过' : auditAction.value === 'reject' ? '已退回' : '已封禁')
    auditOpinion.value = ''
    selectedId.value = null
    courseDetail.value = null
    teachplans.value = []
    await loadPending()
  } catch (e) {
    message.error((e as Error)?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

onMounted(loadPending)
</script>

<template>
  <div class="audit-page">
    <div class="container">
      <h1 class="page-title">课程审核</h1>
      <div class="audit-layout">
        <div class="pending-list">
          <h3>待审核课程</h3>
          <div v-if="loading" class="loading">加载中...</div>
          <div v-else-if="!pendingList.length" class="empty">暂无待审核课程</div>
          <div v-else class="list">
            <div
              v-for="c in pendingList"
              :key="c.id"
              class="list-item"
              :class="{ active: selectedId === c.id }"
              @click="selectCourse(c.id!)"
            >
              <span class="name">{{ c.name }}</span>
              <StatusBadge :code="c.courseStatus" />
            </div>
          </div>
        </div>
        <div class="detail-panel">
          <div v-if="!selectedId" class="placeholder">请从左侧选择课程</div>
          <div v-else-if="detailLoading" class="loading">加载中...</div>
          <template v-else-if="courseDetail">
            <h3>{{ courseDetail.name }}</h3>
            <p class="meta">{{ courseDetail.companyName }}</p>
            <div v-if="courseDetail.description" class="desc">{{ courseDetail.description }}</div>
            <div v-if="teachplans.length" class="teachplans">
              <h4>教学大纲</h4>
              <div v-for="ch in teachplans" :key="ch.id" class="chapter">
                <div class="chapter-title">{{ ch.pname }}</div>
                <div v-for="sec in ch.children" :key="sec.id" class="section">{{ sec.pname }}</div>
              </div>
            </div>
            <div class="audit-actions">
              <a-radio-group v-model:value="auditAction" class="action-group">
                <a-radio-button value="approve">通过</a-radio-button>
                <a-radio-button value="reject">退回</a-radio-button>
                <a-radio-button value="ban">封禁</a-radio-button>
              </a-radio-group>
              <a-textarea
                v-model:value="auditOpinion"
                placeholder="审核意见（可选）"
                :rows="3"
                class="opinion-input"
              />
              <a-button type="primary" :loading="submitting" @click="doAudit">
                {{ auditAction === 'approve' ? '通过' : auditAction === 'reject' ? '退回' : '封禁' }}
              </a-button>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.audit-page {
  padding: var(--space-6) var(--space-4);
}

.container {
  max-width: 1000px;
  margin: 0 auto;
}

.page-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0 0 var(--space-4);
}

.audit-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: var(--space-6);
}

.pending-list {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-4);
}

.pending-list h3 {
  font-size: 1rem;
  margin: 0 0 var(--space-4);
}

.list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3);
  border-radius: var(--radius-sm);
  cursor: pointer;
  margin-bottom: var(--space-2);
}

.list-item:hover,
.list-item.active {
  background: var(--color-primary-light);
}

.list-item .name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: var(--space-2);
}

.detail-panel {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-5);
}

.placeholder,
.loading,
.empty {
  color: var(--color-text-muted);
  text-align: center;
  padding: var(--space-8);
}

.detail-panel h3 {
  margin: 0 0 var(--space-2);
}

.meta {
  color: var(--color-text-muted);
  font-size: 0.9rem;
  margin: 0 0 var(--space-4);
}

.desc {
  margin-bottom: var(--space-4);
  padding: var(--space-4);
  background: var(--color-bg-muted);
  border-radius: var(--radius-sm);
  white-space: pre-wrap;
}

.teachplans {
  margin-bottom: var(--space-5);
}

.teachplans h4 {
  font-size: 0.95rem;
  margin: 0 0 var(--space-3);
}

.chapter {
  margin-bottom: var(--space-3);
}

.chapter-title {
  font-weight: 500;
  margin-bottom: var(--space-1);
}

.section {
  margin-left: var(--space-4);
  font-size: 0.9rem;
  color: var(--color-text-muted);
}

.audit-actions {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.action-group {
  margin-bottom: 0;
}

.opinion-input {
  max-width: 400px;
}

@media (max-width: 768px) {
  .audit-layout {
    grid-template-columns: 1fr;
  }
}
</style>
