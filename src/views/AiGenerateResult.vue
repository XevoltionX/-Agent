<template>
  <div class="ai-page">
    <NavHeader title="AI智能生成结果" :backTo="() => $router.push('/merchant/publish')">
      <template #right>
        <span class="nav-status" @click="regenerate">{{ statusText }}</span>
      </template>
    </NavHeader>

    <div class="ai-scroll" v-if="draft">
      <!-- 图片预览 -->
      <ImageSwiper :images="draft.images || []" />

      <!-- AI生成的商品标题 -->
      <div class="field-group">
        <label class="field-label">商品标题 <span class="field-hint">(10字以内)</span></label>
        <div class="field-value">{{ draft.title || 'AI生成中...' }}</div>
      </div>

      <!-- AI生成的商品简介 -->
      <div class="field-group">
        <label class="field-label">商品简介 <span class="field-hint">(50字以内)</span></label>
        <div class="field-value">{{ draft.description || 'AI生成中...' }}</div>
      </div>

      <!-- AI生成的商品详情 -->
      <div class="field-group">
        <label class="field-label">商品详情 <span class="field-hint">(300字以内)</span></label>
        <div class="field-value pre-wrap">{{ draft.detail || 'AI生成中...' }}</div>
      </div>

      <!-- AI生成的商品标签 -->
      <div class="field-group">
        <label class="field-label">商品标签</label>
        <div class="field-tags" v-if="draft.tags && draft.tags.length">
          <span v-for="(t, i) in draft.tags" :key="i" class="ai-tag">{{ t }}</span>
        </div>
        <div v-else class="field-value">AI生成中...</div>
      </div>

      <!-- AI生成的预估售价 -->
      <div class="field-group">
        <label class="field-label">预估售价</label>
        <div class="field-value price-val">{{ draft.price ? '￥' + Number(draft.price).toLocaleString() : 'AI生成中...' }}</div>
      </div>
    </div>

    <div v-else class="empty-state">请先上传图片</div>

    <!-- 底部按钮 -->
    <div class="bottom-fixed">
      <button class="edit-btn" @click="goEdit">编辑商品信息</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useProductStore } from '@/stores/product'
import { showToast } from 'vant'
import NavHeader from '@/components/common/NavHeader.vue'
import ImageSwiper from '@/components/product/ImageSwiper.vue'
import api from '@/api/index'

const router = useRouter()
const productStore = useProductStore()
const draft = computed(() => productStore.draftProduct)
const statusText = ref('AI生成中')

// 调用豆包VL生成（base64）
const generate = async () => {
  const images = productStore.draftProduct?.images
  if (!images || images.length === 0) {
    statusText.value = '重新生成'
    return
  }
  try {
    const res = await api.post('/ai/recognize', { imageUrl: images[0] }, { timeout: 120000 })
    const data = res.data || {}
    statusText.value = '重新生成'
    // price=0 → 非翡翠或图片不一致
    if (!data.price || data.price == 0) {
      showToast('识别失败，图片可能非翡翠或非同一商品，请重新上传')
      setTimeout(() => router.push('/merchant/publish'), 1000)
      return
    }
    if (productStore.draftProduct) {
      productStore.setDraft({
        ...productStore.draftProduct,
        title: data.title || '',
        description: data.description || '',
        detail: data.detail || '',
        price: data.price || '',
        tags: data.tags || []
      })
    }
  } catch (e) {
    statusText.value = '重新生成'
    showToast('AI识别失败，请重试')
  }
}

onMounted(() => { generate() })

const regenerate = () => {
  statusText.value = 'AI生成中'
  if (productStore.draftProduct) {
    productStore.setDraft({
      ...productStore.draftProduct,
      title: '', description: '', detail: '', price: '', tags: []
    })
  }
  generate()
}

const goEdit = () => {
  router.push('/merchant/publish/edit')
}
</script>

<style scoped>
.ai-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.ai-scroll {
  flex: 1;
  overflow-y: auto;
}
.nav-status {
  font-size: 15px;
  font-weight: 700;
  color: #22c55e;
  cursor: pointer;
}

.field-group {
  padding: var(--space-3) var(--space-4);
  border-bottom: 0.5px solid #f3f4f6;
}
.field-label {
  font-size: var(--font-base);
  color: var(--text-primary);
  font-weight: 700;
  display: block;
  margin-bottom: var(--space-2);
}
.field-hint {
  font-weight: 400;
  color: var(--text-hint);
  font-size: var(--font-sm);
}
.field-value {
  font-size: var(--font-base);
  color: var(--text-primary);
  line-height: 1.6;
}
.pre-wrap {
  white-space: pre-wrap;
}
.price-val {
  color: var(--color-primary);
  font-weight: 700;
  font-size: var(--font-lg);
}
.field-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}
.ai-tag {
  font-size: var(--font-sm);
  color: var(--color-primary);
  background: var(--color-primary-light);
  padding: 3px 10px;
  border-radius: var(--radius-full);
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-hint);
}

.bottom-fixed {
  padding: var(--space-4);
  padding-bottom: calc(var(--space-4) + var(--safe-bottom));
  background: var(--bg-white);
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
}
.edit-btn {
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
.edit-btn:active {
  background: var(--color-primary-dark);
}
</style>
