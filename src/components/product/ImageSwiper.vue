<template>
  <div class="swiper-wrap" v-if="images && images.length">
    <!-- 图片轨道 -->
    <div
      class="swiper-track"
      :style="{ transform: `translateX(-${current * 100}%)` }"
      @touchstart="onTouchStart"
      @touchmove="onTouchMove"
      @touchend="onTouchEnd"
    >
      <img
        v-for="(img, i) in images"
        :key="i"
        :src="img"
        class="swiper-img"
        alt=""
        draggable="false"
      />
    </div>

    <!-- 底部圆点指示器 -->
    <div class="swiper-dots" v-if="images.length > 1">
      <span
        v-for="(_, i) in images"
        :key="i"
        class="dot"
        :class="{ active: i === current }"
      />
    </div>

    <!-- 右下角数量显示 -->
    <div class="swiper-counter" v-if="images.length > 1">
      {{ current + 1 }}/{{ images.length }}
    </div>

    <!-- 替换图片按钮（编辑模式） -->
    <div v-if="showReplace" class="replace-btn" @click="$emit('replace')">
      替换图片
    </div>
  </div>
  <div v-else class="swiper-empty">
    <span>暂无图片</span>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  images: { type: Array, default: () => [] },
  showReplace: { type: Boolean, default: false }  // 发布商品3编辑模式用
})

const current = ref(0)
const emit = defineEmits(['replace'])

// 简易 touch 滑动
let startX = 0
const onTouchStart = (e) => { startX = e.touches[0].clientX }
const onTouchMove = (e) => { /* noop */ }
const onTouchEnd = (e) => {
  const diff = startX - e.changedTouches[0].clientX
  if (diff > 50) {
    // 左滑
    if (current.value < props.images.length - 1) {
      current.value++
    } else {
      current.value = 0  // 最后一张 → 循环回第一张
    }
  } else if (diff < -50) {
    // 右滑
    if (current.value > 0) {
      current.value--
    } else {
      current.value = props.images.length - 1  // 第一张 → 循环到最后
    }
  }
}
</script>

<style scoped>
.swiper-wrap {
  position: relative;
  width: 100%;
  height: 320px;
  overflow: hidden;
  background: #000;
}
.swiper-track {
  display: flex;
  transition: transform 0.3s ease;
  height: 100%;
}
.swiper-img {
  width: 100%;
  height: 320px;
  object-fit: contain;
  flex-shrink: 0;
  user-select: none;
}
.swiper-dots {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 6px;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  transition: background 0.3s;
}
.dot.active {
  background: #fff;
}
.swiper-counter {
  position: absolute;
  bottom: 20px;
  right: 12px;
  color: #fff;
  font-size: var(--font-xs);
  background: rgba(0, 0, 0, 0.4);
  padding: 2px 8px;
  border-radius: var(--radius-full);
}
.replace-btn {
  position: absolute;
  bottom: 16px;
  left: 12px;
  color: #fff;
  font-size: var(--font-sm);
  background: rgba(0, 0, 0, 0.5);
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  cursor: pointer;
}
.swiper-empty {
  width: 100%;
  height: 200px;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-hint);
  font-size: var(--font-sm);
}
</style>
