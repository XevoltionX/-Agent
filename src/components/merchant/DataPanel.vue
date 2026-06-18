<template>
  <div class="data-panel">
    <div class="data-item">
      <div class="data-label">商品数量</div>
      <div class="data-value">{{ productCount }}</div>
      <div class="data-sub">已上架/上限</div>
    </div>
    <div class="data-divider" />
    <div class="data-item">
      <div class="data-label">今日客资</div>
      <div class="data-value">{{ todayLeads }}<span class="data-unit">条</span></div>
      <div class="data-sub" style="visibility:hidden">-</div>
    </div>
    <div class="data-divider" />
    <div class="data-item">
      <div class="data-label">累计客资</div>
      <div class="data-value">{{ totalLeads }}<span class="data-unit">条</span></div>
      <div class="data-sub" style="visibility:hidden">-</div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  productCount: { type: String, default: '0/2' },
  todayLeads: { type: Number, default: 0 },
  totalLeads: { type: Number, default: 0 }
})
</script>

<style scoped>
.data-panel {
  display: flex;
  align-items: center;
  background: var(--gradient-primary);
  padding: var(--space-4) var(--space-3);
  margin: 0 var(--space-3);
  border-radius: var(--radius-md);
}
.data-item {
  flex: 1;
  text-align: left;
  padding: 0 var(--space-2);
}
.data-value {
  font-size: var(--font-xl);
  font-weight: 700;
  color: white;
}
.data-unit {
  font-size: var(--font-sm);
  font-weight: 400;
}
.data-label {
  font-size: var(--font-xs);
  color: rgba(255, 255, 255, 0.75);
  margin-bottom: 2px;
}
.data-sub {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.55);
  margin-top: 1px;
}
.data-divider {
  width: 1px;
  height: 30px;
  background: rgba(255, 255, 255, 0.3);
}
</style>
