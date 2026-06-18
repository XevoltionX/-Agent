import api from './index'
import { mockLeads } from '@/mock/leads'
import { USE_MOCK } from './config'

export const getLeadsApi = (status = 'all') => {
  if (USE_MOCK) {
    let list = [...mockLeads]
    if (status !== 'all') list = list.filter(l => l.status === status.toUpperCase())
    return Promise.resolve({ code: 200, data: { list, total: list.length } })
  }
  return api.get('/leads', { params: { status } })
}

export const getLeadDetailApi = (id) => {
  if (USE_MOCK) {
    const l = mockLeads.find(l => l.id === Number(id))
    return Promise.resolve({ code: 200, data: l || null })
  }
  return api.get(`/leads/${id}`)
}

export const markContactedApi = (id) => {
  if (USE_MOCK) return Promise.resolve({ code: 200, data: null })
  return api.put(`/leads/${id}/contact`)
}
