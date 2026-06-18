<template>
  <div class="dashboard-page">
    <!-- 顶部导航：三条线(菜单) + 标题 + VIP标 + 灰色通知铃铛 -->
    <NavHeader :showBack="false" :titleBold="true">
      <template #left>
        <van-icon name="bars" size="22" color="var(--text-primary)" @click="$router.push('/')" style="cursor:pointer" />
      </template>
      <template #title>商家后台</template>
      <template #badge><VipBadge :show="auth.isVip" /></template>
      <template #right>
        <div class="bell-wrap" @click="$router.push('/merchant/notifications')">
          <van-icon name="bell" size="22" color="var(--text-hint)" />
          <span v-if="unreadCount > 0" class="bell-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
        </div>
      </template>
    </NavHeader>

    <div class="dashboard-scroll">
      <!-- 数据面板 -->
      <div class="panel-section">
        <DataPanel
          :productCount="productCount"
          :todayLeads="todayLeads"
          :totalLeads="totalLeads"
        />
      </div>

      <!-- 4个功能入口 -->
      <div class="btn-grid">
        <div class="grid-item" @click="$router.push('/merchant/publish')">
          <div class="grid-icon" style="background:var(--color-primary)"><van-icon name="add-o" size="22" color="white" /></div>
          <span>发布商品</span>
        </div>
        <div class="grid-item" @click="$router.push('/merchant/products')">
          <div class="grid-icon" style="background:#3b82f6"><van-icon name="orders-o" size="22" color="white" /></div>
          <span>商品管理</span>
        </div>
        <div class="grid-item" @click="$router.push('/merchant/leads')">
          <div class="grid-icon" style="background:#f97316"><van-icon name="friends-o" size="22" color="white" /></div>
          <span>客资列表</span>
        </div>
        <div class="grid-item" @click="$router.push('/merchant/account')">
          <div class="grid-icon" style="background:#8b5cf6"><span style="font-size:20px">👑</span></div>
          <span>账户权限</span>
        </div>
      </div>

      <!-- 最近客资 -->
      <div class="recent-leads">
        <div class="recent-header">
          <h3>最近客资</h3>
          <span class="all-btn" @click="$router.push('/merchant/leads')">全部 &gt;</span>
        </div>
        <div v-if="recentLeads.length === 0" class="no-leads">暂无客资</div>
        <LeadListItem
          v-for="lead in recentLeads"
          :key="lead.id"
          :lead="lead"
          :showFullEmail="auth.isVip"
          :showTimeIcon="true"
          @click="$router.push(`/merchant/leads/${lead.id}`)"
        />
      </div>

    </div>

    <!-- 底部导航 -->
    <BottomNav current="dashboard" @nav="handleNav" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useVip } from '@/composables/useVip'
import { getLeadsApi } from '@/api/leads'
import api from '@/api/index'
import NavHeader from '@/components/common/NavHeader.vue'
import VipBadge from '@/components/common/VipBadge.vue'
import BottomNav from '@/components/common/BottomNav.vue'
import DataPanel from '@/components/merchant/DataPanel.vue'
import LeadListItem from '@/components/merchant/LeadListItem.vue'
const router = useRouter()
const auth = useAuthStore()
const { vipLimits } = useVip()

const todayLeads = ref(3)
const totalLeads = ref(28)
const recentLeads = ref([])
const unreadCount = ref(0)

// 商品数量文案：VIP "10/100"，免费 "1/2"
const productCount = ref(auth.isVip ? '10/100' : '1/2')

onMounted(async () => {
  try {
    const [leadsRes, countRes, dashRes] = await Promise.all([
      getLeadsApi(), api.get('/notifications/unread-count'), api.get('/merchant/dashboard')
    ])
    recentLeads.value = (leadsRes.data?.list || []).slice(0, 5)
    unreadCount.value = countRes.data || 0
    if (dashRes.data) {
      productCount.value = dashRes.data.productCount || '0/2'
      todayLeads.value = dashRes.data.todayLeads || 0
      totalLeads.value = dashRes.data.totalLeads || 0
    }
  } catch (e) { /* ignore */ }
})

// 底部导航跳转
const handleNav = (target) => {
  switch (target) {
    case 'home': router.push('/'); break
    case 'products': router.push('/merchant/products'); break
    case 'dashboard': break // 已在当前页
    case 'profile': router.push('/merchant/profile'); break
  }
}
</script>

<style scoped>
.dashboard-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.dashboard-scroll {
  flex: 1;
  overflow-y: auto;
}
.panel-section {
  padding: var(--space-3) 0;
}

/* 功能入口网格 */
.btn-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0;
  padding: var(--space-3);
  background: var(--bg-white);
  margin: 0 var(--space-3);
  border-radius: var(--radius-md);
}
.grid-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) 0;
  cursor: pointer;
  font-size: var(--font-xs);
  color: var(--text-secondary);
}
.grid-icon {
  width: 42px;
  height: 42px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 最近客资 */
.recent-leads {
  margin: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background: var(--bg-white);
  border-radius: var(--radius-md);
}
.recent-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-2);
}
.recent-header h3 {
  font-size: var(--font-base);
  font-weight: 600;
}
.bell-wrap {
  position: relative;
  cursor: pointer;
}
.bell-badge {
  position: absolute;
  top: -6px;
  right: -8px;
  background: #ef4444;
  color: white;
  font-size: 10px;
  font-weight: 700;
  min-width: 16px;
  height: 16px;
  line-height: 16px;
  text-align: center;
  border-radius: 8px;
  padding: 0 4px;
}
.no-leads {
  text-align: center;
  padding: var(--space-4);
  color: var(--text-hint);
  font-size: var(--font-sm);
}
.all-btn {
  font-size: var(--font-sm);
  color: var(--text-hint);
  cursor: pointer;
}
</style>
