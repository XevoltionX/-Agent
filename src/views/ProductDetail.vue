<template>
  <div class="detail-page">
    <NavHeader title="商品详情" />

    <div class="detail-scroll" v-if="product">
      <!-- 顶部图片轮播 -->
      <ImageSwiper :images="product.images || []" />

      <!-- 商品信息 -->
      <div class="detail-info">
        <!-- 标题 -->
        <h2 class="detail-title">{{ product.title }}</h2>

        <!-- 售价 -->
        <div class="detail-price">
          <span class="price-sym">￥</span>{{ formatPrice(product.price) }}
          <span class="estimate">预估价</span>
        </div>

        <!-- 标签 -->
        <div class="detail-section">
          <h4 class="section-label">标签</h4>
          <div class="tags-row">
            <span v-for="tag in product.tags" :key="tag" class="tag-item">{{ tag }}</span>
          </div>
        </div>

        <!-- 简介 -->
        <div class="detail-section">
          <h4 class="section-label">简介</h4>
          <p class="section-content">{{ product.description }}</p>
        </div>

        <!-- 详情 -->
        <div class="detail-section">
          <h4 class="section-label">详情</h4>
          <p class="section-content detail-text">{{ product.detail }}</p>
        </div>
      </div>
    </div>

    <!-- 加载中 -->
    <div v-else class="detail-loading">
      <van-loading size="24" />
      <span>加载中...</span>
    </div>

    <!-- 底部固定联系表单 -->
    <ContactSeller v-if="product" :productId="product.id" :searchKeyword="route.query.q || ''" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getProductDetailApi } from '@/api/products'
import NavHeader from '@/components/common/NavHeader.vue'
import ImageSwiper from '@/components/product/ImageSwiper.vue'
import ContactSeller from '@/components/merchant/ContactSeller.vue'

const route = useRoute()
const product = ref(null)

const formatPrice = (p) => {
  if (!p && p !== 0) return '--'
  return Number(p).toLocaleString()
}

onMounted(async () => {
  try {
    const res = await getProductDetailApi(route.params.id)
    product.value = res.data
  } catch (e) {
    product.value = null
  }
})
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
.detail-info {
  padding: var(--space-4);
}
.detail-title {
  font-size: var(--font-xl);
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.4;
}
.detail-price {
  font-size: var(--font-2xl);
  color: var(--color-primary);
  font-weight: 700;
  margin-top: var(--space-2);
}
.price-sym {
  font-size: var(--font-base);
}
.estimate {
  font-size: var(--font-sm);
  color: var(--text-hint);
  font-weight: 400;
}
.detail-section {
  margin-top: var(--space-5);
  border-top: 0.5px solid #f0f0f0;
  padding-top: var(--space-3);
}
.section-label {
  font-size: var(--font-base);
  color: var(--text-secondary);
  margin-bottom: var(--space-2);
  font-weight: 500;
}
.section-content {
  font-size: var(--font-base);
  color: var(--text-primary);
  line-height: 1.7;
}
.detail-text {
  white-space: pre-wrap;
}
.tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}
.tag-item {
  font-size: var(--font-sm);
  color: var(--color-primary);
  background: var(--color-primary-light);
  padding: 2px 10px;
  border-radius: var(--radius-full);
}
.detail-loading {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  color: var(--text-hint);
}
</style>
