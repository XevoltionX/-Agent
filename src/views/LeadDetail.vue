<template>
  <div class="detail-page">
    <NavHeader title="客资详情" backTo="/merchant/leads">
      <template #right>
        <span class="nav-status" :class="lead?.status" v-if="lead">{{ lead.status === 'CONTACTED' ? '已联系' : '未联系' }}</span>
      </template>
    </NavHeader>

    <div class="detail-scroll" v-if="lead">
      <!-- 留言时间 -->
      <div class="info-row">
        <span class="info-label-bold">留言时间</span>
        <span class="info-time">{{ lead.created_at }}</span>
      </div>

      <!-- 用户需求原文 -->
      <div class="info-block">
        <span class="info-label-bold">用户需求原文</span>
        <p class="info-body">{{ lead.buyer_message }}</p>
      </div>

      <!-- 关联商品 -->
      <div class="info-row">
        <span class="info-label-bold">关联商品</span>
        <div class="product-mini" v-if="relatedProduct">
          <img :src="relatedProduct.images?.[0] || placeholderImg" class="product-mini-img" />
          <div class="product-mini-info">
            <div class="product-mini-title">{{ relatedProduct.title }}</div>
            <div class="product-mini-price">￥{{ formatPrice(relatedProduct.price) }}</div>
          </div>
        </div>
        <span v-else class="info-val">{{ lead.product_title }}</span>
      </div>

      <!-- 用户邮箱 -->
      <div class="info-row" style="flex-wrap:wrap">
        <span class="info-label-bold">用户邮箱</span>
        <span class="info-val">{{ auth.isVip ? lead.buyer_email : maskEmail(lead.buyer_email) }}</span>
        <p v-if="!auth.isVip" class="info-hint-inline">(VIP可见完整邮箱)</p>
      </div>

      <!-- 商家账号 -->
      <div class="info-row">
        <span class="info-label-bold">商家账号</span>
        <span class="info-val">{{ auth.merchantEmail || 'seller@email.com' }}</span>
      </div>
    </div>

    <div v-else class="empty-state">客资不存在</div>

    <!-- 底部按钮 -->
    <div class="bottom-fixed" v-if="lead">
      <template v-if="auth.isVip">
        <div class="btn-dual">
          <button class="copy-btn" @click="copyEmail">复制邮箱</button>
          <button class="contacted-btn" @click="markContacted" :disabled="lead.status === 'CONTACTED'">
            {{ lead.status === 'CONTACTED' ? '已标记已联系' : '标记已联系' }}
          </button>
        </div>
      </template>
      <template v-else>
        <button class="upgrade-btn" @click="$router.push('/merchant/account')">
          升级VIP，查看完整联系方式
        </button>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getLeadDetailApi, markContactedApi } from '@/api/leads'
import { getProductDetailApi } from '@/api/products'
import { showToast } from 'vant'
import NavHeader from '@/components/common/NavHeader.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const lead = ref(null)
const relatedProduct = ref(null)

const placeholderImg = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200"><rect fill="#f3f4f6" width="200" height="200"/></svg>')

const formatPrice = (p) => {
  if (!p && p !== 0) return '--'
  return Number(p).toLocaleString()
}

const maskEmail = (email) => {
  if (!email) return '****@***.com'
  const atIndex = email.indexOf('@')
  if (atIndex <= 0) return '****@***.com'
  return email.charAt(0) + '****@' + email.slice(atIndex + 1)
}

onMounted(async () => {
  try {
    const res = await getLeadDetailApi(route.params.id)
    lead.value = res.data
    // 获取关联商品详情
    if (lead.value?.product_id) {
      const prodRes = await getProductDetailApi(lead.value.product_id)
      relatedProduct.value = prodRes.data
    }
  } catch (e) { /* ignore */ }
})

const copyEmail = () => {
  if (lead.value?.buyer_email) {
    navigator.clipboard?.writeText(lead.value.buyer_email)
    showToast('邮箱已复制')
  }
}

const markContacted = async () => {
  try {
    await markContactedApi(lead.value.id)
    lead.value.status = 'CONTACTED'
    showToast('已标记为已联系')
  } catch (e) {
    showToast('操作失败')
  }
}
</script>

<style scoped>
.detail-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.detail-scroll {
  flex: 1;
  overflow-y: auto;
}

/* 导航栏右侧状态 */
.nav-status {
  font-size: var(--font-sm);
}
.nav-status.PENDING {
  color: var(--text-hint);
}
.nav-status.CONTACTED {
  color: #22c55e;
}

/* 信息行 */
.info-row {
  display: flex;
  align-items: flex-start;
  padding: var(--space-4);
  border-bottom: 1px solid #f8f8f8;
}
.info-time {
  font-size: var(--font-sm);
  color: var(--text-hint);
  margin-left: auto;
  margin-right: var(--space-1);
}

/* 信息块（标签上，内容下） */
.info-block {
  padding: var(--space-3) var(--space-4);
  border-bottom: 1px solid #f8f8f8;
}
.info-body {
  font-size: var(--font-base);
  color: var(--text-primary);
  line-height: 1.6;
  margin-top: var(--space-2);
}

/* 标签样式 */
.info-label-bold {
  font-size: var(--font-base);
  font-weight: 700;
  color: var(--text-primary);
  flex-shrink: 0;
  width: 72px;
}
.info-val {
  font-size: var(--font-base);
  color: var(--text-primary);
  margin-left: var(--space-4);
}
.info-hint {
  font-size: var(--font-xs);
  color: var(--text-hint);
  padding: 0 var(--space-4) var(--space-3);
  padding-left: calc(var(--space-4) + 72px + var(--space-4));
}
.info-hint-inline {
  font-size: var(--font-xs);
  color: var(--text-hint);
  width: 100%;
  margin-top: var(--space-1);
  padding-left: calc(72px + var(--space-4));
}

/* 关联商品迷你卡片 */
.product-mini {
  display: flex;
  gap: var(--space-2);
  margin-left: var(--space-4);
}
.product-mini-img {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-sm);
  object-fit: cover;
  flex-shrink: 0;
  background: #f3f4f6;
}
.product-mini-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
}
.product-mini-title {
  font-size: var(--font-sm);
  font-weight: 500;
  color: var(--text-primary);
  line-height: 1.4;
}
.product-mini-price {
  font-size: var(--font-base);
  font-weight: 600;
  color: var(--text-primary);
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-hint);
}
.bottom-fixed {
  padding: var(--space-3) var(--space-4);
  padding-bottom: calc(var(--space-3) + var(--safe-bottom));
  background: var(--bg-white);
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
}
.btn-dual {
  display: flex;
  gap: var(--space-3);
}
.copy-btn {
  flex: 1;
  height: 44px;
  border: 1.5px solid #d1d5db;
  border-radius: var(--radius-md);
  background: var(--bg-white);
  color: var(--text-primary);
  font-size: var(--font-base);
  cursor: pointer;
}
.contacted-btn {
  flex: 1;
  height: 44px;
  border: none;
  border-radius: var(--radius-md);
  background: var(--gradient-primary);
  color: white;
  font-size: var(--font-base);
  cursor: pointer;
}
.contacted-btn:disabled {
  background: #d1d5db;
  cursor: not-allowed;
}
.upgrade-btn {
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
</style>
