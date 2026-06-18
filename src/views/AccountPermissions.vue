<template>
  <div class="account-page">
    <NavHeader title="账户权限" backTo="/merchant/profile">
      <template #right>
        <span v-if="auth.isVip" class="nav-vip-tag">VIP</span>
      </template>
    </NavHeader>

    <div class="account-scroll">
      <!-- 会员信息卡 -->
      <div class="member-card" :class="auth.isVip ? 'vip-card' : 'free-card'">
        <div class="card-left">
          <div class="card-icon">👑</div>
        </div>
        <div class="card-info">
          <div class="card-title">{{ auth.isVip ? 'VIP会员' : '普通会员' }}</div>
          <div class="card-expiry">{{ auth.isVip ? '有效期至 ' + (auth.vipEndDate || '--') : '有效期至 永久' }}</div>
        </div>
        <button class="card-btn" @click="$router.push('/merchant/account')">查看权益</button>
      </div>

      <!-- 当前权限 -->
      <div class="section">
        <h3 class="section-title">当前权限</h3>
        <div class="perm-table">
          <div class="perm-row">
            <span class="perm-label">商品发布上限</span>
            <span class="perm-value">{{ auth.isVip ? '100/100件' : '2/2件' }}</span>
          </div>
          <div class="perm-row">
            <span class="perm-label">今日已发布</span>
            <span class="perm-value">{{ auth.isVip ? '36件' : '0件' }}</span>
          </div>
          <div class="perm-row">
            <span class="perm-label">客资查看权限</span>
            <span class="perm-value">{{ auth.isVip ? '无限查看全部' : '无查看权限' }}</span>
          </div>
          <div class="perm-row">
            <span class="perm-label">优先展示权重</span>
            <span class="perm-value">{{ auth.isVip ? '高' : '低' }}</span>
          </div>
        </div>
      </div>

      <!-- 升级/续费 -->
      <div class="section">
        <h3 class="section-title">{{ auth.isVip ? '续费' : '升级VIP' }}</h3>
        <div class="price-list">
          <div class="price-row">
            <div class="price-info">
              <div class="price-name">VIP会员（12个月）</div>
              <div class="price-desc">全年畅享所有VIP权益</div>
            </div>
            <div class="price-num">￥2999</div>
          </div>
          <div class="price-row">
            <div class="price-info">
              <div class="price-name">VIP会员（6个月）</div>
              <div class="price-desc">半年体验VIP服务</div>
            </div>
            <div class="price-num">￥1688</div>
          </div>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div class="bottom-fixed">
        <button class="contact-btn" @click="showPay = true">
          {{ auth.isVip ? '联系运营续费' : '联系运营升级' }}
        </button>
      </div>
    </div>

    <!-- 支付弹窗 -->
    <van-dialog v-model:show="showPay" title="选择VIP方案" show-cancel-button confirmButtonText=" " cancelButtonText="关闭">
      <div class="pay-dialog">
        <div class="pay-option" @click="doPay('12month'); showPay = false">
          <div class="pay-name">VIP会员（12个月）</div>
          <div class="pay-price">￥2999</div>
        </div>
        <div class="pay-option" @click="doPay('6month'); showPay = false">
          <div class="pay-name">VIP会员（6个月）</div>
          <div class="pay-price">￥1688</div>
        </div>
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { showToast } from 'vant'
import NavHeader from '@/components/common/NavHeader.vue'
import api from '@/api/index'

const auth = useAuthStore()
const showPay = ref(false)

const doPay = async (planType) => {
  try {
    showToast('正在创建订单...')
    const res = await api.post('/alipay/create', { planType })
    const orderNo = res.data?.orderNo
    if (orderNo) {
      window.open('/api/alipay/pay?orderNo=' + orderNo)
    }
  } catch (e) {
    showToast('创建订单失败')
  }
}
</script>

<style scoped>
.account-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.account-scroll {
  flex: 1;
  overflow-y: auto;
}

/* 导航VIP标签 */
.nav-vip-tag {
  font-size: 11px;
  font-weight: 700;
  color: white;
  background: var(--color-vip-bg);
  padding: 2px 8px;
  border-radius: var(--radius-full);
}

/* 会员卡 */
.member-card {
  margin: var(--space-4);
  padding: var(--space-4);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  gap: var(--space-3);
}
.vip-card {
  background: linear-gradient(135deg, #f59e0b, #fbbf24, #fcd34d);
  color: white;
}
.free-card {
  background: linear-gradient(135deg, #9ca3af, #d1d5db, #e5e7eb);
  color: var(--text-primary);
}
.card-left {
  flex-shrink: 0;
}
.card-icon {
  font-size: 32px;
}
.card-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.card-title {
  font-size: var(--font-base);
  font-weight: 700;
}
.card-expiry {
  font-size: var(--font-sm);
  opacity: 0.85;
}
.card-btn {
  flex-shrink: 0;
  padding: 6px 14px;
  border: 1.5px solid rgba(0,0,0,0.2);
  border-radius: 4px;
  background: rgba(255,255,255,0.7);
  color: var(--text-primary);
  font-size: var(--font-sm);
  font-weight: 500;
  cursor: pointer;
  white-space: nowrap;
}
.free-card .card-btn {
  background: rgba(255,255,255,0.5);
}

/* 权限区域 */
.section {
  margin: var(--space-3) var(--space-4);
}
.section-title {
  font-size: var(--font-base);
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--space-2);
}
.perm-table {
  border-top: 1px solid #e5e7eb;
}
.perm-row {
  display: flex;
  justify-content: space-between;
  padding: var(--space-3) 0;
  border-bottom: 1px solid #f3f4f6;
  font-size: var(--font-sm);
}
.perm-label {
  color: var(--text-secondary);
}
.perm-value {
  color: var(--text-primary);
  font-weight: 500;
}

/* 价格列表 */
.price-list {
  border-top: 1px solid #e5e7eb;
}
.price-row {
  display: flex;
  align-items: center;
  padding: var(--space-3) 0;
  border-bottom: 1px solid #f3f4f6;
}
.price-info {
  flex: 1;
}
.price-name {
  font-size: var(--font-base);
  font-weight: 600;
  color: var(--text-primary);
}
.price-desc {
  font-size: var(--font-xs);
  color: var(--text-hint);
  margin-top: 2px;
}
.price-num {
  font-size: var(--font-lg);
  font-weight: 700;
  color: var(--text-primary);
}

/* 底部按钮 */
.bottom-fixed {
  padding: var(--space-4);
  padding-bottom: calc(var(--space-4) + var(--safe-bottom));
}
.contact-btn {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: var(--radius-md);
  background: var(--gradient-primary);
  color: white;
  font-size: var(--font-base);
  font-weight: 500;
  cursor: pointer;
}
.contact-btn:active {
  background: var(--color-primary-dark);
}
.pay-dialog {
  padding: var(--space-3) var(--space-4);
}
.pay-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3);
  border: 1.5px solid #e5e7eb;
  border-radius: var(--radius-md);
  margin-bottom: var(--space-2);
  cursor: pointer;
  transition: border-color 0.15s;
}
.pay-option:active {
  border-color: var(--color-primary);
}
.pay-name {
  font-size: var(--font-base);
  font-weight: 500;
  color: var(--text-primary);
}
.pay-price {
  font-size: var(--font-lg);
  font-weight: 700;
  color: var(--color-primary);
}
</style>
