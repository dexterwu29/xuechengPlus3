<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCourse, createCourse, updateCourse } from '@/api/content/course'
import { getCourseMarket, saveCourseMarket } from '@/api/content/courseMarket'
import { getCategoryTree } from '@/api/content/category'
import { listTeachplans, createTeachplan, updateTeachplan, deleteTeachplan, moveTeachplanUp, moveTeachplanDown } from '@/api/content/teachplan'
import { listTeachers, createTeacher, updateTeacher, deleteTeacher } from '@/api/content/courseTeacher'
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

// 师资
const teachers = ref<CourseTeacherVO[]>([])
const teacherForm = ref<CourseTeacherDTO>({ teacherName: '', position: '', description: '' })
const teacherEditId = ref<number | null>(null)

const isAdd = computed(() => !courseId.value)

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
    }
  } catch (e) {
    alert((e as Error)?.message || '加载失败')
    router.push('/courses')
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

async function saveMarket() {
  if (!courseId.value) return
  saving.value = true
  try {
    await saveCourseMarket(courseId.value, marketForm.value)
    step.value = 3
  } catch (e) {
    alert((e as Error)?.message || '保存失败')
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

/** 在指定章下添加子节（仅章节点可用），插入到该章子节列表末尾 */
async function addChildTeachplan(node: TeachplanTreeVO) {
  if (!courseId.value) return
  const name = prompt('请输入小节名称')
  if (!name?.trim()) return
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
  const name = prompt(`请输入新${label}名称`)
  if (!name?.trim()) return
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
  const name = prompt('请输入新名称', node.pname || '')
  if (name == null || name === node.pname) return
  if (!name.trim()) {
    alert('名称不能为空')
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
  router.push('/courses')
}

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
            <label>课程图片</label>
            <input v-model="baseForm.pic" placeholder="图片URL" />
          </div>
          <button class="btn-save" :disabled="saving" @click="saveBase">
            {{ saving ? '保存中...' : '保存并下一步' }}
          </button>
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
          <button class="btn-save" :disabled="saving" @click="saveMarket">
            {{ saving ? '保存中...' : '保存并下一步' }}
          </button>
        </div>

        <!-- Step 3: 课程大纲 -->
        <div v-show="step === 3" class="form-section">
          <div class="teachplan-add">
            <input v-model="teachplanForm.pname" placeholder="计划名称" />
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
              <div class="teachplan-node" :style="{ paddingLeft: (node.grade || 1) * 16 + 'px' }">
                <div class="node-inline-actions">
                  <button type="button" class="btn-inline-add" title="添加子节" @click="addChildTeachplan(node)">
                    + 子节
                  </button>
                  <button type="button" class="btn-inline-add" title="添加同级章" @click="addSiblingTeachplan(node, chIdx)">
                    + 同级
                  </button>
                </div>
                <span class="node-label">{{ node.pname }}</span>
                <div class="node-actions">
                  <button @click="editTeachplan(node)">编辑</button>
                  <button @click="moveUp(node.id)">上移</button>
                  <button @click="moveDown(node.id)">下移</button>
                  <button class="danger" @click="removeTeachplan(node.id)">删除</button>
                </div>
              </div>
              <template v-if="node.children?.length">
                <div
                  v-for="(child, secIdx) in node.children"
                  :key="child.id"
                  class="teachplan-node"
                  :style="{ paddingLeft: (child.grade || 2) * 16 + 'px' }"
                >
                  <div class="node-inline-actions">
                    <button type="button" class="btn-inline-add" title="添加同级节" @click="addSiblingTeachplan(child, secIdx)">
                      + 同级
                    </button>
                  </div>
                  <span class="node-label">{{ child.pname }}</span>
                  <div class="node-actions">
                    <button @click="editTeachplan(child)">编辑</button>
                    <button @click="moveUp(child.id)">上移</button>
                    <button @click="moveDown(child.id)">下移</button>
                    <button class="danger" @click="removeTeachplan(child.id)">删除</button>
                  </div>
                </div>
              </template>
            </template>
          </div>
          <button class="btn-save" @click="step = 4">下一步</button>
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
          <button class="btn-save" @click="goBack">完成</button>
        </div>
      </template>
    </div>
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

.btn-save {
  margin-top: var(--space-5);
  padding: var(--space-3) var(--space-6);
  font-size: 1rem;
  font-weight: 500;
  background: var(--color-accent);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-base);
}

.btn-save:hover:not(:disabled) {
  background: var(--color-accent-hover);
}

.btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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

.node-label {
  flex: 1;
  min-width: 0;
  word-break: break-word;
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
