<template>
  <!-- AI消息（左侧） -->
  <div v-if="role === 'ai'" class="bubble-wrap">
    <div class="bubble bubble-ai">
      <div class="avatar avatar-ai">
        <img src="/src/assets/ai-avatar.png" width="32" height="32" alt="AI" />
      </div>
      <div class="bubble-body">
        <div class="bubble-text ai-text">
          {{ content }}
          <div v-if="time" class="time-row"><span class="bubble-time">{{ time }}</span></div>
        </div>
      </div>
    </div>
    <div v-if="cachedCards && cachedCards.length" class="ai-cards-full cached-section">
      <div class="cards-title">
        <template v-if="cards && cards.length">🔥 热门推荐<span class="cards-count">（服务繁忙,定制推荐生成中...先来看看大家喜欢哪些吧）</span></template>
        <template v-else>为您找到以下优质货源<span class="cards-count">（共{{ cachedCards.length }}件）</span></template>
      </div>
      <div class="cards-row">
        <ProductCard v-for="card in cachedCards" :key="'c'+card.id" :product="card" compact />
      </div>
    </div>
    <div v-if="cards && cards.length" class="ai-cards-full">
      <div class="cards-title">为您找到以下优质货源<span class="cards-count">（共{{ cards.length }}件）</span></div>
      <div class="cards-row">
        <ProductCard v-for="card in cards" :key="card.id" :product="card" compact />
      </div>
    </div>
  </div>

  <!-- 用户消息（右侧） -->
  <div v-else class="bubble bubble-user">
    <div class="bubble-body-user">
      <div class="bubble-text user-text">
        {{ content }}
        <div v-if="time" class="time-row"><span class="bubble-time user-time">{{ time }}</span></div>
      </div>
    </div>
    <div class="avatar avatar-user">
      <svg width="32" height="32" viewBox="0 0 32 32">
        <circle cx="16" cy="16" r="15" fill="white" stroke="#d4ede0" stroke-width="1" />
        <circle cx="16" cy="11" r="4.5" fill="var(--color-primary)" />
        <ellipse cx="16" cy="24" rx="7" ry="5" fill="var(--color-primary)" />
      </svg>
    </div>
  </div>
</template>

<script setup>
import ProductCard from '@/components/product/ProductCard.vue'

defineProps({
  role: { type: String, required: true },
  content: { type: String, default: '' },
  cards: { type: Array, default: null },
  cachedCards: { type: Array, default: null },
  time: { type: String, default: '' }
})
</script>

<style scoped>
.bubble-wrap {
  padding: var(--space-3) var(--space-4) 0;
}
.bubble {
  display: flex;
  gap: var(--space-2);
}
.bubble-ai {
  align-items: flex-start;
  justify-content: flex-start;
}
.bubble-user {
  align-items: flex-start;
  justify-content: flex-end;
}
.avatar {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}
.bubble-body {
  max-width: 78%;
}
.ai-text {
  background: var(--bg-chat-ai);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  border-top-left-radius: var(--space-1);
  box-shadow: 0 1px 4px rgba(5, 111, 53, 0.06);
  border: 1px solid #d4ede0;
  font-size: var(--font-base);
  font-weight: 700;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
.bubble-body-user {
  max-width: 78%;
}
.user-text {
  background: var(--gradient-primary);
  color: white;
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  border-top-right-radius: var(--space-1);
  font-size: var(--font-base);
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  box-shadow: 0 2px 8px rgba(5, 111, 53, 0.25);
}
.time-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 6px;
}
.bubble-time {
  font-size: 10px;
  color: var(--text-hint);
}
.user-time {
  color: rgba(255, 255, 255, 0.7);
}
.ai-cards-full {
  width: 100%;
  margin-top: var(--space-3);
  background: var(--bg-white);
  border-radius: var(--radius-md);
  padding: var(--space-3);
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}
.cards-title {
  font-size: var(--font-sm);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-2);
}
.cards-count {
  font-weight: 400;
  font-size: 10px;
  color: var(--text-hint);
  white-space: nowrap;
}
.cards-row {
  display: flex;
  gap: var(--space-2);
  justify-content: space-between;
}
.cached-section { margin-top: var(--space-2); opacity: 0.85; }
.cards-row::-webkit-scrollbar { display: none; }
</style>
