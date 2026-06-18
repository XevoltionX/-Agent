<template>
  <span v-if="show" class="vip-badge">VIP</span>
</template>

<script setup>
defineProps({
  show: { type: Boolean, default: true }
})
</script>

<style scoped>
.vip-badge {
  display: inline-block;
  background: var(--color-vip-bg);
  color: var(--color-vip-text);
  font-size: 11px;
  font-weight: 700;
  padding: 1px 7px;
  border-radius: var(--radius-full);
  line-height: 1.6;
  vertical-align: middle;
}
</style>
