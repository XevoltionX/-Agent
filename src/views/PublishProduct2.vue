<template>
  <div class="publish-page">
    <NavHeader title="编辑商品信息" :backTo="() => $router.push('/merchant/publish/ai')">
      <template #right>
        <span class="nav-save" :class="{ disabled: submitting }" @click="submitting ? null : saveDraft()">保存</span>
      </template>
    </NavHeader>

    <div class="publish-scroll" v-if="draft">
      <!-- 商品标题 -->
      <div class="field-group">
        <label class="field-label">商品标题 <span class="field-hint">(10字以内)</span></label>
        <input v-model="draft.title" class="field-input" placeholder="请输入商品标题" maxlength="10" />
      </div>

      <!-- 商品简介 -->
      <div class="field-group">
        <label class="field-label">商品简介 <span class="field-hint">(50字以内)</span></label>
        <textarea v-model="draft.description" class="field-textarea" rows="3" placeholder="请输入商品简介" maxlength="50" />
      </div>

      <!-- 商品详情 -->
      <div class="field-group">
        <label class="field-label">商品详情 <span class="field-hint">(300字以内)</span></label>
        <textarea v-model="draft.detail" class="field-textarea" rows="4" placeholder="请输入商品详情" maxlength="300" />
      </div>

      <!-- 商品标签 -->
      <div class="field-group">
        <label class="field-label">商品标签</label>
        <TagEditor v-model="draft.tags" />
      </div>

      <!-- 预估售价 -->
      <div class="field-group">
        <label class="field-label">预估售价</label>
        <div class="price-input-wrap">
          <span class="price-prefix">￥</span>
          <input v-model.number="draft.price" class="field-input price-field" type="number" placeholder="请输入预估售价" />
        </div>
      </div>

      <!-- 确认发布按钮 -->
      <div class="bottom-fixed">
        <button class="publish-btn" :disabled="!canPublish || submitting" @click="confirmPublish">
          确认发布
        </button>
      </div>
    </div>

    <div v-else class="empty-state">请先上传图片</div>

    <!-- 提交遮罩 -->
    <div v-if="submitting" class="submit-mask"></div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useProductStore } from '@/stores/product'
import { useAuthStore } from '@/stores/auth'
import { showToast } from 'vant'
import NavHeader from '@/components/common/NavHeader.vue'
import TagEditor from '@/components/product/TagEditor.vue'
import { publishProductApi } from '@/api/products'

const router = useRouter()
const productStore = useProductStore()
const auth = useAuthStore()
const submitting = ref(false)

const draft = computed({
  get: () => productStore.draftProduct,
  set: (val) => productStore.setDraft(val)
})

const canPublish = computed(() => {
  return draft.value?.title?.trim() && draft.value?.price
})

const confirmPublish = async () => {
  if (submitting.value) return
  submitting.value = true
  if (!draft.value?.title?.trim()) { showToast('请输入商品标题'); submitting.value = false; return }
  if (!draft.value?.price) { showToast('请输入预估售价'); submitting.value = false; return }
  try {
    await publishProductApi({
      title: draft.value.title,
      description: draft.value.description,
      detail: draft.value.detail,
      price: draft.value.price,
      tags: draft.value.tags,
      images: draft.value.images
    })
    showToast('发布成功！')
    productStore.clearDraft()
    router.replace('/merchant/products')
  } catch (e) {
    showToast('发布失败')
  } finally {
    submitting.value = false
  }
}

const saveDraft = async () => {
  if (submitting.value) return
  submitting.value = true
  try {
    await publishProductApi({
      title: draft.value.title || '',
      description: draft.value.description || '',
      detail: draft.value.detail || '',
      price: draft.value.price || 0,
      tags: draft.value.tags || [],
      images: draft.value.images || [],
      status: 'DRAFT'
    })
    showToast('已保存为草稿')
    router.replace('/merchant/products')
  } catch (e) {
    showToast('保存失败')
  } finally {
    submitting.value = false
  }
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
}
.field-group {
  padding: var(--space-3) var(--space-4);
  border-bottom: 0.5px solid #f3f4f6;
}
.nav-save {
  font-size: 15px;
  font-weight: 700;
  color: #22c55e;
  cursor: pointer;
}
.nav-save.disabled {
  opacity: 0.4;
  pointer-events: none;
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
.field-input {
  width: 100%;
  border: 1.5px solid #e5e7eb;
  border-radius: var(--radius-sm);
  padding: var(--space-2) var(--space-3);
  font-size: var(--font-base);
  outline: none;
  font-family: inherit;
  transition: border-color 0.2s;
}
.field-input:focus {
  border-color: var(--color-primary);
}
.field-textarea {
  width: 100%;
  border: 1.5px solid #e5e7eb;
  border-radius: var(--radius-sm);
  padding: var(--space-2) var(--space-3);
  font-size: var(--font-base);
  outline: none;
  resize: none;
  font-family: inherit;
  transition: border-color 0.2s;
}
.field-textarea:focus {
  border-color: var(--color-primary);
}
.price-input-wrap {
  display: flex;
  align-items: center;
  gap: 0;
  border: 1.5px solid #e5e7eb;
  border-radius: var(--radius-sm);
  overflow: hidden;
  transition: border-color 0.2s;
}
.price-input-wrap:focus-within {
  border-color: var(--color-primary);
}
.price-prefix {
  font-size: var(--font-lg);
  font-weight: 600;
  color: var(--color-primary);
  padding: 0 var(--space-3);
  background: var(--color-primary-lighter);
  line-height: 42px;
}
.price-field {
  border: none !important;
  border-radius: 0 !important;
}

.bottom-fixed {
  padding: var(--space-4);
  padding-bottom: calc(var(--space-4) + var(--safe-bottom));
}
.publish-btn {
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
.publish-btn:active {
  background: var(--color-primary-dark);
}
.publish-btn:disabled {
  background: #d1d5db;
  color: #9ca3af;
  cursor: not-allowed;
}
.submit-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  z-index: 9999;
  background: transparent;
}
.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-hint);
}
</style>
