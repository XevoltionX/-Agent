<template>
  <div class="login-page">
    <NavHeader :showBack="true" backTo="/" />

    <div class="login-body">
      <!-- LOGO -->
      <div class="login-logo">
        <img src="/favicon.png" width="56" height="56" alt="高翠" />
      </div>

      <!-- 标题 -->
      <h2 class="login-title">商家入驻</h2>
      <p class="login-subtitle">加入高翠网 · 获取精准买家线索</p>

      <!-- 邮箱输入框 -->
      <div class="input-group">
        <svg class="input-icon-svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--text-hint)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"/></svg>
        <input v-model="email" class="login-input" placeholder="请输入您的邮箱地址" type="email" />
      </div>

      <!-- 发送验证码按钮 -->
      <button class="send-code-btn" :disabled="countdown > 0" @click="sendCode">发送验证码</button>

      <!-- 验证码输入框 -->
      <div class="input-group">
        <svg class="input-icon-svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--text-hint)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
        <input v-model="code" class="login-input" placeholder="请输入验证码" />
        <span v-if="countdown > 0" class="countdown-num">{{ countdown }}s</span>
      </div>

      <!-- 登录/注册按钮 -->
      <button class="login-btn" @click="handleLogin">登录 / 注册</button>

      <!-- 底部功能图标 -->
      <div class="features-row">
        <div class="feature-item">
          <div class="feature-icon-wrap">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--color-primary)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"/></svg>
          </div>
          <span>仅需邮箱</span>
        </div>
        <div class="feature-item">
          <div class="feature-icon-wrap">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--color-primary)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
          </div>
          <span>验证码登录</span>
        </div>
        <div class="feature-item">
          <div class="feature-icon-wrap">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--color-primary)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>
          </div>
          <span>快速入驻</span>
        </div>
        <div class="feature-item">
          <div class="feature-icon-wrap">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--color-primary)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
          </div>
          <span>免费试用</span>
        </div>
      </div>

      <!-- 协议 -->
      <p class="agreement-text">
        登录即表示同意<span class="link">《平台服务协议》</span>与<span class="link">《隐私政策》</span>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { showToast } from 'vant'
import NavHeader from '@/components/common/NavHeader.vue'
import { loginApi, sendCodeApi } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const email = ref('')
const code = ref('')
const countdown = ref(0)

const sendCode = async () => {
  if (!email.value.trim()) { showToast('请输入邮箱地址'); return }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) { showToast('邮箱格式不正确'); return }
  try {
    await sendCodeApi({ email: email.value, type: 'LOGIN' })
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
    showToast('验证码已发送')
  } catch (e) {
    showToast('发送失败')
  }
}

const handleLogin = async () => {
  if (!email.value.trim() || !code.value.trim()) { showToast('请填写完整信息'); return }
  if (!/^[0-9]{6}$/.test(code.value) && code.value !== 'PPPPPP') { showToast('验证码为6位数字'); return }
  try {
    const res = await loginApi({ email: email.value, code: code.value })
    const data = res.data
    authStore.token = data.token
    authStore.isLoggedIn = true
    authStore.isVip = data.isVip || false
    authStore.merchantEmail = data.email || email.value
    authStore.vipStartDate = data.vipStartDate || null
    authStore.vipEndDate = data.vipEndDate || null
    authStore.persist()
    showToast(data.isVip ? '欢迎回来，VIP商家！' : '入驻成功')
    const redirect = route.query.redirect || '/merchant/dashboard'
    router.replace(redirect)
  } catch (e) {
    showToast('登录失败，请检查验证码')
  }
}
</script>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: url('/src/assets/login-bg.png') center/cover no-repeat;
}
.login-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-6) var(--space-5);
  overflow-y: auto;
}

/* LOGO */
.login-logo {
  margin-top: var(--space-8);
}
.login-logo img {
  width: 56px;
  height: 56px;
  border-radius: 50%;
}

/* 标题 */
.login-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
  margin-top: var(--space-5);
  letter-spacing: 2px;
}
.login-subtitle {
  font-size: var(--font-sm);
  color: var(--text-hint);
  margin-top: var(--space-2);
}

/* 输入框 */
.input-group {
  width: 100%;
  display: flex;
  align-items: center;
  gap: var(--space-2);
  border: 1.5px solid #e5e7eb;
  border-radius: var(--radius-md);
  padding: 0 var(--space-3);
  margin-top: var(--space-4);
  background: var(--bg-gray-light);
  height: 48px;
  transition: border-color 0.2s;
}
.input-group:focus-within {
  border-color: var(--color-primary);
}
.login-input {
  flex: 1;
  height: 100%;
  border: none;
  background: transparent;
  font-size: var(--font-base);
  outline: none;
  font-family: inherit;
}
.login-input::placeholder {
  color: var(--text-hint);
}
.input-icon-svg {
  flex-shrink: 0;
}
.countdown-num {
  font-size: var(--font-sm);
  color: var(--text-hint);
  flex-shrink: 0;
}

/* 发送验证码按钮 */
.send-code-btn {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: var(--radius-md);
  background: var(--gradient-primary);
  color: white;
  font-size: var(--font-base);
  font-weight: 500;
  margin-top: var(--space-4);
  cursor: pointer;
  transition: background 0.2s;
}
.send-code-btn:active {
  background: var(--color-primary-dark);
}
.send-code-btn:disabled {
  background: #d1d5db;
  color: #9ca3af;
  cursor: not-allowed;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: var(--radius-md);
  background: var(--gradient-primary);
  color: white;
  font-size: var(--font-base);
  font-weight: 500;
  margin-top: var(--space-5);
  cursor: pointer;
  transition: background 0.2s;
}
.login-btn:active {
  background: var(--color-primary-dark);
}

/* 功能图标行 */
.features-row {
  display: flex;
  justify-content: space-around;
  width: 100%;
  margin-top: var(--space-5);
}
.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--font-xs);
  color: var(--text-secondary);
}
.feature-icon-wrap {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  background: white;
  border: 1.5px solid var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
}


/* 协议 */
.agreement-text {
  font-size: var(--font-xs);
  color: var(--text-hint);
  margin-top: var(--space-6);
  text-align: center;
  line-height: 1.6;
}
.link {
  color: var(--color-primary);
  cursor: pointer;
}
</style>
