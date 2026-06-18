<template>
  <div class="bottom-nav">
    <div class="nav-item" :class="{ active: current === 'home' }" @click="$emit('nav', 'home')">
      <van-icon :name="current === 'home' ? 'wap-home' : 'wap-home-o'" size="20" />
      <span>首页</span>
    </div>
    <div class="nav-item" :class="{ active: current === 'products' }" @click="$emit('nav', 'products')">
      <van-icon :name="current === 'products' ? 'orders' : 'orders-o'" size="20" />
      <span>商品</span>
    </div>
    <div class="nav-item" :class="{ active: current === 'dashboard' }" @click="$emit('nav', 'dashboard')">
      <van-icon :name="current === 'dashboard' ? 'gem' : 'gem-o'" size="20" />
      <span>管理后台</span>
    </div>
    <div class="nav-item" :class="{ active: current === 'profile' }" @click="$emit('nav', 'profile')">
      <van-icon :name="current === 'profile' ? 'user' : 'user-o'" size="20" />
      <span>我的</span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  current: { type: String, default: 'dashboard' }
})
defineEmits(['nav'])
</script>

<style scoped>
.bottom-nav {
  display: flex;
  background: var(--bg-white);
  border-top: 1px solid #f0f0f0;
  padding-bottom: var(--safe-bottom);
  flex-shrink: 0;
  height: 50px;
}
.nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  font-size: 10px;
  color: var(--text-hint);
  cursor: pointer;
  transition: color 0.15s;
}
.nav-item.active {
  color: var(--color-primary);
}
</style>
