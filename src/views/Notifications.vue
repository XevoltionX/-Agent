<template>
  <div class="notify-page">
    <NavHeader title="系统通知" backTo="/merchant/dashboard" />

    <div class="notify-list" v-if="notifications.length">
      <NotificationItem v-for="n in notifications" :key="n.id" :item="n" />
    </div>
    <div v-else class="empty-state">暂无通知</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import NavHeader from '@/components/common/NavHeader.vue'
import NotificationItem from '@/components/merchant/NotificationItem.vue'
import api from '@/api/index'

const notifications = ref([])

onMounted(async () => {
  try {
    const res = await api.get('/notifications')
    notifications.value = res.data || []
    // 标记全部已读
    await api.put('/notifications/read-all')
  } catch (e) { /* ignore */ }
})
</script>

<style scoped>
.notify-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.notify-list {
  flex: 1;
  overflow-y: auto;
  background: var(--bg-white);
}
.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-hint);
  font-size: var(--font-sm);
}
</style>
