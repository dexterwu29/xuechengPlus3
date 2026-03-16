<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { listMessages, getUnreadCount, markAsRead } from '@/api/content/message'
import type { SystemMessageVO } from '@/types/content'

const router = useRouter()
const loading = ref(false)
const messages = ref<SystemMessageVO[]>([])
const total = ref(0)
const pageNo = ref(1)
const pageSize = ref(20)
const unreadCount = ref(0)

async function loadMessages() {
  loading.value = true
  try {
    const res = await listMessages(pageNo.value, pageSize.value)
    messages.value = res?.items ?? []
    total.value = res?.total ?? 0
  } catch {
    messages.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function loadUnreadCount() {
  try {
    unreadCount.value = (await getUnreadCount()) ?? 0
  } catch {
    unreadCount.value = 0
  }
}

async function handleMarkRead(m: SystemMessageVO) {
  if (m.isRead || !m.id) return
  try {
    await markAsRead(m.id)
    m.isRead = true
    loadUnreadCount()
  } catch {
    // ignore
  }
}

function formatTime(s: string) {
  if (!s) return '-'
  const d = new Date(s)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  return d.toLocaleDateString()
}

onMounted(() => {
  loadMessages()
  loadUnreadCount()
})

watch(pageNo, loadMessages)
</script>

<template>
  <div class="message-center">
    <div class="container">
      <h1 class="page-title">站内信</h1>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="!messages.length" class="empty">
        <p>暂无消息</p>
      </div>
      <div v-else class="message-list">
        <div
          v-for="m in messages"
          :key="m.id"
          class="message-item"
          :class="{ unread: !m.isRead }"
          @click="handleMarkRead(m)"
        >
          <div class="msg-header">
            <span class="msg-title">{{ m.title }}</span>
            <span class="msg-time">{{ formatTime(m.createTime) }}</span>
          </div>
          <div class="msg-content">{{ m.content }}</div>
          <div v-if="m.courseId" class="msg-meta">
            课程：{{ m.courseName || `#${m.courseId}` }}
          </div>
        </div>
      </div>
      <div v-if="total > pageSize" class="pagination">
        <a-button :disabled="pageNo <= 1" @click="pageNo--">上一页</a-button>
        <span>{{ pageNo }} / {{ Math.ceil(total / pageSize) }}</span>
        <a-button :disabled="pageNo >= Math.ceil(total / pageSize)" @click="pageNo++">下一页</a-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.message-center {
  padding: var(--space-6) var(--space-4);
}

.container {
  max-width: 700px;
  margin: 0 auto;
}

.page-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0 0 var(--space-4);
}

.loading,
.empty {
  text-align: center;
  padding: var(--space-8);
  color: var(--color-text-muted);
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.message-item {
  padding: var(--space-4);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: border-color var(--transition-base);
}

.message-item:hover {
  border-color: var(--color-accent);
}

.message-item.unread {
  border-left: 3px solid var(--color-accent);
}

.msg-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-2);
}

.msg-title {
  font-weight: 500;
  flex: 1;
}

.msg-time {
  font-size: 0.85rem;
  color: var(--color-text-muted);
}

.msg-content {
  font-size: 0.95rem;
  color: var(--color-text);
  line-height: 1.5;
}

.msg-meta {
  margin-top: var(--space-2);
  font-size: 0.85rem;
  color: var(--color-text-muted);
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-4);
  margin-top: var(--space-6);
}
</style>
