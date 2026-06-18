<template>
  <div class="profile-page">
    <NavHeader title="个人中心" backTo="/merchant/dashboard" />

    <div class="profile-scroll">
      <!-- 卖家信息 -->
      <div class="seller-card" @click="$router.push('/merchant/account')">
        <div class="seller-avatar">
          <svg width="40" height="40" viewBox="0 0 40 40"><circle cx="20" cy="20" r="20" fill="#e5e7eb"/><text x="20" y="26" text-anchor="middle" font-size="18">👤</text></svg>
        </div>
        <div class="seller-info">
          <div class="seller-email-row">
            <span class="seller-email">{{ auth.merchantEmail || 'seller@email.com' }}</span>
            <span v-if="auth.isVip" class="vip-member-badge">VIP会员</span>
          </div>
          <div class="seller-expiry">{{ auth.isVip ? '有效期至 ' + (auth.vipEndDate || '--') : '有效期至 永久' }}</div>
        </div>
      </div>

      <!-- 账户信息 -->
      <ExpandableSection title="账户信息" icon="user-o">
        <div v-if="auth.isVip" class="info-text">VIP起止日 {{ auth.vipStartDate || '--' }} ~ {{ auth.vipEndDate || '--' }}</div>
        <div v-else class="info-text">免费账户 — 永久有效</div>
      </ExpandableSection>

      <!-- 修改邮箱 -->
      <ExpandableSection title="修改邮箱" icon="comment-o">
        <div class="info-text mb-2">当前邮箱：{{ auth.merchantEmail || 'seller@email.com' }}</div>
        <div class="input-row">
          <input v-model="newEmail" class="field-input" placeholder="新邮箱地址" />
          <button class="code-btn">发送验证码</button>
        </div>
        <div class="input-row mt-2">
          <input v-model="verifyCode" class="field-input" placeholder="验证码" />
          <button class="save-btn" @click="saveEmail">保存</button>
        </div>
      </ExpandableSection>

      <!-- 通知设置 -->
      <ExpandableSection title="通知设置" icon="bell">
        <div class="checkbox-row">
          <van-checkbox v-model="notifyWeb" checked disabled>有人感兴趣时网页内通知我</van-checkbox>
        </div>
        <div class="checkbox-row mt-2">
          <van-checkbox v-model="notifyEmail">有人感兴趣时邮件通知我</van-checkbox>
        </div>
      </ExpandableSection>

      <!-- 帮助中心 -->
      <div class="plain-row" @click="onHold">
        <van-icon name="question-o" size="16" color="var(--text-hint)" class="row-icon" />
        <span>帮助中心</span>
        <van-icon name="arrow" size="14" color="var(--text-hint)" class="row-arrow" />
      </div>

      <!-- 关于我们 -->
      <div class="plain-row" @click="onHold">
        <van-icon name="info-o" size="16" color="var(--text-hint)" class="row-icon" />
        <span>关于我们</span>
        <van-icon name="arrow" size="14" color="var(--text-hint)" class="row-arrow" />
      </div>

      <!-- 退出登录 -->
      <div class="plain-row" @click="doLogout">
        <van-icon name="cross" size="16" color="var(--text-hint)" class="row-icon" />
        <span>退出登录</span>
        <van-icon name="arrow" size="14" color="var(--text-hint)" class="row-arrow" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { showToast } from 'vant'
import NavHeader from '@/components/common/NavHeader.vue'
import ExpandableSection from '@/components/merchant/ExpandableSection.vue'

const router = useRouter()
const auth = useAuthStore()

const newEmail = ref('')
const verifyCode = ref('')
const notifyWeb = ref(true)
const notifyEmail = ref(true)

const saveEmail = () => {
  showToast('邮箱修改成功（Mock）')
}

const onHold = () => {
  showToast('暂未开放')
}

const doLogout = () => {
  auth.logout()
  showToast('已退出登录')
  router.replace('/')
}
</script>

<style scoped>
.profile-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.profile-scroll {
  flex: 1;
  overflow-y: auto;
}

/* 卖家信息卡片 */
.seller-card {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  border-bottom: 8px solid #f5f5f5;
  cursor: pointer;
}
.seller-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid var(--color-primary-light);
}
.seller-info {
  flex: 1;
}
.seller-email-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}
.seller-email {
  font-size: var(--font-base);
  font-weight: 700;
  color: var(--text-primary);
}
.vip-member-badge {
  font-size: 10px;
  font-weight: 600;
  color: white;
  background: var(--color-vip-bg);
  padding: 1px 8px;
  border-radius: var(--radius-full);
  white-space: nowrap;
}
.seller-expiry {
  font-size: var(--font-xs);
  color: var(--text-hint);
  margin-top: 2px;
}

/* 通用行 */
.plain-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-4);
  border-bottom: 1px solid #f3f4f6;
  font-size: var(--font-base);
  color: var(--text-hint);
  cursor: pointer;
}
.row-icon { flex-shrink: 0; }
.row-arrow { margin-left: auto; }

/* 展开区域内 */
.info-text {
  font-size: var(--font-sm);
  color: var(--text-secondary);
}
.mb-2 { margin-bottom: var(--space-2); }
.mt-2 { margin-top: var(--space-2); }
.input-row {
  display: flex;
  gap: var(--space-2);
}
.field-input {
  flex: 1;
  height: 40px;
  border: 1.5px solid #e5e7eb;
  border-radius: var(--radius-sm);
  padding: 0 var(--space-2);
  font-size: var(--font-sm);
  outline: none;
  font-family: inherit;
}
.field-input:focus { border-color: var(--color-primary); }
.code-btn {
  height: 40px;
  padding: 0 var(--space-3);
  border: 1.5px solid var(--color-primary);
  border-radius: var(--radius-sm);
  background: var(--bg-white);
  color: var(--color-primary);
  font-size: var(--font-sm);
  cursor: pointer;
  white-space: nowrap;
}
.save-btn {
  height: 40px;
  padding: 0 var(--space-4);
  border: none;
  border-radius: var(--radius-sm);
  background: var(--color-primary);
  color: white;
  font-size: var(--font-sm);
  cursor: pointer;
}
.checkbox-row {
  padding: var(--space-1) 0;
}
</style>
