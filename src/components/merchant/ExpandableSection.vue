<template>
  <div class="expand-section" :class="{ open: isOpen }">
    <div class="expand-header" @click="isOpen = !isOpen">
      <van-icon :name="icon" size="16" color="var(--text-hint)" />
      <span class="expand-title">{{ title }}</span>
      <van-icon name="arrow" size="16" color="var(--text-hint)" class="expand-arrow" />
    </div>
    <div class="expand-body" v-show="isOpen">
      <slot />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  title: { type: String, required: true },
  icon: { type: String, default: '' }
})

const isOpen = ref(false)
</script>

<style scoped>
.expand-section {
  border-bottom: 1px solid #f3f4f6;
}
.expand-header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-4);
  cursor: pointer;
  font-size: var(--font-base);
  color: var(--text-hint);
}
.expand-arrow {
  margin-left: auto;
}
.expand-body {
  padding: 0 var(--space-4) var(--space-4);
  padding-left: calc(var(--space-4) + 24px);
}
</style>
