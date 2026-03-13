<script setup lang="ts">
/**
 * 课程封面上传
 * 上传后得到 fileId，父组件设置 pic = "media:" + fileId
 */
import { ref, watch } from 'vue'
import { getFileInfo } from '@/api/content/media'
import MediaUpload from './MediaUpload.vue'

const props = defineProps<{
  modelValue?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const picUrl = ref<string>('')

function extractFileId(pic: string | undefined): string | null {
  if (!pic) return null
  if (pic.startsWith('media:')) return pic.slice(6)
  return null
}

async function loadPicUrl() {
  const fileId = extractFileId(props.modelValue)
  if (!fileId) {
    picUrl.value = ''
    return
  }
  try {
    const info = await getFileInfo(fileId)
    picUrl.value = info.url || ''
  } catch {
    picUrl.value = ''
  }
}

watch(
  () => props.modelValue,
  loadPicUrl,
  { immediate: true }
)

function onSuccess(fileId: string) {
  emit('update:modelValue', `media:${fileId}`)
  loadPicUrl()
}
</script>

<template>
  <div class="course-pic-upload">
    <div v-if="picUrl" class="pic-preview">
      <img :src="picUrl" alt="课程封面" />
    </div>
    <MediaUpload
      accept="image/*"
      :max-count="1"
      list-type="picture-card"
      tip="建议尺寸 480x270，支持 jpg/png"
      @success="onSuccess"
    />
  </div>
</template>

<style scoped>
.course-pic-upload {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-4);
  align-items: flex-start;
}

.pic-preview {
  width: 160px;
  height: 90px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--color-bg);
}

.pic-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
