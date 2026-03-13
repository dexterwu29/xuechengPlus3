<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loading = ref(false)
const formState = reactive({
  username: '',
  password: '',
})

async function onFinish() {
  if (!formState.username.trim() || !formState.password) {
    message.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await authStore.login(formState.username.trim(), formState.password)
    message.success('登录成功')
    const redirect = (route.query.redirect as string) || '/courses'
    router.replace(redirect)
  } catch (e) {
    message.error((e as Error)?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card-wrap">
      <a-card title="学成在线 - 登录" class="login-card">
        <a-form
          :model="formState"
          layout="vertical"
          @finish="onFinish"
        >
          <a-form-item
            label="用户名"
            name="username"
            :rules="[{ required: true, message: '请输入用户名' }]"
          >
            <a-input
              v-model:value="formState.username"
              placeholder="如 teacher1_org1"
              size="large"
              allow-clear
            />
          </a-form-item>
          <a-form-item
            label="密码"
            name="password"
            :rules="[{ required: true, message: '请输入密码' }]"
          >
            <a-input-password
              v-model:value="formState.password"
              placeholder="请输入密码"
              size="large"
            />
          </a-form-item>
          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              block
              :loading="loading"
            >
              登录
            </a-button>
          </a-form-item>
        </a-form>
        <p class="login-hint">测试账号：teacher1_org1 / password</p>
      </a-card>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-4);
  background: linear-gradient(135deg, var(--color-bg) 0%, var(--color-primary-light) 100%);
}

.login-card-wrap {
  width: 100%;
  max-width: 400px;
}

.login-card :deep(.ant-card-head-title) {
  font-size: 1.25rem;
  font-weight: 600;
}

.login-hint {
  margin-top: var(--space-4);
  font-size: 0.875rem;
  color: var(--color-text-muted);
  text-align: center;
}
</style>
