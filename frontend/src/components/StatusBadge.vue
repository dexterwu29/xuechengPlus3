<script setup lang="ts">
import { computed } from 'vue'
import { CourseStatus } from '@/types/content'

const props = defineProps<{
  code: string
}>()

const { label, variant } = computed(() => {
  const c = props.code
  if (c === CourseStatus.DRAFT) return { label: '草稿', variant: 'default' }
  if (c === CourseStatus.PENDING) return { label: '待审核', variant: 'warning' }
  if (c === CourseStatus.PUBLISHED) return { label: '已发布', variant: 'success' }
  if (c === CourseStatus.BANNED) return { label: '已下架', variant: 'danger' }
  return { label: c || '-', variant: 'default' }
})
</script>

<template>
  <span class="status-badge" :class="`status-${variant}`">{{ label }}</span>
</template>

<style scoped>
.status-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 500;
}
.status-default {
  background: var(--color-bg-muted);
  color: var(--color-text-muted);
}
.status-warning {
  background: #fff7e6;
  color: #d46b08;
}
.status-success {
  background: #f6ffed;
  color: #389e0d;
}
.status-danger {
  background: #fff2f0;
  color: #cf1322;
}
</style>
