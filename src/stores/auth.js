import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const isLoggedIn = ref(false)
  const isVip = ref(false)
  const merchantEmail = ref('')
  const vipStartDate = ref(null)
  const vipEndDate = ref(null)
  const token = ref(localStorage.getItem('gaocui_token') || '')

  // 从localStorage恢复登录态
  const restoreSession = () => {
    const saved = localStorage.getItem('gaocui_auth')
    if (saved) {
      try {
        const data = JSON.parse(saved)
        isLoggedIn.value = data.isLoggedIn || false
        isVip.value = data.isVip || false
        merchantEmail.value = data.merchantEmail || ''
        vipStartDate.value = data.vipStartDate || null
        vipEndDate.value = data.vipEndDate || null
        token.value = data.token || ''
      } catch (e) { /* ignore parse error */ }
    }
  }

  // 持久化到localStorage
  const persist = () => {
    localStorage.setItem('gaocui_token', token.value)
    localStorage.setItem('gaocui_auth', JSON.stringify({
      isLoggedIn: isLoggedIn.value,
      isVip: isVip.value,
      merchantEmail: merchantEmail.value,
      vipStartDate: vipStartDate.value,
      vipEndDate: vipEndDate.value,
      token: token.value
    }))
  }

  // VIP是否过期
  const isVipExpired = computed(() => {
    if (!isVip.value || !vipEndDate.value) return false
    return new Date(vipEndDate.value) < new Date()
  })

  // 有效VIP状态（未过期才算VIP）
  const isEffectiveVip = computed(() => isVip.value && !isVipExpired.value)

  // Mock模式：模拟登录
  const loginMock = (email, isVipUser = false) => {
    token.value = 'mock_token_' + Date.now() + (isVipUser ? '_vip' : '')
    isLoggedIn.value = true
    isVip.value = isVipUser
    merchantEmail.value = email
    if (isVipUser) {
      const start = new Date()
      const end = new Date()
      end.setFullYear(end.getFullYear() + 1)
      vipStartDate.value = start.toISOString().split('T')[0]
      vipEndDate.value = end.toISOString().split('T')[0]
    } else {
      vipStartDate.value = null
      vipEndDate.value = null
    }
    persist()
  }

  // 登出
  const logout = () => {
    token.value = ''
    isLoggedIn.value = false
    isVip.value = false
    merchantEmail.value = ''
    vipStartDate.value = null
    vipEndDate.value = null
    localStorage.removeItem('gaocui_token')
    localStorage.removeItem('gaocui_auth')
  }

  return { isLoggedIn, isVip, merchantEmail, vipStartDate, vipEndDate, token, isVipExpired, isEffectiveVip, restoreSession, persist, loginMock, logout }
})
