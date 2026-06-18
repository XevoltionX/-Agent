<template>
  <div class="promo-banner">
    <h3 class="promo-title">极速匹配客源</h3>
    <p class="promo-sub">AI智能分发，成交快人一步</p>
  </div>
</template>

<script setup>
// 推广横幅 — 替代缺失的 Image.png 底图，用渐变替代
</script>

<style scoped>
.promo-banner {
  padding: var(--space-5) var(--space-4);
  margin: var(--space-3);
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, #056f35, #2a8451);
  text-align: center;
}
.promo-title {
  font-size: var(--font-lg);
  font-weight: 700;
  color: white;
}
.promo-sub {
  font-size: var(--font-sm);
  color: rgba(255, 255, 255, 0.8);
  margin-top: var(--space-1);
}
</style>
