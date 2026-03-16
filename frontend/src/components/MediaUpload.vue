<script setup lang="ts">
/**
 * 媒资上传组件
 * - 小文件(<5MB)直传
 * - 大文件分片上传
 * - 支持图片、视频、文档
 */
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import SparkMD5 from 'spark-md5'
import {
  uploadSimple,
  checkByMd5,
  initChunkUpload,
  getUploadStatus,
  uploadChunk,
  completeChunkUpload,
} from '@/api/content/media'

const CHUNK_SIZE = 5 * 1024 * 1024 // 5MB
const SIMPLE_LIMIT = 5 * 1024 * 1024 // 5MB

const props = withDefaults(
  defineProps<{
    accept?: string
    maxCount?: number
    listType?: 'text' | 'picture' | 'picture-card'
    tip?: string
    /** 单文件最大字节数，超出则上传前拦截（如封面上传 2MB） */
    maxSize?: number
  }>(),
  {
    accept: 'image/*,video/*,.mp4,.pdf,.doc,.docx',
    maxCount: 1,
    listType: 'picture-card',
    tip: '支持图片、视频、文档，单文件不超过500MB',
    maxSize: 0,
  }
)

const emit = defineEmits<{
  success: [fileId: string, fileName: string]
  change: [fileId: string | null]
}>()

const uploading = ref(false)
const percent = ref(0)

async function md5(file: File): Promise<string> {
  const buf = await file.arrayBuffer()
  return SparkMD5.ArrayBuffer.hash(buf)
}

async function uploadByChunk(file: File): Promise<string> {
  const fileMd5 = await md5(file)
  const { exists, fileId } = await checkByMd5(fileMd5)
  if (exists && fileId) return fileId

  const initRes = await initChunkUpload(fileMd5, file.name, file.size, CHUNK_SIZE)
  if (initRes.instant && initRes.fileId) return initRes.fileId

  const uploadId = initRes.uploadId ?? ''
  if (!uploadId) throw new Error('初始化失败')
  const totalChunks = Math.ceil(file.size / CHUNK_SIZE)
  const uploaded = await getUploadStatus(uploadId)

  for (let i = 0; i < totalChunks; i++) {
    if (uploaded.includes(i)) continue
    const start = i * CHUNK_SIZE
    const end = Math.min(start + CHUNK_SIZE, file.size)
    const chunk = file.slice(start, end)
    await uploadChunk(uploadId, i, chunk)
    percent.value = Math.round(((i + 1) / totalChunks) * 100)
  }

  return completeChunkUpload(uploadId)
}

async function handleUpload(options: { file: File }) {
  const { file } = options
  if (props.maxSize && file.size > props.maxSize) {
    const mb = (props.maxSize / 1024 / 1024).toFixed(1)
    message.error(`单张图片不超过 ${mb}MB`)
    return
  }
  uploading.value = true
  percent.value = 0
  try {
    let fileId: string
    if (file.size < SIMPLE_LIMIT) {
      fileId = await uploadSimple(file)
    } else {
      fileId = await uploadByChunk(file)
    }
    percent.value = 100
    emit('success', fileId, file.name)
    emit('change', fileId)
    message.success('上传成功')
  } catch (e) {
    message.error((e as Error)?.message || '上传失败')
    emit('change', null)
  } finally {
    uploading.value = false
    percent.value = 0
  }
}
</script>

<template>
  <div class="media-upload">
    <a-upload
      :accept="accept"
      :max-count="maxCount"
      :list-type="listType"
      :show-upload-list="false"
      :custom-request="handleUpload"
      :disabled="uploading"
    >
      <template v-if="listType === 'picture-card'">
        <div v-if="uploading" class="uploading-mask">
          <a-progress type="circle" :percent="percent" :size="48" />
        </div>
        <div v-else class="upload-trigger">
          <plus-outlined />
          <span>上传</span>
        </div>
      </template>
      <template v-else>
        <a-button :loading="uploading">选择文件</a-button>
      </template>
    </a-upload>
    <p v-if="tip" class="upload-tip">{{ tip }}</p>
  </div>
</template>

<style scoped>
.media-upload {
  display: inline-block;
}

.upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-4);
  color: var(--color-text-muted);
}

.upload-trigger:hover {
  color: var(--color-accent);
}

.uploading-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.9);
}

.upload-tip {
  margin-top: var(--space-2);
  font-size: 0.75rem;
  color: var(--color-text-muted);
}
</style>
