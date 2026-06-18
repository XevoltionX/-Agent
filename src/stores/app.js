import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const notifications = ref([])    // 系统通知列表
  const loading = ref(false)       // 全局loading

  const setLoading = (val) => { loading.value = val }
  const setNotifications = (list) => { notifications.value = list }

  return { notifications, loading, setLoading, setNotifications }
})
