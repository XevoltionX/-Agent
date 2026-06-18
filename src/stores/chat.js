import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useChatStore = defineStore('chat', () => {
  const sessionId = ref('')
  // messages: [{role:'user'|'ai', content, cards?:[], timestamp}]
  const messages = ref([])

  // 从localStorage恢复
  const loadFromStorage = () => {
    const saved = localStorage.getItem('gaocui_chat')
    if (saved) {
      try { messages.value = JSON.parse(saved) } catch (e) { messages.value = [] }
    }
  }

  // 持久化
  const persist = () => {
    localStorage.setItem('gaocui_chat', JSON.stringify(messages.value))
  }

  // 添加消息
  const addMessage = (msg) => {
    if (!msg.timestamp) {
      const d = new Date()
      msg.timestamp = d.getHours().toString().padStart(2, '0') + ':' + d.getMinutes().toString().padStart(2, '0')
    }
    messages.value.push(msg)
    persist()
  }

  // 清空聊天
  const clearHistory = () => {
    messages.value = []
    localStorage.removeItem('gaocui_chat')
  }

  const lastKeyword = ref('')

  return { sessionId, messages, lastKeyword, loadFromStorage, persist, addMessage, clearHistory }
})
