<template>
  <div class="prompts-wrap">
    <div
      v-for="(prompt, i) in prompts"
      :key="i"
      class="prompt-chip"
      @click="$emit('select', prompt.text)"
    >
      <span class="prompt-icon">{{ prompt.icon }}</span>
      <span>{{ prompt.text }}</span>
    </div>
  </div>
</template>

<script setup>
const prompts = [
  { icon: '✨', text: '10万预算 帝王绿手镯' },
  { icon: '✨', text: '冰种平安扣 预算2万 无纹无裂' },
  { icon: '✨', text: '冰种翡翠吊坠 送礼自用均可' }
]

defineEmits(['select'])
</script>

<style scoped>
.prompts-wrap {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
}
.prompt-chip {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  background: var(--bg-gray-light);
  border: 1px solid #e5e7eb;
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-4);
  font-size: var(--font-sm);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.15s;
  user-select: none;
}
.prompt-icon {
  font-size: 18px;
  flex-shrink: 0;
}
.prompt-chip:active {
  border-color: var(--color-accent);
  background: #e8f5ee;
  color: var(--color-primary);
}
</style>
