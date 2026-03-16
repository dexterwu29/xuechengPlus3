<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getCourse, createCourse, updateCourse, submitCourse } from '@/api/content/course'
import { getCourseMarket, saveCourseMarket } from '@/api/content/courseMarket'
import { getCategoryTree } from '@/api/content/category'
import { listTeachplans, createTeachplan, updateTeachplan, deleteTeachplan, moveTeachplanUp, moveTeachplanDown } from '@/api/content/teachplan'
import { bindMedia, unbindMediaByFileId, updateMediaDuration } from '@/api/content/teachplanMedia'
import { getFileInfo, getFileContent, isUnsupportedVideoFormat, checkTranscodeNeeded, transcodeToMp4 } from '@/api/content/media'
import { marked } from 'marked'
import { listTeachers, createTeacher, updateTeacher, deleteTeacher } from '@/api/content/courseTeacher'
import { useAuthStore } from '@/stores/auth'
import CoursePicUpload from '@/components/CoursePicUpload.vue'
import MediaUpload from '@/components/MediaUpload.vue'
import type {
  CourseDetailVO,
  CourseCreateDTO,
  CourseMarketDTO,
  TeachplanDTO,
  TeachplanTreeVO,
  CourseTeacherDTO,
  CourseTeacherVO,
  CourseCategoryTreeVO,
} from '@/types/content'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const courseId = computed(() => {
  const id = route.params.id
  return id === 'add' ? null : Number(id)
})

const step = ref(1)
const loading = ref(false)
const saving = ref(false)
const categoryTree = ref<CourseCategoryTreeVO[]>([])

// 基本信息
const baseForm = ref<CourseCreateDTO>({
  name: '',
  users: '',
  tags: '',
  mt: '',
  st: '',
  grade: '',
  teachMode: '',
  description: '',
  pic: '',
  coverPics: '',
  defaultCoverIndex: 0,
})

// 营销信息
const marketForm = ref<CourseMarketDTO>({
  charge: '201000',
  price: 0,
  originalPrice: 0,
  qq: '',
  wechat: '',
  phone: '',
  validDays: 365,
})

// 教学计划树
const teachplanTree = ref<TeachplanTreeVO[]>([])
const teachplanForm = ref<TeachplanDTO & { parentId: number }>({ pname: '', parentId: 0, grade: 1 })
/** 选中的节 ID，用于右侧媒资区展示（仅节有媒资） */
const selectedSectionId = ref<number | null>(null)
/** 媒资管理区是否展开文件列表（用于解绑等操作），默认展开让用户选择是否折叠 */
const mediaListExpanded = ref(true)
/** 文档 4.2 / 31.3：当页预览 - 当前预览的 fileId、url；弹窗展示不缩在抽屉内 */
const previewFileId = ref<string | null>(null)
const previewUrl = ref<string | null>(null)
const previewLoading = ref(false)
/** 预览时的媒资项（用于类型判断，避免关闭抽屉后 getSelectedMediaList 为空） */
const previewMediaItem = ref<{ mediaFileName?: string; mediaType?: string } | null>(null)
/** 31.4：MD 预览渲染后的 HTML（fetch 文本 + marked 解析，避免 iframe 触发下载） */
const previewMdHtml = ref<string | null>(null)

// 师资
const teachers = ref<CourseTeacherVO[]>([])
const teacherForm = ref<CourseTeacherDTO>({ teacherName: '', position: '', description: '' })
const teacherEditId = ref<number | null>(null)

const isAdd = computed(() => !courseId.value)

const coverPicsArray = computed(() => {
  const s = baseForm.value.coverPics
  if (!s?.trim()) return []
  try {
    const arr = JSON.parse(s)
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
})

/** 从三级分类 id（如 1-001-001）推导大分类（1-001），兼容数据库存错的情况 */
function deriveMtSt(mt: string, st: string): { mt: string; st: string } {
  if (!mt) return { mt: '', st: st || '' }
  const parts = mt.split('-')
  if (parts.length >= 3) {
    return { mt: parts.slice(0, 2).join('-'), st: mt }
  }
  return { mt, st: st || '' }
}

async function loadCourse() {
  if (!courseId.value) return
  loading.value = true
  try {
    const c = await getCourse(courseId.value)
    const { mt, st } = deriveMtSt(c.mt || '', c.st || '')
    let coverPics = c.coverPics || ''
    let defaultCoverIndex = c.defaultCoverIndex ?? 0
    if (!coverPics && c.pic) {
      coverPics = JSON.stringify([c.pic])
      defaultCoverIndex = 0
    }
    baseForm.value = {
      name: c.name,
      users: c.users,
      tags: c.tags,
      mt,
      st,
      grade: c.grade,
      teachMode: c.teachMode,
      description: c.description,
      pic: c.pic,
      coverPics,
      defaultCoverIndex,
    }
  } catch (e) {
    alert((e as Error)?.message || '加载失败')
    router.push('/manage/courses')
  } finally {
    loading.value = false
  }
}

async function loadMarket() {
  if (!courseId.value) return
  try {
    const m = await getCourseMarket(courseId.value)
    if (m) {
      marketForm.value = {
        charge: m.charge ?? '201000',
        price: m.price ?? 0,
        originalPrice: m.originalPrice ?? 0,
        qq: m.qq ?? '',
        wechat: m.wechat ?? '',
        phone: m.phone ?? '',
        validDays: m.validDays ?? 365,
      }
    }
  } catch {
    // ignore
  }
}

async function loadTeachplans() {
  if (!courseId.value) return
  try {
    const tree = await listTeachplans(courseId.value)
    teachplanTree.value = tree ?? []
  } catch {
    teachplanTree.value = []
  }
}

async function loadTeachers() {
  if (!courseId.value) return
  try {
    teachers.value = (await listTeachers(courseId.value)) ?? []
  } catch {
    teachers.value = []
  }
}

async function loadCategories() {
  try {
    categoryTree.value = (await getCategoryTree()) ?? []
  } catch {
    categoryTree.value = []
  }
}

function flattenCategories(nodes: CourseCategoryTreeVO[]): CourseCategoryTreeVO[] {
  const out: CourseCategoryTreeVO[] = []
  for (const n of nodes) {
    out.push(n)
    if (n.childrenTreeNodes?.length) {
      out.push(...flattenCategories(n.childrenTreeNodes))
    }
  }
  return out
}

const stOptions = computed(() => {
  const mt = baseForm.value.mt
  if (!mt) return []
  const all = flattenCategories(categoryTree.value)
  const children = all.filter((c) => c.parentId === mt)
  if (children.length) return children
  const mtNode = all.find((c) => c.id === mt)
  if (mtNode && mtNode.isLeaf) return [{ ...mtNode }]
  return []
})

async function saveBase() {
  if (!baseForm.value.name?.trim()) {
    alert('请填写课程名称')
    return
  }
  saving.value = true
  try {
    if (isAdd.value) {
      const id = await createCourse(baseForm.value)
      await router.replace(`/courses/${id}/edit`)
    } else {
      await updateCourse(courseId.value!, baseForm.value)
    }
    step.value = 2
  } catch (e) {
    alert((e as Error)?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const PHONE_REG = /^1[3-9]\d{9}$/

async function saveMarket() {
  if (!courseId.value) return
  const { qq, wechat, phone } = marketForm.value
  if (!qq?.trim()) {
    message.warning('咨询QQ不能为空')
    return
  }
  if (!wechat?.trim()) {
    message.warning('微信不能为空')
    return
  }
  if (!phone?.trim()) {
    message.warning('手机号不能为空')
    return
  }
  if (!PHONE_REG.test(phone.trim())) {
    message.warning('手机号需为11位，1开头，第二位3-9')
    return
  }
  saving.value = true
  try {
    await saveCourseMarket(courseId.value, marketForm.value)
    message.success('保存成功')
    step.value = 3
  } catch (e) {
    message.error((e as Error)?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function getChapterIds(nodes: TeachplanTreeVO[]): { id: number; pname: string }[] {
  const out: { id: number; pname: string }[] = []
  for (const n of nodes) {
    if ((n.grade || 1) === 1) {
      out.push({ id: n.id, pname: n.pname || '' })
    }
    if (n.children?.length) {
      out.push(...getChapterIds(n.children))
    }
  }
  return out
}

function findSectionById(nodes: TeachplanTreeVO[], id: number): TeachplanTreeVO | null {
  for (const n of nodes) {
    if (n.id === id) return n
    if (n.children?.length) {
      const found = findSectionById(n.children, id)
      if (found) return found
    }
  }
  return null
}

function getSectionName(sectionId: number): string {
  const node = findSectionById(teachplanTree.value, sectionId)
  return node?.pname ?? ''
}

function getSelectedMediaList() {
  if (!selectedSectionId.value) return []
  const node = findSectionById(teachplanTree.value, selectedSectionId.value)
  return node?.mediaList ?? []
}

async function addTeachplan() {
  if (!courseId.value || !teachplanForm.value.pname?.trim()) {
    alert('请填写计划名称')
    return
  }
  const grade = teachplanForm.value.grade ?? 1
  const parentId = grade === 2 ? (teachplanForm.value.parentId || 0) : 0
  if (grade === 2 && !parentId) {
    alert('添加节时请选择所属章')
    return
  }
  saving.value = true
  try {
    await createTeachplan(courseId.value, {
      pname: teachplanForm.value.pname,
      parentId,
      grade,
    })
    teachplanForm.value = { pname: '', parentId: 0, grade: 1 }
    await loadTeachplans()
  } catch (e) {
    alert((e as Error)?.message || '添加失败')
  } finally {
    saving.value = false
  }
}

const TITLE_MAX_LEN = 30

/** 在指定章下添加子节（仅章节点可用），插入到该章子节列表末尾 */
async function addChildTeachplan(node: TeachplanTreeVO) {
  if (!courseId.value) return
  const name = prompt(`请输入小节名称（最多${TITLE_MAX_LEN}字）`)
  if (!name?.trim()) return
  if (name.trim().length > TITLE_MAX_LEN) {
    alert(`名称最多${TITLE_MAX_LEN}字`)
    return
  }
  saving.value = true
  try {
    await createTeachplan(courseId.value, {
      pname: name.trim(),
      parentId: node.id,
      grade: 2,
    })
    await loadTeachplans()
  } catch (e) {
    alert((e as Error)?.message || '添加失败')
  } finally {
    saving.value = false
  }
}

/** 在指定节点后添加同级章节，插入到正确位置（如 4.1 后则新节点在 4.1 与 4.2 之间）
 * @param siblingIndex 可选，当前节点在兄弟列表中的索引，用于计算插入位置（不依赖接口返回的 orderBy）
 */
async function addSiblingTeachplan(node: TeachplanTreeVO, siblingIndex?: number) {
  if (!courseId.value) return
  const grade = (node.grade || 1) === 1 ? 1 : 2
  const label = grade === 1 ? '章' : '节'
  const name = prompt(`请输入新${label}名称（最多${TITLE_MAX_LEN}字）`)
  if (!name?.trim()) return
  if (name.trim().length > TITLE_MAX_LEN) {
    alert(`名称最多${TITLE_MAX_LEN}字`)
    return
  }
  const parentId = grade === 1 ? 0 : (node.parentId ?? 0)
  // 优先用 siblingIndex 计算插入位置，避免接口未返回 orderBy 导致插入到末尾
  const insertOrder = siblingIndex != null ? siblingIndex + 2 : (node.orderBy ?? 0) + 1
  saving.value = true
  try {
    await createTeachplan(courseId.value, {
      pname: name.trim(),
      parentId,
      grade,
      orderBy: insertOrder,
    })
    await loadTeachplans()
  } catch (e) {
    alert((e as Error)?.message || '添加失败')
  } finally {
    saving.value = false
  }
}

async function editTeachplan(node: TeachplanTreeVO) {
  const name = prompt(`请输入新名称（最多${TITLE_MAX_LEN}字）`, node.pname || '')
  if (name == null || name === node.pname) return
  if (!name.trim()) {
    alert('名称不能为空')
    return
  }
  if (name.trim().length > TITLE_MAX_LEN) {
    alert(`名称最多${TITLE_MAX_LEN}字`)
    return
  }
  saving.value = true
  try {
    await updateTeachplan(node.id, {
      pname: name.trim(),
      parentId: node.parentId ?? 0,
      grade: node.grade ?? 1,
      orderBy: node.orderBy,
    })
    await loadTeachplans()
  } catch (e) {
    alert((e as Error)?.message || '修改失败')
  } finally {
    saving.value = false
  }
}

async function removeTeachplan(id: number) {
  if (!confirm('确定删除该计划吗？')) return
  try {
    await deleteTeachplan(id)
    await loadTeachplans()
  } catch (e) {
    alert((e as Error)?.message || '删除失败')
  }
}

async function moveUp(id: number) {
  try {
    await moveTeachplanUp(id)
    await loadTeachplans()
  } catch (e) {
    alert((e as Error)?.message || '上移失败')
  }
}

async function moveDown(id: number) {
  try {
    await moveTeachplanDown(id)
    await loadTeachplans()
  } catch (e) {
    alert((e as Error)?.message || '下移失败')
  }
}

async function saveTeacher() {
  if (!courseId.value || !teacherForm.value.teacherName?.trim()) {
    alert('请填写教师姓名')
    return
  }
  saving.value = true
  try {
    if (teacherEditId.value) {
      await updateTeacher(courseId.value, teacherEditId.value, teacherForm.value)
      teacherEditId.value = null
    } else {
      await createTeacher(courseId.value, teacherForm.value)
    }
    teacherForm.value = { teacherName: '', position: '', description: '' }
    await loadTeachers()
  } catch (e) {
    alert((e as Error)?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function editTeacher(t: CourseTeacherVO) {
  teacherForm.value = {
    teacherName: t.teacherName,
    position: t.position,
    description: t.description,
    photograph: t.photograph,
  }
  teacherEditId.value = t.id
}

async function removeTeacher(id: number) {
  if (!courseId.value || !confirm('确定删除该教师吗？')) return
  try {
    await deleteTeacher(courseId.value, id)
    await loadTeachers()
  } catch (e) {
    alert((e as Error)?.message || '删除失败')
  }
}

function goBack() {
  router.push('/manage/courses')
}

function goToStep4() {
  if (!teachplanTree.value?.length) {
    message.warning('教学计划不能为空，请至少添加一章或一节后再进行下一步')
    return
  }
  step.value = 4
}

/**
 * 保存并下一步（静默保存当前步骤，然后进入下一步）
 */
async function saveAndNext(currentStep: number) {
  saving.value = true
  try {
    if (currentStep === 1) {
      // 保存基本信息
      if (isAdd.value) {
        const id = await createCourse(baseForm.value)
        await router.replace(`/courses/${id}/edit`)
      } else {
        await updateCourse(courseId.value!, baseForm.value)
      }
    } else if (currentStep === 2) {
      // 保存营销信息
      if (courseId.value) {
        await saveCourseMarket(courseId.value, marketForm.value)
      }
    }
    step.value = currentStep + 1
  } catch (e) {
    message.error((e as Error)?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

/**
 * Step 4: 保存草稿
 */
async function saveTeachersAndDraft() {
  saving.value = true
  try {
    // 教师信息已经在saveTeacher中保存了，这里只是确认保存
    message.success('已保存草稿')
  } catch (e) {
    message.error((e as Error)?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

/**
 * Step 4: 提交审核
 */
async function saveTeachersAndSubmit() {
  await doSubmit()
}

async function doSubmit() {
  if (!courseId.value) return
  if (!teachplanTree.value?.length) {
    message.warning('教学计划不能为空，请至少添加一章或一节后再提交')
    return
  }
  try {
    await submitCourse(courseId.value)
    const role = authStore.user?.role ?? ''
    message.success(role === 'super_admin' ? '已提交并发布' : '已提交，待管理员审核')
    await loadCourse()
  } catch (e) {
    message.error((e as Error)?.message || '提交失败')
  }
}

function doPreview() {
  if (!courseId.value) return
  // 使用前端路由，避免后端 Thymeleaf 预览 403
  window.open(`/courses/${courseId.value}/preview`, '_blank')
}

async function onTeachplanMediaSuccess(teachplanId: number, fileId: string, fileName: string) {
  try {
    const ext = fileName.split('.').pop()?.toLowerCase()
    const mediaType = ['mp4', 'avi', 'mov', 'webm'].includes(ext || '') ? 'video' : 'doc'
    await bindMedia(teachplanId, { fileId, mediaFileName: fileName, mediaType })
    message.success('媒资关联成功')

    // 31.4：如果是视频文件，获取时长并更新到后端
    if (mediaType === 'video') {
      fetchAndUpdateVideoDuration(teachplanId, fileId)
    }

    // 31.4：检测不支持的格式（如AVI），自动转码
    // 后端会自动：转码MP4、更新关联、删除原AVI文件
    if (isUnsupportedVideoFormat(fileName)) {
      try {
        message.loading({
          content: `正在将 ${ext?.toUpperCase()} 格式转码为 MP4，大文件可能需要较长时间...`,
          key: 'transcode',
          duration: 0
        })
        await transcodeToMp4(fileId)
        message.success({ content: `转码完成，原AVI文件已自动删除`, key: 'transcode', duration: 3 })
        await loadTeachplans()
      } catch (e: any) {
        message.error({
          content: `转码失败：${e.response?.data?.msg || e.message || '未知错误'}`,
          key: 'transcode',
          duration: 5
        })
      }
    }

    await loadTeachplans()
  } catch (e) {
    message.error((e as Error)?.message || '关联失败')
  }
}

/**
 * 31.4：获取视频时长并更新到后端
 * 通过创建隐藏的video元素获取duration
 */
async function fetchAndUpdateVideoDuration(teachplanId: number, fileId: string) {
  try {
    const info = await getFileInfo(fileId)
    if (!info?.url) return

    // 创建隐藏的video元素获取时长
    const video = document.createElement('video')
    video.preload = 'metadata'
    video.src = info.url

    video.addEventListener('loadedmetadata', () => {
      if (video.duration && video.duration > 0 && Number.isFinite(video.duration)) {
        const duration = Math.round(video.duration)
        updateMediaDuration(teachplanId, fileId, duration).catch(() => {
          // 静默失败，不影响主流程
        })
      }
    })

    // 设置超时，避免长时间等待
    setTimeout(() => {
      if (video.readyState < 1) {
        video.src = ''
      }
    }, 10000)
  } catch {
    // 静默失败
  }
}

async function unbindTeachplanMedia(teachplanId: number, fileId: string) {
  try {
    await unbindMediaByFileId(teachplanId, fileId)
    message.success('已解绑')
    if (previewFileId.value === fileId) closePreview()
    await loadTeachplans()
  } catch (e) {
    message.error((e as Error)?.message || '解绑失败')
  }
}

/** 打开第一个可预览媒资（用于媒资摘要的播放按钮） */
function openFirstPreview() {
  const list = getSelectedMediaList()
  const fid = list[0]?.fileId ?? list[0]?.mediaId
  if (fid) openPreview(fid)
}

/** 获取当前预览对应的媒资项（用于弹窗内类型判断） */
function getPreviewMediaItem(): { mediaFileName?: string; mediaType?: string } | null {
  return previewMediaItem.value ?? getSelectedMediaList().find(m => (m.fileId ?? m.mediaId) === previewFileId.value) ?? null
}

/** 文档 4.2 / 31.3：打开视频/文档预览，弹窗展示（不缩在抽屉内） */
async function openPreview(fileId: string) {
  const item = getSelectedMediaList().find(m => (m.fileId ?? m.mediaId) === fileId)
  previewMediaItem.value = item ?? null
  previewFileId.value = fileId
  previewUrl.value = null
  previewMdHtml.value = null
  previewLoading.value = true
  try {
    const info = await getFileInfo(fileId)
    previewUrl.value = info?.url ?? null
    if (!previewUrl.value) {
      message.warning('暂无预览地址')
      return
    }
    // 31.4：MD 文件用后端接口获取文本 + marked 渲染，弹窗内直接预览，避免 iframe 触发下载
    if (item && isMdFile(item)) {
      try {
        const text = await getFileContent(fileId)
        previewMdHtml.value = text ? (marked.parse(text) as string) : null
      } catch (e) {
        console.warn('MD content load failed:', e)
        previewMdHtml.value = null
      }
    }
  } catch (e) {
    message.error((e as Error)?.message || '获取预览地址失败')
    closePreview()
  } finally {
    previewLoading.value = false
  }
}

function closePreview() {
  previewFileId.value = null
  previewUrl.value = null
  previewMediaItem.value = null
  previewMdHtml.value = null
}

/**
 * 切换章的试看状态。以章为试看单元：章设为试看则其所有子节均为试看。
 * 会递归更新该章及其所有子节的 isPreviewEnabled。
 */
async function togglePreview(node: TeachplanTreeVO) {
  if (!courseId.value) return
  const newStatus = node.isPreviewEnabled === '1' ? '0' : '1'
  const nodesToUpdate: TeachplanTreeVO[] = [node]
  if (node.children?.length) {
    const collect = (n: TeachplanTreeVO) => {
      nodesToUpdate.push(n)
      n.children?.forEach(collect)
    }
    node.children.forEach(collect)
  }
  try {
    for (const n of nodesToUpdate) {
      await updateTeachplan(n.id, {
        pname: n.pname || '',
        parentId: n.parentId ?? 0,
        grade: n.grade ?? 1,
        orderBy: n.orderBy,
        isPreviewEnabled: newStatus,
      })
      n.isPreviewEnabled = newStatus
    }
    message.success(newStatus === '1' ? '已设为试看（整章及子节）' : '已取消试看')
  } catch (e) {
    message.error((e as Error)?.message || '操作失败')
  }
}

function isVideoFile(m: { mediaFileName?: string; mediaType?: string }): boolean {
  const ext = (m.mediaFileName ?? '').split('.').pop()?.toLowerCase()
  const videoExts = ['mp4', 'avi', 'mov', 'webm', 'mkv']
  return videoExts.includes(ext || '') || (m.mediaType ?? '').toLowerCase() === 'video'
}

function isPdfFile(m: { mediaFileName?: string }): boolean {
  return (m.mediaFileName ?? '').toLowerCase().endsWith('.pdf')
}

function isMdFile(m: { mediaFileName?: string }): boolean {
  const ext = (m.mediaFileName ?? '').split('.').pop()?.toLowerCase()
  return ext === 'md' || ext === 'markdown'
}

/** 计算媒资列表总时长（秒），多视频时求和 */
function getTotalDurationSec(mediaList: { duration?: number }[]): number {
  if (!mediaList?.length) return 0
  return mediaList.reduce((sum, m) => sum + (m.duration ?? 0), 0)
}

/** 格式化时长为 MM:SS */
function formatDuration(sec: number): string {
  if (sec <= 0) return '—'
  const m = Math.floor(sec / 60)
  const s = Math.floor(sec % 60)
  return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}

watch(selectedSectionId, () => {
  closePreview()
  // 切换小节时保持用户之前的展开/收起偏好，不重置
})

onMounted(async () => {
  await loadCategories()
  if (courseId.value) {
    await loadCourse()
    await loadMarket()
    await loadTeachplans()
    await loadTeachers()
  }
})
</script>

<template>
  <div class="course-edit-page">
    <div class="container">
      <div class="header">
        <button class="btn-back" @click="goBack">← 返回</button>
        <h1 class="page-title">{{ isAdd ? '添加课程' : '编辑课程' }}</h1>
      </div>

      <div v-if="loading" class="loading">加载中...</div>
      <template v-else>
        <!-- 步骤条 -->
        <div class="steps">
          <button
            v-for="s in 4"
            :key="s"
            class="step-btn"
            :class="{ active: step === s }"
            @click="step = s"
          >
            <span class="step-icon">{{ ['📋', '💰', '📑', '👨‍🏫'][s - 1] }}</span>
            {{ ['基本信息', '课程营销', '课程大纲', '师资管理'][s - 1] }}
          </button>
        </div>

        <!-- Step 1: 基本信息 -->
        <div v-show="step === 1" class="form-section">
          <div class="form-row">
            <label>课程名称 *</label>
            <input v-model="baseForm.name" placeholder="请输入课程名称" />
          </div>
          <div class="form-row">
            <label>大分类</label>
            <select v-model="baseForm.mt" class="form-select">
              <option value="">请选择大分类</option>
              <option v-for="c in categoryTree" :key="c.id" :value="c.id">
                {{ c.name }}
              </option>
            </select>
          </div>
          <div class="form-row">
            <label>小分类</label>
            <select v-model="baseForm.st" class="form-select">
              <option value="">请选择小分类</option>
              <option v-for="c in stOptions" :key="c.id" :value="c.id">
                {{ c.name }}
              </option>
            </select>
          </div>
          <div class="form-row">
            <label>适用人群</label>
            <input v-model="baseForm.users" placeholder="如：零基础学员" />
          </div>
          <div class="form-row">
            <label>课程标签</label>
            <input v-model="baseForm.tags" placeholder="如：Java,Spring" />
          </div>
          <div class="form-row">
            <label>课程介绍</label>
            <textarea v-model="baseForm.description" rows="4" placeholder="课程简介" />
          </div>
          <div class="form-row">
            <label>课程图片（最多3张）</label>
            <CoursePicUpload
              :cover-pics="coverPicsArray"
              :default-cover-index="baseForm.defaultCoverIndex ?? 0"
              @update:cover-pics="baseForm.coverPics = $event?.length ? JSON.stringify($event) : ''"
              @update:default-cover-index="baseForm.defaultCoverIndex = $event"
            />
          </div>
          <div class="form-actions-bar">
            <div class="actions-left"><button class="btn-cancel" @click="goBack">取消</button></div>
            <div class="actions-center">
              <button class="btn-save" :disabled="saving" @click="saveBase">{{ saving ? '保存中...' : '保存草稿' }}</button>
              <button v-if="courseId" class="btn-preview" @click="doPreview">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 4 8 11 8 11 8-4 8-11-8z"/><path d="M10 8v4"/><path d="M10 16h4"/></svg>
                预览
              </button>
              <button class="btn-next" :disabled="saving" @click="saveAndNext(1)">下一步</button>
            </div>
            <div v-if="courseId" class="actions-right"><button class="btn-submit" @click="doSubmit">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 19l7-7 3 3-7-7-7-3 3 7 7z"/><polyline points="19 5 12 12 5 19"/></svg>
              提交审核
            </button></div>
          </div>
        </div>

        <!-- Step 2: 课程营销 -->
        <div v-show="step === 2" class="form-section">
          <div class="form-row">
            <label>收费规则</label>
            <select v-model="marketForm.charge">
              <option value="201000">免费</option>
              <option value="201001">收费</option>
            </select>
          </div>
          <div v-if="marketForm.charge === '201001'" class="form-row">
            <label>价格</label>
            <input v-model.number="marketForm.price" type="number" placeholder="现价" />
          </div>
          <div v-if="marketForm.charge === '201001'" class="form-row">
            <label>原价</label>
            <input v-model.number="marketForm.originalPrice" type="number" placeholder="原价" />
          </div>
          <div class="form-row">
            <label>有效期(天)</label>
            <input v-model.number="marketForm.validDays" type="number" />
          </div>
          <div class="form-row">
            <label>咨询QQ</label>
            <input v-model="marketForm.qq" />
          </div>
          <div class="form-row">
            <label>微信</label>
            <input v-model="marketForm.wechat" />
          </div>
          <div class="form-row">
            <label>电话</label>
            <input v-model="marketForm.phone" />
          </div>
          <div class="form-actions-bar">
            <div class="actions-left"><button class="btn-cancel" @click="goBack">取消</button></div>
            <div class="actions-center">
              <button class="btn-save" :disabled="saving" @click="saveMarket">{{ saving ? '保存中...' : '保存草稿' }}</button>
              <button v-if="courseId" class="btn-preview" @click="doPreview">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 4 8 11 8 11 8-4 8-11-8z"/><path d="M10 8v4"/><path d="M10 16h4"/></svg>
                预览
              </button>
              <button class="btn-next" :disabled="saving" @click="saveAndNext(2)">下一步</button>
            </div>
            <div v-if="courseId" class="actions-right"><button class="btn-submit" @click="doSubmit">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 19l7-7 3 3-7-7-7-3 3 7 7z"/><polyline points="19 5 12 12 5 19"/></svg>
              提交审核
            </button></div>
          </div>
        </div>

        <!-- Step 3: 课程大纲（文档 4.1：章节树全宽，媒资以抽屉拉出） -->
        <div v-show="step === 3" class="form-section">
          <div class="teachplan-full">
            <div class="teachplan-add">
              <input v-model="teachplanForm.pname" placeholder="计划名称" maxlength="30" />
                <select v-model="teachplanForm.grade">
                  <option :value="1">章</option>
                  <option :value="2">节</option>
                </select>
                <select v-if="teachplanForm.grade === 2" v-model="teachplanForm.parentId">
                  <option :value="0">选择所属章</option>
                  <option v-for="ch in getChapterIds(teachplanTree)" :key="ch.id" :value="ch.id">
                    {{ ch.pname }}
                  </option>
                </select>
                <button :disabled="saving" @click="addTeachplan">添加</button>
              </div>
              <div class="teachplan-tree">
                <template v-for="(node, chIdx) in teachplanTree" :key="node.id">
                  <div class="teachplan-node teachplan-chapter" :style="{ paddingLeft: (node.grade || 1) * 12 + 'px' }">
                    <div class="node-inline-actions">
                      <button type="button" class="btn-inline-add" title="添加子节" @click="addChildTeachplan(node)">
                        + 子节
                      </button>
                      <button type="button" class="btn-inline-add" title="添加同级章" @click="addSiblingTeachplan(node, chIdx)">
                        + 同级
                      </button>
                      <!-- 试看：以章为单位，章设为试看则其所有子节均为试看 -->
                      <label class="preview-toggle" title="试看开关（整章及其子节）" @click.stop>
                        <input
                          type="checkbox"
                          :checked="node.isPreviewEnabled === '1'"
                          @change="togglePreview(node)"
                        />
                        <span class="preview-label">试看</span>
                      </label>
                    </div>
                    <span class="node-label" :title="node.pname">{{ node.pname }}</span>
                    <div class="node-actions">
                      <button @click="editTeachplan(node)">编辑</button>
                      <button @click="moveUp(node.id)">上移</button>
                      <button @click="moveDown(node.id)">下移</button>
                      <button
                        class="danger"
                        :disabled="(node.grade === 1 && node.children?.length) || false"
                        :title="node.grade === 1 && node.children?.length ? '该章下有子节，请先删除子节' : ''"
                        @click="removeTeachplan(node.id)"
                      >
                        删除
                      </button>
                    </div>
                  </div>
                  <template v-if="node.children?.length">
                    <div
                      v-for="(child, secIdx) in node.children"
                      :key="child.id"
                      class="teachplan-node teachplan-section"
                      :class="{ selected: selectedSectionId === child.id }"
                      :style="{ paddingLeft: (child.grade || 2) * 12 + 'px' }"
                      @click="selectedSectionId = child.id"
                    >
                      <div class="node-inline-actions">
                        <button type="button" class="btn-inline-add" title="添加同级节" @click.stop="addSiblingTeachplan(child, secIdx)">
                          + 同级
                        </button>
                      </div>
                      <span class="node-label" :title="child.pname">{{ child.pname }}</span>
                      <div class="node-actions" @click.stop>
                        <button @click="editTeachplan(child)">编辑</button>
                        <button @click="moveUp(child.id)">上移</button>
                        <button @click="moveDown(child.id)">下移</button>
                        <button class="danger" @click="removeTeachplan(child.id)">删除</button>
                      </div>
                    </div>
                  </template>
                </template>
              </div>
            </div>
          <!-- 媒资管理抽屉：选择小节后从右侧拉出 -->
          <a-drawer
            :open="!!selectedSectionId"
            title="媒资管理"
            placement="right"
            :width="440"
            :get-container="false"
            :z-index="1000"
            @close="selectedSectionId = null"
          >
            <template v-if="selectedSectionId">
              <div class="media-drawer-body">
                <div class="media-panel-header">
                  <span class="muted">{{ getSectionName(selectedSectionId) }}</span>
                </div>
                <!-- 媒资摘要：播放图标 + 总时长 -->
                <div v-if="getSelectedMediaList().length" class="media-summary">
                  <button type="button" class="media-play-btn" title="预览" @click="openFirstPreview()">
                    <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
                  </button>
                  <span class="media-duration">{{ formatDuration(getTotalDurationSec(getSelectedMediaList())) }}</span>
                  <a class="media-manage-link" @click="mediaListExpanded = !mediaListExpanded">
                    {{ mediaListExpanded ? '收起' : '展开' }}
                  </a>
                </div>
                <!-- 已上传文件列表，默认展开，用户可折叠 -->
                <div v-if="mediaListExpanded && getSelectedMediaList().length" class="media-list-manage">
                  <div v-for="m in getSelectedMediaList()" :key="m.fileId ?? m.mediaId" class="media-item">
                    <span class="media-name">{{ m.mediaFileName || m.fileId }}</span>
                    <div class="media-item-actions">
                      <a-button size="small" @click="openPreview((m.fileId ?? m.mediaId)!)">预览</a-button>
                      <a-button size="small" danger @click="unbindTeachplanMedia(selectedSectionId, (m.fileId ?? m.mediaId)!)">解绑</a-button>
                    </div>
                  </div>
                </div>
                <MediaUpload
                  accept="video/*,.mp4,.pdf,.doc,.docx,.md,.markdown"
                  :max-count="5"
                  list-type="text"
                  tip="视频/文档"
                  @success="(fid, fn) => onTeachplanMediaSuccess(selectedSectionId, fid, fn)"
                />
              </div>
            </template>
          </a-drawer>
          <div class="form-actions-bar">
            <div class="actions-left"><button class="btn-cancel" @click="goBack">取消</button></div>
            <div class="actions-center">
              <button class="btn-save" @click="goToStep4">保存草稿</button>
              <button v-if="courseId" class="btn-preview" @click="doPreview">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 4 8 11 8 11 8-4 8-11-8z"/><path d="M10 8v4"/><path d="M10 16h4"/></svg>
                预览
              </button>
              <button class="btn-next" @click="goToStep4">下一步</button>
            </div>
            <div v-if="courseId" class="actions-right"><button class="btn-submit" @click="doSubmit">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 19l7-7 3 3-7-7-7-3 3 7 7z"/><polyline points="19 5 12 12 5 19"/></svg>
              提交审核
            </button></div>
          </div>
        </div>

        <!-- Step 4: 师资管理 -->
        <div v-show="step === 4" class="form-section">
          <div class="teacher-form">
            <input v-model="teacherForm.teacherName" placeholder="教师姓名 *" />
            <input v-model="teacherForm.position" placeholder="职位" />
            <textarea v-model="teacherForm.description" placeholder="简介" rows="2" />
            <button :disabled="saving" @click="saveTeacher">
              {{ teacherEditId ? '更新' : '添加' }}</button>
          </div>
          <div class="teacher-list">
            <div v-for="t in teachers" :key="t.id" class="teacher-item">
              <span>{{ t.teacherName }}</span>
              <span v-if="t.position" class="muted">{{ t.position }}</span>
              <div class="teacher-actions">
                <button @click="editTeacher(t)">编辑</button>
                <button class="danger" @click="removeTeacher(t.id)">删除</button>
              </div>
            </div>
          </div>
          <div class="form-actions-bar">
            <div class="actions-left"><button class="btn-cancel" @click="goBack">取消</button></div>
            <div class="actions-center">
              <button class="btn-save" @click="saveTeachersAndDraft">保存草稿</button>
              <button v-if="courseId" class="btn-preview" @click="doPreview">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 4 8 11 8 11 8-4 8-11-8z"/><path d="M10 8v4"/><path d="M10 16h4"/></svg>
                预览
              </button>
            </div>
            <div class="actions-right"><button class="btn-submit" @click="saveTeachersAndSubmit">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 19l7-7 3 3-7-7-7-3 3 7 7z"/><polyline points="19 5 12 12 5 19"/></svg>
              提交审核
            </button></div>
          </div>
        </div>
      </template>
    </div>

    <!-- 31.3：同页面预览弹窗（视频/PDF/MD 不缩在抽屉内） -->
    <a-modal
      :open="!!previewFileId"
      title="媒体预览"
      :width="800"
      :footer="null"
      @cancel="closePreview"
    >
      <div v-if="previewLoading" class="preview-loading">加载中...</div>
      <template v-else-if="previewUrl && previewFileId">
        <div v-if="getPreviewMediaItem() && isVideoFile(getPreviewMediaItem()!)" class="preview-modal-body">
          <video :src="previewUrl" controls class="preview-video-full" />
        </div>
        <div v-else-if="getPreviewMediaItem() && isPdfFile(getPreviewMediaItem()!)" class="preview-modal-body">
          <div class="preview-doc-header">
            <span>PDF 文档</span>
            <a :href="previewUrl" target="_blank" rel="noopener">在新窗口打开</a>
          </div>
          <iframe :src="previewUrl" class="preview-iframe-full" title="PDF预览" />
        </div>
        <div v-else-if="getPreviewMediaItem() && isMdFile(getPreviewMediaItem()!)" class="preview-modal-body">
          <div class="preview-doc-header">
            <span>Markdown 文档</span>
            <a :href="previewUrl" target="_blank" rel="noopener">在新窗口打开</a>
          </div>
          <!-- 31.4：fetch 文本 + marked 渲染，弹窗内直接预览，避免 iframe 触发下载 -->
          <div v-if="previewMdHtml" class="preview-md-content markdown-body" v-html="previewMdHtml" />
          <div v-else class="preview-md-fallback">
            <p>无法在弹窗内加载预览（可能受 CORS 限制），请点击上方「在新窗口打开」查看。</p>
          </div>
        </div>
        <div v-else class="preview-modal-body">
          <a :href="previewUrl" target="_blank" rel="noopener">在新窗口打开</a>
        </div>
      </template>
    </a-modal>
  </div>
</template>

<style scoped>
.course-edit-page {
  padding: var(--space-6) var(--space-4);
}

.container {
  max-width: 960px;
  margin: 0 auto;
}

.header {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin-bottom: var(--space-6);
  flex-wrap: wrap;
}

.teachplan-media-block {
  flex: 1 1 100%;
  margin-top: var(--space-2);
  padding: var(--space-3);
  background: var(--color-bg);
  border-radius: var(--radius-sm);
}

.media-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-2) 0;
  border-bottom: 1px solid var(--color-border);
}

.media-item:last-of-type {
  border-bottom: none;
}

.media-name {
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.btn-back {
  padding: var(--space-3) var(--space-4);
  font-size: 1rem;
  background: transparent;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  color: var(--color-text);
  cursor: pointer;
  transition: all var(--transition-base);
}

.btn-back:hover {
  border-color: var(--color-accent);
  color: var(--color-accent);
}

.page-title {
  font-size: 1.5rem;
  margin: 0;
}

.steps {
  display: flex;
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}

.step-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-5);
  font-size: 1rem;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  color: var(--color-text-muted);
  cursor: pointer;
  transition: all var(--transition-base);
}

.step-icon {
  font-size: 1.2rem;
  line-height: 1;
}

.step-btn.active {
  border-color: var(--color-accent);
  color: var(--color-accent);
  background: rgba(214, 158, 46, 0.08);
}

.form-section {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-6);
}

.form-row {
  margin-bottom: var(--space-5);
}

.form-row label {
  display: block;
  margin-bottom: var(--space-2);
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text);
}

.form-row input,
.form-row select,
.form-row textarea {
  width: 100%;
  padding: var(--space-3) var(--space-4);
  font-size: 15px;
  font-family: inherit;
  line-height: 1.5;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-bg-card);
  color: var(--color-text);
}

/* 课程介绍框：强制与 input 字体一致，避免浏览器默认差异 */
.form-row textarea {
  font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, sans-serif;
  font-size: 15px;
  line-height: 1.6;
  resize: vertical;
}

/* 按钮布局：取消左、提交审核右（防误触），中间为保存草稿/预览/下一步 */
.form-actions-bar {
  display: flex;
  flex-wrap: nowrap;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-4);
  margin-top: var(--space-6);
  padding: var(--space-4) 0;
}

.actions-left,
.actions-right {
  flex: 0 0 auto;
  min-width: 100px;
}

.actions-left {
  justify-content: flex-start;
}

.actions-right {
  justify-content: flex-end;
}

.actions-center {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  flex: 1;
}

/* 取消：左侧，颜色较轻，避免误触提交 */
.btn-cancel {
  padding: var(--space-3) var(--space-5);
  font-size: 0.9rem;
  font-weight: 400;
  background: transparent;
  color: var(--color-text-muted);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-base);
}

.btn-cancel:hover {
  border-color: var(--color-text-muted);
  color: var(--color-text);
}

.btn-preview {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-5);
  font-size: 0.95rem;
  font-weight: 500;
  background: transparent;
  color: var(--color-accent);
  border: 1px solid var(--color-accent);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-base);
}

.btn-preview:hover {
  background: rgba(214, 158, 46, 0.1);
}

/* 保存草稿：中间，次要强调 */
.btn-save {
  padding: var(--space-3) var(--space-5);
  font-size: 0.95rem;
  font-weight: 500;
  background: var(--color-bg-card);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-base);
}

.btn-save:hover:not(:disabled) {
  border-color: var(--color-accent);
  color: var(--color-accent);
}

.btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 提交审核：右侧，着重颜色强调 */
.btn-submit {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-6);
  font-size: 1rem;
  font-weight: 600;
  background: var(--color-accent);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-base);
  box-shadow: 0 2px 8px rgba(214, 158, 46, 0.35);
}

.btn-submit:hover {
  background: var(--color-accent-hover);
  box-shadow: 0 4px 12px rgba(214, 158, 46, 0.45);
}

/* 下一步：中间，深色填充 */
.btn-next {
  padding: var(--space-3) var(--space-5);
  font-size: 0.95rem;
  font-weight: 500;
  background: #334155;
  color: white;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-base);
}

.btn-next:hover:not(:disabled) {
  background: #475569;
}

.btn-next:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 文档 4.1：章节树全宽，媒资以抽屉拉出 */
.teachplan-full {
  width: 100%;
  min-width: 0;
}

.media-drawer-body {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.media-panel {
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-4);
}

.media-panel-header {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  margin-bottom: var(--space-3);
  font-weight: 500;
}

.media-panel-header .muted {
  font-size: 0.875rem;
  font-weight: 400;
  color: var(--color-text-muted);
}

.media-summary {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) 0;
  margin-bottom: var(--space-2);
}

.media-play-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  padding: 0;
  background: var(--color-accent);
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  transition: all var(--transition-base);
}

.media-play-btn:hover {
  background: var(--color-accent-hover);
  transform: scale(1.05);
}

.media-play-btn svg {
  margin-left: 2px;
}

.media-duration {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-text);
}

.media-manage-link {
  margin-left: auto;
  font-size: 0.875rem;
  color: var(--color-accent);
  cursor: pointer;
}

.media-manage-link:hover {
  text-decoration: underline;
}

.media-list-manage {
  margin-top: var(--space-2);
  padding-top: var(--space-2);
  border-top: 1px dashed var(--color-border);
}

.media-panel-placeholder {
  padding: var(--space-6);
  text-align: center;
  color: var(--color-text-muted);
  background: var(--color-bg);
  border: 1px dashed var(--color-border);
  border-radius: var(--radius-md);
}

.media-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-2) 0;
  border-bottom: 1px solid var(--color-border);
  gap: var(--space-2);
}

.media-item .media-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.media-item-actions {
  display: flex;
  gap: var(--space-1);
  flex-shrink: 0;
}

/* 文档 4.2：当页预览 */
.preview-block {
  margin-top: var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-2) var(--space-3);
  background: var(--color-bg);
  border-bottom: 1px solid var(--color-border);
}

.preview-loading {
  padding: var(--space-6);
  text-align: center;
  color: var(--color-text-muted);
}

.preview-video {
  width: 100%;
  max-height: 360px;
  display: block;
}

.preview-iframe {
  width: 100%;
  height: 400px;
  border: none;
}

.preview-doc {
  padding: var(--space-4);
  text-align: center;
}

.preview-doc a {
  color: var(--color-accent);
}

/* 31.3：同页面预览弹窗样式 */
.preview-modal-body {
  min-height: 400px;
  display: flex;
  flex-direction: column;
}

.preview-video-full {
  width: 100%;
  max-height: 70vh;
  display: block;
}

.preview-pdf-header,
.preview-doc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-2);
}

.preview-pdf-header a,
.preview-doc-header a {
  color: var(--color-accent);
}

.preview-iframe-full {
  width: 100%;
  height: 70vh;
  min-height: 400px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
}

/* 31.4：MD 预览 - fetch+marked 渲染样式 */
.preview-md-content {
  width: 100%;
  max-height: 70vh;
  min-height: 400px;
  overflow: auto;
  padding: var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: #fff;
}

.preview-md-content :deep(h1) { font-size: 1.5rem; margin: 0.5em 0; }
.preview-md-content :deep(h2) { font-size: 1.25rem; margin: 0.5em 0; }
.preview-md-content :deep(h3) { font-size: 1.1rem; margin: 0.5em 0; }
.preview-md-content :deep(p) { margin: 0.5em 0; line-height: 1.6; }
.preview-md-content :deep(ul), .preview-md-content :deep(ol) { margin: 0.5em 0; padding-left: 1.5em; }
.preview-md-content :deep(code) { background: #f5f5f5; padding: 2px 6px; border-radius: 4px; font-size: 0.9em; }
.preview-md-content :deep(pre) { background: #f5f5f5; padding: var(--space-3); overflow-x: auto; border-radius: 4px; }
.preview-md-content :deep(pre code) { background: none; padding: 0; }
.preview-md-content :deep(blockquote) { border-left: 4px solid var(--color-accent); margin: 0.5em 0; padding-left: 1em; color: #666; }
.preview-md-content :deep(a) { color: var(--color-accent); }
.preview-md-content :deep(table) { border-collapse: collapse; width: 100%; }
.preview-md-content :deep(th), .preview-md-content :deep(td) { border: 1px solid var(--color-border); padding: 6px 10px; }

.preview-md-fallback {
  padding: var(--space-6);
  text-align: center;
  color: var(--color-text-muted);
}

.teachplan-add {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

.teachplan-add input {
  flex: 1;
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
}

.teachplan-tree {
  margin-bottom: var(--space-4);
}

.teachplan-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-2) 0;
  border-bottom: 1px solid var(--color-border);
  gap: var(--space-3);
}

.teachplan-section {
  cursor: pointer;
  transition: background var(--transition-base);
}

.teachplan-section:hover {
  background: rgba(214, 158, 46, 0.06);
}

.teachplan-section.selected {
  background: rgba(214, 158, 46, 0.12);
  border-left: 3px solid var(--color-accent);
}


.node-inline-actions {
  display: flex;
  gap: var(--space-1);
  flex-shrink: 0;
}

.btn-inline-add {
  padding: 2px 8px;
  font-size: 13px;
  font-weight: 500;
  background: var(--color-accent);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all var(--transition-base);
}

.btn-inline-add:hover {
  background: var(--color-accent-hover);
}

/* 试看开关样式 */
.preview-toggle {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-left: 8px;
  padding: 2px 8px;
  font-size: 12px;
  cursor: pointer;
  user-select: none;
}

.preview-toggle input[type="checkbox"] {
  cursor: pointer;
}

.preview-label {
  font-size: 12px;
  color: var(--color-text-muted);
}

/* 章节标题：单行显示，充分利用横向空间 */
.node-label {
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
}

.node-actions button,
.teacher-actions button {
  margin-left: var(--space-1);
  padding: 4px 10px;
  font-size: 13px;
  background: transparent;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  cursor: pointer;
  transition: all var(--transition-base);
}

.node-actions button.danger,
.teacher-actions button.danger {
  border-color: #dc3545;
  color: #dc3545;
}

.teacher-form {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

.teacher-form input,
.teacher-form textarea {
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
}

.teacher-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.teacher-form input,
.teacher-form textarea {
  width: 100%;
}

.teacher-list {
  margin-bottom: var(--space-4);
}

.teacher-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2) 0;
  border-bottom: 1px solid var(--color-border);
}

.teacher-item .muted {
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.loading {
  text-align: center;
  padding: var(--space-8);
  color: var(--color-text-muted);
}
</style>
