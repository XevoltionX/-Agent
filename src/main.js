import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './assets/variables.css'
// Vant 基础样式（组件样式由 unplugin-vue-components 自动按需导入）
import 'vant/lib/index.css'

const pinia = createPinia()
const app = createApp(App)
app.use(pinia)
app.use(router)

// 恢复登录态和聊天记录（必须在pinia和router注册之后）
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
const authStore = useAuthStore()
authStore.restoreSession()
const chatStore = useChatStore()
chatStore.loadFromStorage()

app.mount('#app')
