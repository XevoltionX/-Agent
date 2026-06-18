<template>
  <div class="product-row">
    <!-- 左侧缩略图 -->
    <img :src="product.images?.[0] || placeholderImg" class="row-img" alt="" />

    <!-- 中间信息 -->
    <div class="row-info">
      <div class="row-title">{{ product.title }}</div>
      <div class="row-price">￥{{ formatPrice(product.price) }}</div>
      <div class="row-time">{{ formatTime(product.created_at || product.published_at) }}</div>
    </div>

    <!-- 右侧状态+操作 -->
    <div class="row-actions">
      <span class="status-tag" :class="product.status">{{ statusText }}</span>
      <div class="action-btns">
        <button class="action-text-btn edit-btn" @click.stop="$emit('edit', product)">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
          编辑
        </button>
        <button class="action-text-btn del-btn" @click.stop="$emit('delete', product)">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
          删除
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  product: { type: Object, required: true }
})

defineEmits(['edit', 'delete'])

const placeholderImg = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200"><rect fill="#f3f4f6" width="200" height="200"/></svg>')

const statusText = computed(() => {
  switch (props.product.status) {
    case 'PUBLISHED': return '已上架'
    case 'DRAFT': return '草稿'
    case 'DELISTED': return '已下架'
    default: return props.product.status
  }
})

const formatPrice = (p) => {
  if (!p && p !== 0) return '--'
  return Number(p).toLocaleString()
}

const formatTime = (t) => {
  if (!t) return '-'
  // "2026-06-15" or "2026-06-15 10:30" → "06-15" or "06-15 10:30"
  return t.slice(5, 16).replace('T', ' ')
}
</script>

<style scoped>
.product-row {
  display: flex;
  gap: var(--space-3);
  padding: var(--space-3);
  border-bottom: 1px solid #f3f4f6;
  align-items: flex-end;
}
.row-img {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-sm);
  object-fit: cover;
  flex-shrink: 0;
  background: #f3f4f6;
}
.row-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.row-title {
  font-size: var(--font-sm);
  font-weight: 700;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.row-price {
  font-size: var(--font-base);
  font-weight: 600;
  color: var(--color-primary);
}
.row-time {
  font-size: var(--font-xs);
  color: var(--text-hint);
}
.row-actions {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: var(--space-3);
}
.status-tag {
  font-size: 11px;
}
.status-tag.PUBLISHED {
  color: #22c55e;
}
.status-tag.DRAFT {
  color: var(--text-hint);
}
.status-tag.DELISTED {
  color: var(--text-hint);
}
.action-btns {
  display: flex;
  gap: var(--space-4);
}
.action-text-btn {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 11px;
  border: none;
  background: none;
  cursor: pointer;
  padding: 0;
}
.edit-btn {
  color: var(--text-hint);
}
.del-btn {
  color: #ef4444;
}
</style>
