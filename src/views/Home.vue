<template>
  <div class="home-page">
    <!-- 固定顶部：LOGO(左) + 刷新按钮 + 商家入驻按钮(右) -->
    <div class="home-top">
      <LogoHeader />
      <div class="top-actions">
        <span class="refresh-btn" @click="resetChat" title="清除聊天">🔄</span>
        <span class="top-right-btn" @click="goMerchantEntry">商家入驻</span>
      </div>
    </div>

    <!-- 聊天区域（可滚动） -->
    <div class="chat-area" ref="chatAreaRef">
      <!-- AI欢迎语 + 提示词（无聊天时显示） -->
      <div v-if="chatStore.messages.length === 0" class="welcome-section">
        <ChatBubble role="ai" content="您好！&#10;请说出您的翡翠需求（预算、品类、尺寸、品相），我将为您精准匹配货源~" />
        <PromptSuggestions @select="handlePrompt" />
      </div>

      <!-- 聊天消息列表 -->
      <template v-else>
        <ChatBubble
          v-for="(msg, i) in chatStore.messages"
          :key="i"
          :role="msg.role"
          :content="msg.content"
          :cards="msg.cards"
          :cachedCards="msg.cachedCards"
          :time="msg.timestamp || formatChatTime(msg.timestamp)"
        />
      </template>
    </div>

    <!-- 底部输入区（固定） -->
    <ChatInput @send="handleSend" />
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '@/stores/chat'
import { useAuthStore } from '@/stores/auth'
import { chatApi } from '@/api/ai'
import { useSSE } from '@/composables/useSSE'
import LogoHeader from '@/components/common/LogoHeader.vue'
import ChatBubble from '@/components/chat/ChatBubble.vue'
import ChatInput from '@/components/chat/ChatInput.vue'
import PromptSuggestions from '@/components/chat/PromptSuggestions.vue'

const router = useRouter()
const chatStore = useChatStore()
const authStore = useAuthStore()
const chatAreaRef = ref(null)
const { streamChat, abort: abortSSE } = useSSE()

onMounted(() => {
  chatStore.loadFromStorage()
})

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    const el = chatAreaRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

// "商家入驻"按钮 — 根据登录态跳不同页
const goMerchantEntry = () => {
  if (authStore.isLoggedIn) {
    // 已登录商家 → 跳管理后台
    router.push('/merchant/dashboard')
  } else {
    // 游客 → 跳登录注册页
    router.push('/merchant/login')
  }
}

// 格式化聊天时间戳 → HH:mm
const formatChatTime = (ts) => {
  if (!ts) return ''
  const d = new Date(ts)
  return d.getHours().toString().padStart(2, '0') + ':' + d.getMinutes().toString().padStart(2, '0')
}

// 清除聊天重置
const resetChat = () => {
  chatStore.clearHistory()
  chatStore.sessionId = ''
}

// 点击提示词 — 直接发送
const handlePrompt = (text) => {
  handleSend(text)
}

// 发送消息
const handleSend = (text) => {
  // 用户消息
  const userMsg = {
    role: 'user',
    content: text,
    timestamp: formatChatTime(Date.now())
  }
  chatStore.addMessage(userMsg)
  chatStore.lastKeyword = text
  scrollToBottom()

  const aiMsgIndex = chatStore.messages.length
  chatStore.addMessage({ role: 'ai', content: '已为您解析需求，正在匹配优质翡翠货源', cards: undefined, timestamp: '' })

  const baseText = '已为您解析需求，正在匹配优质翡翠货源'
  let dotCount = 3
  const fakeTimer = setInterval(() => {
    dotCount = dotCount > 1 ? dotCount - 1 : 3
    const msgs = [...chatStore.messages]
    if (msgs[aiMsgIndex] && msgs[aiMsgIndex].content && msgs[aiMsgIndex].content.startsWith(baseText)) {
      msgs[aiMsgIndex] = { ...msgs[aiMsgIndex], content: baseText + '.'.repeat(dotCount) }
      chatStore.messages = msgs
    }
  }, 500)

  streamChat(text, chatStore.sessionId, {
    onChunk: (char) => {
      clearInterval(fakeTimer)
      const msgs = [...chatStore.messages]
      const cur = msgs[aiMsgIndex].content
      msgs[aiMsgIndex] = { ...msgs[aiMsgIndex], content: (cur.startsWith(baseText) ? '' : cur) + char }
      chatStore.messages = msgs; scrollToBottom()
    },
    onCards: (cards) => {
      const msgs = [...chatStore.messages]
      if (!msgs[aiMsgIndex].cards) { msgs[aiMsgIndex] = { ...msgs[aiMsgIndex], cards } }
      else { msgs[aiMsgIndex] = { ...msgs[aiMsgIndex], cachedCards: msgs[aiMsgIndex].cards, cards } }
      chatStore.messages = msgs
    },
    onDone: (sid) => {
      clearInterval(fakeTimer)
      if (sid) chatStore.sessionId = sid.replace(/"/g, '')
      const d = new Date()
      const msgs = [...chatStore.messages]
      msgs[aiMsgIndex] = { ...msgs[aiMsgIndex], timestamp: d.getHours().toString().padStart(2,'0')+':'+d.getMinutes().toString().padStart(2,'0') }
      chatStore.messages = msgs
      chatStore.persist(); scrollToBottom()
    },
    onError: () => {
      clearInterval(fakeTimer)
      const msgs = [...chatStore.messages]
      msgs[aiMsgIndex] = { ...msgs[aiMsgIndex], content: '抱歉，服务暂时不可用，请稍后重试。' }
      chatStore.messages = msgs; scrollToBottom()
    }
  })
}
</script>

<style scoped>
.home-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--color-primary-lighter);
}

/* 顶部区域 */
.home-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: var(--space-4);
  background: var(--bg-white);
  flex-shrink: 0;
}
.top-actions {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}
.refresh-btn {
  font-size: 16px;
  cursor: pointer;
  opacity: 0.6;
}
.refresh-btn:active {
  opacity: 1;
}
.top-right-btn {
  font-size: var(--font-sm);
  color: var(--bg-white);
  background: var(--gradient-primary);
  cursor: pointer;
  white-space: nowrap;
  user-select: none;
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  font-weight: 500;
}

/* 聊天区域 */
.chat-area {
  flex: 1;
  overflow-y: auto;
  padding-top: var(--space-4);
  padding-bottom: var(--space-2);
}
.welcome-section {
  padding-top: var(--space-6);
}
</style>
