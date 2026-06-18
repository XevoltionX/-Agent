<template>
  <div class="contact-seller">
    <h3 class="contact-title">联系卖家</h3>
    <p class="contact-hint">留下邮箱，卖家将通过邮件联系您</p>

    <input
      v-model="email"
      class="contact-input"
      placeholder="请输入您的联系邮箱"
      type="email"
    />

    <button class="contact-btn" :disabled="!email.trim()" @click="submit">提交意向，等待卖家联系</button>
    <p class="privacy-note">我们尊重您的隐私，仅用于卖家联系您</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import api from '@/api/index'

const router = useRouter()

const props = defineProps({
  productId: { type: Number, required: true },
  searchKeyword: { type: String, default: '' }  // 用户搜索词
})

const email = ref('')

const submit = async () => {
  if (!email.value.trim()) {
    showToast('请输入联系邮箱')
    return
  }
  try {
    await api.post('/inquiry', {
      productId: props.productId,
      email: email.value,
      message: props.searchKeyword || ''
    })
    showToast('意向已提交，卖家将尽快联系您')
    setTimeout(() => router.push('/'), 800)
  } catch (e) {
    showToast('提交失败，请稍后重试')
  }
}
</script>

<style scoped>
.contact-seller {
  padding: var(--space-4);
  background: var(--bg-white);
  border-top: 0.5px solid #f0f0f0;
  flex-shrink: 0;
}
.contact-title {
  font-size: var(--font-lg);
  color: var(--text-primary);
  margin-bottom: 2px;
}
.contact-hint {
  font-size: var(--font-sm);
  color: var(--text-hint);
  margin-bottom: var(--space-3);
}
.contact-input {
  width: 100%;
  border: 1.5px solid #e5e7eb;
  border-radius: var(--radius-md);
  padding: var(--space-3);
  font-size: var(--font-base);
  margin-bottom: var(--space-3);
  outline: none;
  font-family: inherit;
}
.contact-input:focus {
  border-color: var(--color-primary);
}
.contact-btn {
  width: 100%;
  height: 44px;
  border: none;
  border-radius: var(--radius-md);
  background: var(--gradient-primary);
  color: white;
  font-size: var(--font-base);
  font-weight: 500;
  cursor: pointer;
}
.contact-btn:disabled {
  background: #d1d5db;
  color: #9ca3af;
  cursor: not-allowed;
}
.contact-btn:active {
  background: var(--color-primary-dark);
}
.privacy-note {
  font-size: var(--font-xs);
  color: var(--text-hint);
  text-align: center;
  margin-top: var(--space-2);
}
</style>
