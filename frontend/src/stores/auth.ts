/**
 * 认证状态
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as apiLogin, type LoginVO } from '@/api/auth'

const TOKEN_KEY = 'token'
const COMPANY_KEY = 'companyId'
const USER_KEY = 'user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem(TOKEN_KEY))
  const user = ref<LoginVO | null>((() => {
    try {
      const s = localStorage.getItem(USER_KEY)
      return s ? (JSON.parse(s) as LoginVO) : null
    } catch {
      return null
    }
  })())

  const isLoggedIn = computed(() => !!token.value)

  function setAuth(vo: LoginVO) {
    token.value = vo.token
    user.value = vo
    localStorage.setItem(TOKEN_KEY, vo.token)
    localStorage.setItem(COMPANY_KEY, vo.companyId != null ? String(vo.companyId) : '1')
    localStorage.setItem(USER_KEY, JSON.stringify(vo))
  }

  function clearAuth() {
    token.value = null
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(COMPANY_KEY)
    localStorage.removeItem(USER_KEY)
  }

  async function login(username: string, password: string): Promise<LoginVO> {
    const vo = await apiLogin(username, password)
    setAuth(vo)
    return vo
  }

  function logout() {
    clearAuth()
  }

  return { token, user, isLoggedIn, login, logout, setAuth, clearAuth }
})
