/**
 * 站内信组合式函数
 */
import { ref, onMounted } from 'vue'
import { listMessages, getUnreadCount, markAsRead } from '@/api/content/message'
import type { SystemMessageVO } from '@/types/content'
import type { PageResult } from '@/types/content'

export function useMessage() {
  const messages = ref<SystemMessageVO[]>([])
  const total = ref(0)
  const unreadCount = ref(0)
  const loading = ref(false)
  const pageNo = ref(1)
  const pageSize = ref(20)

  async function fetchMessages(p = 1, size = 20) {
    loading.value = true
    try {
      const res = await listMessages(p, size)
      messages.value = res?.items ?? []
      total.value = res?.total ?? 0
      pageNo.value = p
      pageSize.value = size
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

  async function markAsReadById(id: number) {
    try {
      await markAsRead(id)
      const m = messages.value.find((x) => x.id === id)
      if (m) m.isRead = true
      loadUnreadCount()
    } catch {
      // ignore
    }
  }

  onMounted(() => {
    loadUnreadCount()
  })

  return {
    messages,
    total,
    unreadCount,
    loading,
    pageNo,
    pageSize,
    fetchMessages,
    loadUnreadCount,
    markAsRead: markAsReadById,
  }
}
