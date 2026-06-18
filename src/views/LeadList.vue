<template>
  <div class="leadlist-page">
    <NavHeader title="客资列表" :titleBold="true" backTo="/merchant/dashboard" />

    <!-- 分类标签 -->
    <div class="tabs-row">
      <div v-for="tab in tabs" :key="tab.key" class="tab-item" :class="{ active: activeTab === tab.key }" @click="activeTab = tab.key">
        {{ tab.label }}
      </div>
    </div>

    <!-- 客资列表 -->
    <div class="leads-scroll" v-if="filteredLeads.length" @scroll="onScroll">
      <LeadListItem
        v-for="lead in filteredLeads"
        :key="lead.id"
        :lead="lead"
        :showFullEmail="auth.isVip"
        :showStatus="true"
        @click="goDetail(lead)"
      />
    </div>
    <div v-else class="empty-state">暂无客资</div>

    <!-- 免费商家底部按钮 -->
    <div v-if="!auth.isVip" class="bottom-fixed">
      <button class="upgrade-btn" @click="$router.push('/merchant/account')">
        升级VIP，查看全部联系方式
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getLeadsApi } from '@/api/leads'
import NavHeader from '@/components/common/NavHeader.vue'
import LeadListItem from '@/components/merchant/LeadListItem.vue'

const router = useRouter()
const auth = useAuthStore()
const activeTab = ref('all')
const leads = ref([])
const visibleCount = ref(10)
const loadingMore = ref(false)

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'pending', label: '待联系' },
  { key: 'contacted', label: '已联系' }
]

const filteredLeads = computed(() => {
  let list = activeTab.value === 'all' ? leads.value : leads.value.filter(l => l.status === activeTab.value.toUpperCase())
  return list.slice(0, visibleCount.value)
})

const hasMore = computed(() => {
  let list = activeTab.value === 'all' ? leads.value : leads.value.filter(l => l.status === activeTab.value.toUpperCase())
  return visibleCount.value < list.length
})

const loadMore = () => {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  setTimeout(() => { visibleCount.value += 10; loadingMore.value = false }, 300)
}

onMounted(async () => {
  try {
    const res = await getLeadsApi()
    leads.value = res.data?.list || []
  } catch (e) { /* ignore */ }
})

const onScroll = (e) => {
  const el = e.target
  if (el.scrollHeight - el.scrollTop - el.clientHeight < 50) loadMore()
}
const goDetail = (lead) => {
  router.push(`/merchant/leads/${lead.id}`)
}
</script>

<style scoped>
.leadlist-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.tabs-row {
  display: flex;
  background: var(--bg-white);
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}
.tab-item {
  flex: 1;
  text-align: center;
  padding: var(--space-3) 0;
  font-size: var(--font-sm);
  color: var(--text-secondary);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.15s;
}
.tab-item.active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
  font-weight: 600;
}
.leads-scroll {
  flex: 1;
  overflow-y: auto;
  background: var(--bg-white);
  padding: 0 var(--space-4);
}
.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-hint);
  font-size: var(--font-sm);
}
.bottom-fixed {
  padding: var(--space-3) var(--space-4);
  padding-bottom: calc(var(--space-3) + var(--safe-bottom));
  background: var(--bg-white);
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
}
.upgrade-btn {
  width: 100%;
  height: 44px;
  border: none;
  border-radius: var(--radius-md);
  background: var(--gradient-primary);
  color: white;
  font-size: var(--font-base);
  font-weight: 500;
  cursor: pointer;
}
</style>
