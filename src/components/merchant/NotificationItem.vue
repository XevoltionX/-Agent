<template>
  <div class="notify-item">
    <span class="notify-time">{{ formatTime(item.createdAt) }}</span>
    <span class="notify-content">{{ item.title }}</span>
  </div>
</template>

<script setup>
defineProps({
  item: { type: Object, required: true }
})

const formatTime = (t) => {
  if (!t) return ''
  return String(t).replace('T', ' ').substring(0, 16)
}
</script>

<style scoped>
.notify-item {
  display: flex;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border-bottom: 1px solid #f3f4f6;
  font-size: var(--font-sm);
}
.notify-time {
  color: var(--text-hint);
  flex-shrink: 0;
  font-size: var(--font-xs);
}
.notify-content {
  color: var(--text-primary);
  flex: 1;
}
</style>
