<script setup lang="ts">
/**
 * 课程封面上传 - 多封面（最多3张、默认选择）
 * coverPics: ["media:fileId1","media:fileId2"]，defaultCoverIndex: 0-2
 */
import { ref, watch, computed } from 'vue'
import { getFileInfo } from '@/api/content/media'
import MediaUpload from './MediaUpload.vue'
import { StarFilled, StarOutlined } from '@ant-design/icons-vue'

const MAX_COUNT = 3
const MAX_SIZE = 2 * 1024 * 1024 // 2MB

const props = defineProps<{
  coverPics?: string[]  // ["media:fileId1", ...]
  defaultCoverIndex?: number
}>()

const emit = defineEmits<{
  'update:coverPics': [value: string[]]
  'update:defaultCoverIndex': [value: number]
}>()

const pics = ref<{ fileId: string; url: string }[]>([])

function extractFileId(pic: string): string | null {
  if (!pic || !pic.startsWith('media:')) return null
  return pic.slice(6)
}

async function loadUrls() {
  const arr = props.coverPics || []
  const results: { fileId: string; url: string }[] = []
  for (const p of arr) {
    const fid = extractFileId(p)
    if (!fid) continue
    try {
      const info = await getFileInfo(fid)
      results.push({ fileId: fid, url: info.url || '' })
    } catch {
      results.push({ fileId: fid, url: '' })
    }
  }
  pics.value = results
}

watch(
  () => props.coverPics,
  loadUrls,
  { immediate: true }
)

const defaultIdx = computed({
  get: () => Math.max(0, Math.min((props.defaultCoverIndex ?? 0), pics.value.length - 1)),
  set: (v) => emit('update:defaultCoverIndex', v),
})

function onSuccess(fileId: string, index: number) {
  const media = `media:${fileId}`
  const arr = [...(props.coverPics || [])]
  if (index < arr.length) {
    arr[index] = media
  } else {
    if (arr.length >= MAX_COUNT) return
    arr.push(media)
  }
  emit('update:coverPics', arr)
  if (arr.length === 1) emit('update:defaultCoverIndex', 0)
}

function removeAt(index: number) {
  const arr = (props.coverPics || []).filter((_, i) => i !== index)
  emit('update:coverPics', arr)
  let idx = (props.defaultCoverIndex ?? 0)
  if (idx >= arr.length) idx = Math.max(0, arr.length - 1)
  emit('update:defaultCoverIndex', idx)
}

function setDefault(index: number) {
  emit('update:defaultCoverIndex', index)
}
</script>

<template>
  <div class="course-pic-upload">
    <div
      v-for="(item, i) in pics"
      :key="i"
      class="pic-slot"
      :class="{ default: defaultIdx === i }"
    >
      <img v-if="item.url" :src="item.url" alt="封面" />
      <div v-else class="placeholder">加载中</div>
      <div class="actions">
        <span class="default-btn" @click="setDefault(i)">
          <StarFilled v-if="defaultIdx === i" />
          <StarOutlined v-else />
          {{ defaultIdx === i ? '默认' : '设默认' }}
        </span>
        <span class="del-btn" @click="removeAt(i)">删除</span>
      </div>
    </div>
    <div v-if="pics.length < MAX_COUNT" class="upload-slot">
      <MediaUpload
        accept="image/*"
        :max-count="1"
        list-type="picture-card"
        :max-size="MAX_SIZE"
        tip=""
        @success="(fid) => onSuccess(fid, pics.length)"
      />
    </div>
    <p class="tip">建议尺寸 480×270，单张 ≤2MB，最多3张，支持 jpg/png</p>
  </div>
</template>

<style scoped>
.course-pic-upload {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-4);
  align-items: flex-start;
}

.pic-slot {
  position: relative;
  width: 160px;
  height: 90px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--color-bg);
}

.pic-slot.default {
  border-color: var(--color-accent);
  box-shadow: 0 0 0 1px var(--color-accent);
}

.pic-slot img,
.pic-slot .placeholder {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pic-slot .placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 4px;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  gap: 8px;
  font-size: 0.7rem;
}

.default-btn,
.del-btn {
  cursor: pointer;
  color: #fff;
}

.del-btn:hover {
  color: #ff7875;
}

.upload-slot {
  width: 160px;
  height: 90px;
}

.tip {
  width: 100%;
  margin-top: var(--space-2);
  font-size: 0.75rem;
  color: var(--color-text-muted);
}
</style>
