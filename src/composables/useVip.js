import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { showToast } from 'vant'

export function useVip() {
  const auth = useAuthStore()

  const isVip = computed(() => auth.isVip)

  const vipLimits = computed(() => ({
    maxProducts: isVip.value ? 100 : 2,
    canViewFullEmail: isVip.value,
    maskEmail: (email) => {
      if (!email) return '****@***.com'
      const atIndex = email.indexOf('@')
      if (atIndex <= 0) return '****@***.com'
      return email.charAt(0) + '****@' + email.slice(atIndex + 1)
    }
  }))

  const checkPublishLimit = (currentCount) => {
    const max = vipLimits.value.maxProducts
    if (currentCount >= max) {
      const msg = isVip.value ? '请下架部分商品后再发布' : '需升级VIP提升发布额度'
      showToast(msg)
      return false
    }
    return true
  }

  return { isVip, vipLimits, checkPublishLimit }
}
