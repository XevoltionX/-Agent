<template>
  <div class="logo-header">
    <div class="logo-wrap">
      <img src="/favicon.png" class="logo-icon" alt="高翠" />
      <span class="logo-text">AI翡翠匹配</span>
    </div>
  </div>
</template>

<script setup>
// 首页专用：固定LOGO + "AI翡翠匹配" 文本
</script>

<style scoped>
.logo-header {
  padding: var(--space-3) var(--space-4);
  background: var(--bg-white);
  flex-shrink: 0;
}
.logo-wrap {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}
.logo-icon {
  width: 28px;
  height: 28px;
  display: block;
}
.logo-text {
  font-size: var(--font-lg);
  font-weight: 600;
  color: var(--text-primary);
}
</style>
