<template>
  <div class="nav-header">
    <!-- 左侧：返回箭头或自定义插槽 -->
    <div class="nav-left" @click="handleLeft">
      <van-icon v-if="showBack" name="arrow-left" size="22" color="var(--text-primary)" />
      <slot v-else name="left" />
    </div>

    <!-- 中间：标题 + 可选badge -->
    <div class="nav-title">
      <slot name="title">
        <span class="title-text bold">{{ title }}</span>
        <slot name="badge" />
      </slot>
    </div>

    <!-- 右侧：自定义按钮区域 -->
    <div class="nav-right">
      <slot name="right" />
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  title: { type: String, default: '' },
  titleBold: { type: Boolean, default: false },
  showBack: { type: Boolean, default: true },
  backTo: { type: [String, Function], default: null }
})

const router = useRouter()
const emit = defineEmits(['back'])

const handleLeft = () => {
  if (!props.showBack) return
  emit('back')
  if (props.backTo) {
    if (typeof props.backTo === 'function') {
      props.backTo()
    } else {
      router.push(props.backTo)
    }
  } else {
    router.back()
  }
}
</script>

<style scoped>
.nav-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  min-height: 44px;
  padding: 0 var(--space-4);
  background: var(--bg-white);
  flex-shrink: 0;
  border-bottom: 0.5px solid #f0f0f0;
  position: relative;
  z-index: 10;
}
.nav-left,
.nav-right {
  width: 44px;
  display: flex;
  align-items: center;
  cursor: pointer;
}
.nav-right {
  justify-content: flex-end;
  width: auto;
  min-width: 44px;
}
.nav-title {
  flex: 1;
  text-align: center;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  font-size: var(--font-lg);
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
}
.title-text {
  font-weight: 700;
}
</style>
