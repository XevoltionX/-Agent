<template>
  <div class="chat-input-wrap">
    <div class="input-card">
      <div class="input-row">
        <textarea
          ref="inputRef"
          v-model="text"
          class="chat-textarea"
          :rows="2"
          placeholder="请输入您的翡翠需求...&#10;支持中文英文等多语言"
          @input="onInput"
          @keydown.enter="onEnter"
        />
        <button
          class="send-btn"
          :class="{ active: text.trim() }"
          :disabled="!text.trim()"
          @click="onSend"
        >
          <svg class="send-icon" :class="{ active: text.trim() }" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/>
          </svg>
          AI匹配
        </button>
      </div>
      <p class="hint-text">AI智能匹配，仅供参考，不做鉴定与交易</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const text = ref('')
const emit = defineEmits(['send'])

const onInput = () => {
  // textarea 高度自适应由CSS max-height控制，此处预留扩展
}

const onEnter = (e) => {
  if (!e.shiftKey) {
    e.preventDefault()
    onSend()
  }
}

const onSend = () => {
  const val = text.value.trim()
  if (!val) return
  emit('send', val)
  text.value = ''
}
</script>

<style scoped>
.chat-input-wrap {
  flex-shrink: 0;
  padding: var(--space-2) var(--space-3);
  padding-bottom: calc(var(--space-2) + var(--safe-bottom));
}

/* 输入卡片 — 圆角容器 + 白色背景 + 与边缘有间距 */
.input-card {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: var(--space-3);
  box-shadow: 0 2px 12px rgba(5, 111, 53, 0.06);
  border: 2px solid #c8e6d8;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.input-card:focus-within {
  border-color: var(--color-primary);
  box-shadow: 0 2px 16px rgba(5, 111, 53, 0.12);
}

.input-row {
  display: flex;
  gap: var(--space-2);
  align-items: center;
}
.chat-textarea {
  flex: 1;
  border: 2px solid #d4ede0;
  border-radius: var(--radius-sm);
  padding: var(--space-2) var(--space-3);
  font-size: var(--font-base);
  resize: none;
  outline: none;
  line-height: 1.5;
  min-height: 44px;
  max-height: 120px;
  font-family: inherit;
  background: var(--bg-white);
  transition: border-color 0.2s;
}
.chat-textarea:focus {
  border-color: var(--color-primary);
}
.send-btn {
  height: 40px;
  padding: 0 var(--space-4);
  border: none;
  border-radius: var(--radius-full);
  background: #d1d5db;
  color: #9ca3af;
  font-size: var(--font-sm);
  font-weight: 500;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: var(--space-1);
}
.send-icon {
  color: #9ca3af;
  transition: color 0.2s;
}
.send-icon.active {
  color: white;
}
.send-btn.active {
  background: var(--gradient-primary);
  color: white;
  box-shadow: 0 2px 8px rgba(5, 111, 53, 0.3);
}
.send-btn:disabled {
  cursor: not-allowed;
}
.hint-text {
  font-size: var(--font-xs);
  color: var(--text-hint);
  margin-top: var(--space-2);
  text-align: center;
  padding-top: var(--space-2);
  border-top: 1px solid #f0f4f1;
}
</style>
