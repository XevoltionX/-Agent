<template>
  <div class="publish-page">
    <NavHeader title="编辑商品" backTo="/merchant/products">
      <template #right>
        <span class="nav-del" @click="doDelete">删除</span>
        <span class="nav-save" :class="{ disabled: submitting }" @click="submitting ? null : saveChanges()">保存</span>
      </template>
    </NavHeader>

    <div class="publish-scroll" v-if="product">
      <!-- 图片预览（带替换按钮） -->
      <div class="image-section">
        <ImageSwiper :images="product.images || []" :showReplace="true" @replace="triggerReplace" />
      </div>

      <!-- 商品标题 -->
      <div class="field-group">
        <label class="field-label">商品标题 <span class="field-hint">(10字以内)</span></label>
        <input v-model="product.title" class="field-input" placeholder="请输入商品标题" maxlength="10" />
      </div>

      <!-- 商品简介 -->
      <div class="field-group">
        <label class="field-label">商品简介 <span class="field-hint">(50字以内)</span></label>
        <textarea v-model="product.description" class="field-textarea" rows="3" placeholder="请输入商品简介" maxlength="50" />
      </div>

      <!-- 商品详情 -->
      <div class="field-group">
        <label class="field-label">商品详情 <span class="field-hint">(300字以内)</span></label>
        <textarea v-model="product.detail" class="field-textarea" rows="4" placeholder="请输入商品详情" maxlength="300" />
      </div>

      <!-- 商品标签 -->
      <div class="field-group">
        <label class="field-label">商品标签</label>
        <TagEditor v-model="product.tags" />
      </div>

      <!-- 预估售价 -->
      <div class="field-group">
        <label class="field-label">预估售价</label>
        <div class="price-input-wrap">
          <span class="price-prefix">￥</span>
          <input v-model.number="product.price" class="field-input price-field" type="number" placeholder="请输入预估售价" />
        </div>
      </div>
    </div>

    <!-- 底部双按钮 -->
    <div class="bottom-fixed bottom-dual" v-if="product">
      <button class="delist-btn" @click="toggleStatus">
        {{ product.status === 'PUBLISHED' ? '下架商品' : '上架商品' }}
      </button>
      <button class="publish-btn" @click="saveChanges">保存修改</button>
    </div>

    <div v-else class="empty-state">商品不存在</div>

    <div v-if="submitting" class="submit-mask"></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetailApi, updateProductApi, deleteProductApi, updateProductStatusApi } from '@/api/products'
import { showToast } from 'vant'
import NavHeader from '@/components/common/NavHeader.vue'
import ImageSwiper from '@/components/product/ImageSwiper.vue'
import TagEditor from '@/components/product/TagEditor.vue'

const route = useRoute()
const router = useRouter()
const product = ref(null)
const submitting = ref(false)

onMounted(async () => {
  try {
    const res = await getProductDetailApi(route.params.id)
    product.value = { ...res.data, tags: [...(res.data.tags || [])] }
  } catch (e) { /* ignore */ }
})



const saveChanges = async () => {
  if (submitting.value) return
  submitting.value = true
  if (submitting.value) return // double-check
  try {
    const data = {
      title: product.value.title,
      description: product.value.description,
      detail: product.value.detail,
      price: product.value.price,
      tags: product.value.tags,
      images: product.value.images
    }
    await updateProductApi(product.value.id, data)
    showToast('修改已保存')
    router.back()
  } catch (e) {
    showToast('保存失败')
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async () => {
  try {
    const newStatus = product.value.status === 'PUBLISHED' ? 'DELISTED' : 'PUBLISHED'
    await updateProductStatusApi(product.value.id, newStatus)
    product.value = { ...product.value, status: newStatus }
    showToast(newStatus === 'PUBLISHED' ? '商品已上架' : '商品已下架')
  } catch (e) {
    showToast('操作失败')
  }
}

const doDelete = async () => {
  try {
    await deleteProductApi(product.value.id)
    showToast('商品已删除')
    router.back()
  } catch (e) {
    showToast('删除失败')
  }
}

const triggerReplace = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = (e) => {
    const file = e.target.files?.[0]
    if (file) {
      const reader = new FileReader()
      reader.onload = (ev) => {
        const images = [...(product.value.images || [])]
        images[0] = ev.target.result
        product.value.images = images
      }
      reader.readAsDataURL(file)
    }
  }
  input.click()
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
.image-section {
  position: relative;
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
.field-input:focus { border-color: var(--color-primary); }
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
.field-textarea:focus { border-color: var(--color-primary); }
.price-input-wrap {
  display: flex;
  align-items: center;
  gap: 0;
  border: 1.5px solid #e5e7eb;
  border-radius: var(--radius-sm);
  overflow: hidden;
  transition: border-color 0.2s;
}
.price-input-wrap:focus-within { border-color: var(--color-primary); }
.price-prefix {
  font-size: var(--font-lg);
  font-weight: 600;
  color: var(--color-primary);
  padding: 0 var(--space-3);
  background: var(--color-primary-lighter);
  line-height: 42px;
}
.price-field { border: none !important; border-radius: 0 !important; }

.bottom-fixed {
  padding: var(--space-4);
  padding-bottom: calc(var(--space-4) + var(--safe-bottom));
  flex-shrink: 0;
  background: var(--bg-white);
  border-top: 1px solid #f0f0f0;
}
.bottom-dual {
  display: flex;
  gap: var(--space-3);
}
.delist-btn {
  flex: 1;
  height: 48px;
  border: 1.5px solid #d1d5db;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, #f9fafb, #e5e7eb);
  color: var(--text-secondary);
  font-size: var(--font-base);
  cursor: pointer;
}
.publish-btn {
  flex: 1;
  height: 48px;
  border: none;
  border-radius: var(--radius-md);
  background: var(--gradient-primary);
  color: white;
  font-size: var(--font-base);
  font-weight: 500;
  cursor: pointer;
}

/* 导航右侧文字按钮 */
.nav-del {
  font-size: var(--font-base);
  font-weight: 700;
  color: #ef4444;
  cursor: pointer;
  margin-right: 48px;
}
.nav-save {
  font-size: var(--font-base);
  font-weight: 700;
  color: #22c55e;
  cursor: pointer;
}
.nav-save.disabled {
  opacity: 0.4;
  pointer-events: none;
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
