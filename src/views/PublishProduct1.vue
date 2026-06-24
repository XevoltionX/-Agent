<template>
  <div class="publish-page">
    <NavHeader title="发布商品" :backTo="() => $router.push('/merchant/dashboard')">
      <template #right>
        <span class="nav-status">{{ images.length > 0 ? '已选择' + images.length + '张图片' : '待上传图片' }}</span>
      </template>
    </NavHeader>

    <div class="publish-scroll">
      <!-- 步骤流 + 上传组件 -->
      <StepFlow :current="0">
        <template #step1-content>
          <ImageUploader v-model="images" :maxCount="9" />
        </template>
      </StepFlow>

      <!-- 底部固定区域 -->
      <div class="bottom-fixed">
        <!-- 限额提示 -->
        <p class="limit-hint">
          <template v-if="auth.isVip">
            商家最多发布 <span class="highlight">{{ vipLimits.maxProducts }}</span> 件商品，当前已发布 <span class="highlight">{{ usedCount }}/{{ vipLimits.maxProducts }}</span> 件
          </template>
          <template v-else>
            免费商家最多发布 <span class="highlight">{{ vipLimits.maxProducts }}</span> 件商品，当前已发布 <span class="highlight">{{ usedCount }}/{{ vipLimits.maxProducts }}</span> 件
          </template>
        </p>

        <!-- AI生成按钮 -->
        <button class="ai-btn" :disabled="!canProceed" @click="goNext">
          AI生成
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useProductStore } from '@/stores/product'
import { useVip } from '@/composables/useVip'
import { showToast } from 'vant'
import NavHeader from '@/components/common/NavHeader.vue'
import StepFlow from '@/components/product/StepFlow.vue'
import ImageUploader from '@/components/product/ImageUploader.vue'
import api from '@/api/index'

const router = useRouter()
const auth = useAuthStore()
const productStore = useProductStore()
const { vipLimits, checkPublishLimit } = useVip()

const images = ref([])
const usedCount = computed(() => auth.isVip ? 10 : 1)

const canProceed = computed(() => {
  return images.value.length > 0 && !isAtLimit.value
})

const isAtLimit = computed(() => {
  return usedCount.value >= vipLimits.value.maxProducts
})

const goNext = async () => {
  if (images.value.length === 0) { showToast('请上传至少一张商品图片'); return }
  // 总量不超过60MB
  let totalSize = 0
  for (const img of images.value) { totalSize += img.length }
  if (totalSize > 60_000_000) { showToast('所有图片总大小不能超过60MB'); return }
  if (!checkPublishLimit(usedCount.value)) return

  // 保存base64（前端展示+豆包AI识别用）
  productStore.setDraft({
    images: images.value,
    title: '', description: '', detail: '', price: '', tags: []
  })
  router.push('/merchant/publish/ai')
}
</script>

<style scoped>
.publish-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.publish-scroll {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}
/* 导航状态 */
.nav-status {
  font-size: 15px;
  font-weight: 700;
  color: #22c55e;
  white-space: nowrap;
}

.bottom-fixed {
  margin-top: auto;
  padding: var(--space-4);
  padding-bottom: calc(var(--space-4) + var(--safe-bottom));
}
.limit-hint {
  font-size: var(--font-xs);
  color: var(--text-secondary);
  text-align: center;
  margin-bottom: var(--space-3);
}
.highlight {
  color: var(--color-primary);
  font-weight: 600;
}
.ai-btn {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: var(--radius-md);
  background: var(--gradient-primary);
  color: white;
  font-size: var(--font-base);
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}
.ai-btn:active {
  background: var(--color-primary-dark);
}
.ai-btn:disabled {
  background: #d1d5db;
  color: #9ca3af;
  cursor: not-allowed;
}
</style>
