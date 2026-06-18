import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useLeadStore = defineStore('lead', () => {
  const leads = ref([])            // 客资列表
  const currentLead = ref(null)    // 客资详情

  const setLeads = (list) => { leads.value = list }
  const setCurrentLead = (lead) => { currentLead.value = lead }
  const clearCurrentLead = () => { currentLead.value = null }

  return { leads, currentLead, setLeads, setCurrentLead, clearCurrentLead }
})
