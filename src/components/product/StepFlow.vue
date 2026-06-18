<template>
  <div class="step-flow">
    <div v-for="(step, i) in steps" :key="i">
      <div class="step-item" :class="{ active: i <= current }">
        <div class="step-dot-wrap">
          <div class="step-dot" :class="{ done: i < current, active: i === current }">
            <span v-if="i < current">✓</span>
            <span v-else>{{ i + 1 }}</span>
          </div>
          <div v-if="i < steps.length - 1" class="step-line" :class="{ filled: i < current }" />
        </div>
        <div class="step-text">
          <div class="step-num">步骤 {{ i + 1 }}</div>
          <div class="step-title">{{ step.title }}</div>
          <div class="step-desc">{{ step.desc }}</div>
        </div>
      </div>
      <!-- 步骤1和步骤2之间的插槽（竖线连续穿过） -->
      <div v-if="i === 0" class="step-slot">
        <div class="step-slot-line"></div>
        <div class="step-slot-content">
          <slot name="step1-content" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  current: { type: Number, default: 0 }  // 当前步骤索引 0-3
})

const steps = [
  { title: '上传商品图片', desc: '上传清晰的翡翠图片，AI将为您自动生成商品文案' },
  { title: 'AI智能生成', desc: '正在识别图片特征，生成商品信息...' },
  { title: '编辑商品信息', desc: '可修改AI生成的标题、描述、标签和价格' },
  { title: '提交发布', desc: '发布后商品将在平台展示给买家' }
]
</script>

<style scoped>
.step-flow {
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: 0;
}
.step-item {
  display: flex;
  gap: var(--space-3);
}
.step-dot-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}
.step-dot {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #e5e7eb;
  color: var(--text-hint);
  font-size: var(--font-xs);
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}
.step-dot.done {
  background: var(--gradient-primary);
  color: white;
}
.step-dot.active {
  background: var(--color-primary-light);
  color: var(--color-primary);
  border: 2px solid var(--color-primary);
}
.step-line {
  width: 2px;
  flex: 1;
  min-height: 24px;
  background: #e5e7eb;
  transition: background 0.3s;
}
.step-line.filled {
  background: var(--gradient-primary);
}
.step-slot {
  display: flex;
  gap: var(--space-3);
}
.step-slot-line {
  width: 2px;
  min-height: 24px;
  background: #e5e7eb;
  margin-left: 13px;
  flex-shrink: 0;
  align-self: stretch;
}
.step-slot-content {
  flex: 1;
  padding-bottom: var(--space-2);
}
.step-text {
  padding-bottom: var(--space-4);
}
.step-num {
  font-size: var(--font-xs);
  color: var(--text-hint);
}
.step-title {
  font-size: var(--font-base);
  font-weight: 600;
  color: var(--text-primary);
  margin-top: 1px;
}
.step-desc {
  font-size: var(--font-xs);
  color: var(--text-hint);
  margin-top: 2px;
}
</style>
