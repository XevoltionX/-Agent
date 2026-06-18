<template>
  <div class="product-card" :class="{ compact }" @click="goDetail">
    <!-- 图片 -->
    <img :src="product.images?.[0] || placeholderImg" class="card-img" alt="" />

    <!-- 信息区 -->
    <div class="card-info">
      <!-- 标题 -->
      <div class="card-title">{{ product.title }}</div>

      <!-- 标签（最多3个） -->
      <div class="card-tags" v-if="product.tags && product.tags.length">
        <span v-for="tag in product.tags.slice(0, 3)" :key="tag" class="tag">{{ tag }}</span>
      </div>

      <!-- 价格 + 徽章 -->
      <div class="card-bottom">
        <span class="card-price">￥{{ formatPrice(product.price) }}</span>
        <span class="card-badge" :class="isVipProduct ? 'vip-badge' : 'merchant-badge'">
          {{ isVipProduct ? 'VIP' : '商家' }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '@/stores/chat'

const props = defineProps({
  product: { type: Object, required: true },
  compact: { type: Boolean, default: false }
})

const router = useRouter()
const chatStore = useChatStore()
const placeholderImg = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="400" height="400"><rect fill="#f3f4f6" width="400" height="400"/><text x="200" y="210" text-anchor="middle" fill="#9ca3af" font-size="20">暂无图片</text></svg>')

const formatPrice = (p) => {
  if (!p && p !== 0) return '--'
  return Number(p).toLocaleString()
}

// VIP商家判断：merchantId为2的是VIP
const isVipProduct = computed(() => props.product.merchantId === 2 || props.product.merchant_id === 2)

const goDetail = () => {
  const kw = chatStore.lastKeyword || ''
  const query = kw ? `?q=${encodeURIComponent(kw)}` : ''
  router.push(`/product/${props.product.id}${query}`)
}
</script>

<style scoped>
.product-card {
  cursor: pointer;
  flex-shrink: 0;
  transition: transform 0.15s;
}
.product-card:active {
  transform: scale(0.97);
}
.compact {
  flex: 1;
  min-width: 0;
}

/* 图片 */
.card-img {
  width: 100%;
  aspect-ratio: 3 / 4;
  object-fit: cover;
  border-radius: var(--radius-sm);
  display: block;
  background: #f3f4f6;
}

/* 信息区 */
.card-info {
  padding: var(--space-1) 2px 0;
}

/* 标题 */
.card-title {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

/* 标签 */
.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
  margin-top: 3px;
}
.tag {
  font-size: 9px;
  color: var(--text-primary);
  background: #f3f4f6;
  padding: 1px 5px;
  border-radius: 2px;
  white-space: nowrap;
}

/* 底部价格+徽章 */
.card-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 4px;
}
.card-price {
  font-size: 12px;
  font-weight: 700;
  color: #ef4444;
}
.card-badge {
  font-size: 9px;
  font-weight: 600;
  padding: 1px 5px;
  border-radius: 2px;
  white-space: nowrap;
}
.vip-badge {
  color: #dc2626;
  background: #fef3c7;
}
.merchant-badge {
  color: var(--color-primary);
  background: var(--color-primary-light);
}
</style>
