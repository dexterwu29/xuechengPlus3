<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import {
  pageArchives,
  createArchive,
  updateArchive,
  deleteArchive,
  approveArchive,
  rejectArchive,
} from '@/api/content/archive'
import type { ArchiveVO, ArchiveDTO } from '@/api/content/archive'
import { message, Modal } from 'ant-design-vue'

const route = useRoute()
const list = ref<ArchiveVO[]>([])
const total = ref(0)
const pageNo = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const archiveType = ref<string>((route.query.type as string) || '')
const status = ref<string>('')
const modalVisible = ref(false)
const modalMode = ref<'add' | 'edit'>('add')
const editingId = ref<number | null>(null)
const form = ref<ArchiveDTO>({
  name: '',
  archiveType: 'org',
  phone: '',
  wechat: '',
  qq: '',
  idCard: '',
  licenseNo: '',
})

const TYPE_OPTIONS = [
  { value: '', label: '全部' },
  { value: 'org', label: '机构' },
  { value: 'teacher', label: '讲师' },
  { value: 'sales', label: '销售' },
]
const STATUS_OPTIONS = [
  { value: '', label: '全部' },
  { value: 'pending', label: '待审核' },
  { value: 'approved', label: '已通过' },
  { value: 'rejected', label: '已拒绝' },
]

const pageTitle = computed(() => {
  const t = archiveType.value
  if (t === 'org') return '机构备案'
  if (t === 'teacher') return '讲师备案'
  if (t === 'sales') return '销售备案'
  return '备案管理'
})

function resetForm() {
  form.value = {
    name: '',
    archiveType: archiveType.value || 'org',
    phone: '',
    wechat: '',
    qq: '',
    idCard: '',
    licenseNo: '',
  }
}

async function loadList() {
  loading.value = true
  try {
    const res = await pageArchives({
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      archiveType: archiveType.value || undefined,
      status: status.value || undefined,
    })
    list.value = (res?.items ?? []) as ArchiveVO[]
    total.value = Number(res?.total ?? 0)
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function openAdd() {
  modalMode.value = 'add'
  editingId.value = null
  resetForm()
  form.value.archiveType = archiveType.value || 'org'
  modalVisible.value = true
}

function openEdit(row: ArchiveVO) {
  modalMode.value = 'edit'
  editingId.value = row.id
  form.value = {
    name: row.name,
    archiveType: row.archiveType,
    phone: row.phone ?? '',
    wechat: row.wechat ?? '',
    qq: row.qq ?? '',
    idCard: row.idCard ?? '',
    licenseNo: row.licenseNo ?? '',
  }
  modalVisible.value = true
}

async function handleSubmit() {
  if (!form.value.name?.trim()) {
    message.error('请输入名称')
    return
  }
  try {
    if (modalMode.value === 'add') {
      await createArchive({ ...form.value, archiveType: form.value.archiveType || archiveType.value || 'org' })
      message.success('添加成功')
    } else if (editingId.value) {
      await updateArchive(editingId.value, form.value)
      message.success('更新成功')
    }
    modalVisible.value = false
    loadList()
  } catch (e) {
    message.error((e as Error)?.message || '操作失败')
  }
}

async function handleDelete(row: ArchiveVO) {
  Modal.confirm({
    title: '确认删除',
    content: `确定删除「${row.name}」吗？`,
    onOk: async () => {
      try {
        await deleteArchive(row.id)
        message.success('删除成功')
        loadList()
      } catch (e) {
        message.error((e as Error)?.message || '删除失败')
      }
    },
  })
}

async function handleApprove(row: ArchiveVO) {
  try {
    await approveArchive(row.id)
    message.success('已通过')
    loadList()
  } catch (e) {
    message.error((e as Error)?.message || '操作失败')
  }
}

async function handleReject(row: ArchiveVO) {
  try {
    await rejectArchive(row.id)
    message.success('已拒绝')
    loadList()
  } catch (e) {
    message.error((e as Error)?.message || '操作失败')
  }
}

function statusTag(s: string) {
  if (s === 'approved') return 'success'
  if (s === 'rejected') return 'error'
  return 'warning'
}

function statusText(s: string) {
  if (s === 'approved') return '已通过'
  if (s === 'rejected') return '已拒绝'
  return '待审核'
}

watch(
  () => route.query.type,
  (v) => {
    archiveType.value = v || ''
    pageNo.value = 1
    loadList()
  }
)

onMounted(() => {
  loadList()
})
</script>

<template>
  <div class="archive-page">
    <div class="container">
      <h1 class="page-title">{{ pageTitle }}</h1>
      <div class="toolbar">
        <a-space>
          <a-select v-model:value="archiveType" placeholder="类型" style="width: 120px" @change="loadList">
            <a-select-option v-for="o in TYPE_OPTIONS" :key="o.value" :value="o.value">
              {{ o.label }}
            </a-select-option>
          </a-select>
          <a-select v-model:value="status" placeholder="状态" style="width: 120px" @change="loadList">
            <a-select-option v-for="o in STATUS_OPTIONS" :key="o.value" :value="o.value">
              {{ o.label }}
            </a-select-option>
          </a-select>
          <a-button type="primary" @click="openAdd">新增备案</a-button>
        </a-space>
      </div>
      <a-table
        :columns="[
          { title: 'ID', dataIndex: 'id', width: 80 },
          { title: '类型', dataIndex: 'archiveType', width: 80 },
          { title: '名称', dataIndex: 'name' },
          { title: '联系电话', dataIndex: 'phone', width: 120 },
          { title: '状态', dataIndex: 'status', width: 100 },
          { title: '操作', key: 'action', width: 200 },
        ]"
        :data-source="list"
        :loading="loading"
        :pagination="{
          current: pageNo,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (t: number) => `共 ${t} 条`,
          onChange: (p: number, s: number) => {
            pageNo.value = p
            pageSize.value = s
            loadList()
          },
        }"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'archiveType'">
            {{ record.archiveType === 'org' ? '机构' : record.archiveType === 'teacher' ? '讲师' : '销售' }}
          </template>
          <template v-else-if="column.dataIndex === 'status'">
            <a-tag :color="statusTag(record.status)">{{ statusText(record.status) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a @click="openEdit(record)">编辑</a>
              <a v-if="record.status === 'pending'" @click="handleApprove(record)">通过</a>
              <a v-if="record.status === 'pending'" @click="handleReject(record)">拒绝</a>
              <a-popconfirm title="确定删除？" @confirm="handleDelete(record)">
                <a class="danger">删除</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <a-modal
      v-model:open="modalVisible"
      :title="modalMode === 'add' ? '新增备案' : '编辑备案'"
      @ok="handleSubmit"
    >
      <a-form layout="vertical">
        <a-form-item label="类型" v-if="modalMode === 'add'">
          <a-select v-model:value="form.archiveType" placeholder="选择类型">
            <a-select-option value="org">机构</a-select-option>
            <a-select-option value="teacher">讲师</a-select-option>
            <a-select-option value="sales">销售</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="名称" required>
          <a-input v-model:value="form.name" placeholder="请输入名称" />
        </a-form-item>
        <a-form-item label="联系电话">
          <a-input v-model:value="form.phone" placeholder="手机号" />
        </a-form-item>
        <a-form-item label="微信">
          <a-input v-model:value="form.wechat" placeholder="微信号" />
        </a-form-item>
        <a-form-item label="QQ">
          <a-input v-model:value="form.qq" placeholder="QQ号" />
        </a-form-item>
        <a-form-item label="身份证号（讲师）">
          <a-input v-model:value="form.idCard" placeholder="身份证号" />
        </a-form-item>
        <a-form-item label="营业执照号（机构）">
          <a-input v-model:value="form.licenseNo" placeholder="营业执照号" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.archive-page {
  padding: var(--space-6) var(--space-4);
}
.container {
  max-width: 960px;
  margin: 0 auto;
}
.page-title {
  font-size: 1.5rem;
  margin-bottom: var(--space-4);
}
.toolbar {
  margin-bottom: var(--space-4);
}
.danger {
  color: var(--color-error, #ff4d4f);
}
</style>
