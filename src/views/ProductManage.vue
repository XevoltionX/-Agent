<template>
  <div class="manage-page">
    <!-- 顶部导航 -->
    <NavHeader title="商品管理" :titleBold="true" backTo="/merchant/dashboard">
      <template #right>
        <van-icon name="share-o" size="20" color="var(--text-primary)" style="cursor:pointer" @click="onShare" />
      </template>
    </NavHeader>

    <!-- 分类标签页 -->
    <div class="tabs-row">
      <div v-for="tab in tabs" :key="tab.key" class="tab-item" :class="{ active: activeTab === tab.key }" @click="activeTab = tab.key">
        {{ tab.label }} <span class="tab-count">({{ getCount(tab.key) }})</span>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="product-list" v-if="filteredProducts.length">
      <ProductListItem
        v-for="p in filteredProducts"
        :key="p.id"
        :product="p"
        @edit="goEdit"
        @delete="confirmDelete"
      />
    </div>
    <div v-else class="empty-list">暂无商品</div>

    <!-- 底部按钮 -->
    <div class="bottom-fixed">
      <button
        class="bottom-btn"
        :class="{ disabled: isAtLimit }"
        :disabled="isAtLimit"
        @click="goPublish"
      >
        {{ bottomBtnText }}
      </button>
    </div>

    <!-- 删除确认弹窗 -->
    <van-dialog v-model:show="showDelete" title="确认删除" message="确定要删除该商品吗？删除后不可恢复" show-cancel-button confirmButtonText="删除" confirmButtonColor="#ef4444" @confirm="doDelete" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useVip } from '@/composables/useVip'
import { getProductsApi, deleteProductApi } from '@/api/products'
import { showToast, showDialog } from 'vant'
import NavHeader from '@/components/common/NavHeader.vue'
import ProductListItem from '@/components/product/ProductListItem.vue'

const router = useRouter()
const auth = useAuthStore()
const { vipLimits } = useVip()

const activeTab = ref('all')
const products = ref([])
const showDelete = ref(false)
const deleteTarget = ref(null)

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'published', label: '已上架' },
  { key: 'draft', label: '草稿' },
  { key: 'delisted', label: '已下架' }
]

const getCount = (key) => {
  if (key === 'all') return products.value.length
  return products.value.filter(p => p.status === key.toUpperCase()).length
}

const filteredProducts = computed(() => {
  if (activeTab.value === 'all') return products.value
  return products.value.filter(p => p.status === activeTab.value.toUpperCase())
})

const isAtLimit = computed(() => {
  return auth.isVip
    ? getCount('published') >= vipLimits.value.maxProducts
    : getCount('all') >= vipLimits.value.maxProducts
})

const bottomBtnText = computed(() => {
  if (auth.isVip) {
    return isAtLimit.value ? '请下架部分商品后再发布' : '+ 发布新商品'
  } else {
    return isAtLimit.value ? '升级VIP发布更多商品' : '+ 发布新商品'
  }
})

onMounted(async () => {
  try {
    const res = await getProductsApi()
    products.value = res.data?.list || []
  } catch (e) { /* ignore */ }
})

const goEdit = (product) => {
  router.push(`/merchant/publish/manage/${product.id}`)
}

const confirmDelete = (product) => {
  deleteTarget.value = product
  showDelete.value = true
}

const doDelete = async () => {
  if (!deleteTarget.value) return
  try {
    await deleteProductApi(deleteTarget.value.id)
    products.value = products.value.filter(p => p.id !== deleteTarget.value.id)
    showToast('已删除')
  } catch (e) { /* ignore */ }
  deleteTarget.value = null
}

const goPublish = () => {
  if (isAtLimit.value) {
    if (auth.isVip) {
      showToast('请下架部分商品后再发布')
    } else {
      router.push('/merchant/account')
    }
    return
  }
  router.push('/merchant/publish')
}
const onShare = () => {
  showToast('分享功能')
}

</script>

<style scoped>
.manage-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 标签页 */
.tabs-row {
  display: flex;
  background: var(--bg-white);
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
  overflow-x: auto;
}
.tab-item {
  flex: 1;
  text-align: center;
  padding: var(--space-3) 0;
  font-size: var(--font-sm);
  color: var(--text-secondary);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  white-space: nowrap;
  transition: all 0.15s;
}
.tab-item.active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
  font-weight: 600;
}
.tab-count {
  font-size: var(--font-xs);
}

/* 商品列表 */
.product-list {
  flex: 1;
  overflow-y: auto;
  background: var(--bg-white);
}
.empty-list {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-hint);
  font-size: var(--font-sm);
}

/* 底部 */
.bottom-fixed {
  padding: var(--space-3) var(--space-4);
  padding-bottom: calc(var(--space-3) + var(--safe-bottom));
  background: var(--bg-white);
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
}
.bottom-btn {
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
.bottom-btn:active {
  background: var(--color-primary-dark);
}
.bottom-btn.disabled {
  background: #d1d5db;
  color: #9ca3af;
}
.bottom-btn:disabled {
  cursor: not-allowed;
}
</style>
