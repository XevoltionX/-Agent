import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useProductStore = defineStore('product', () => {
  const products = ref([])          // 商品列表
  const currentProduct = ref(null)  // 正在编辑/查看的商品
  const draftProduct = ref(null)    // 发布流程中的临时草稿

  const setDraft = (draft) => { draftProduct.value = draft }
  const clearDraft = () => { draftProduct.value = null }

  const setCurrentProduct = (product) => { currentProduct.value = product }
  const clearCurrentProduct = () => { currentProduct.value = null }

  return { products, currentProduct, draftProduct, setDraft, clearDraft, setCurrentProduct, clearCurrentProduct }
})
