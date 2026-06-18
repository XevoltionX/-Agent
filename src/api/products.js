import api from './index'
import { mockProducts } from '@/mock/products'
import { USE_MOCK } from './config'

export const getProductsApi = (status = '') => {
  if (USE_MOCK) {
    let list = [...mockProducts]
    if (status) list = list.filter(p => p.status === status.toUpperCase())
    return Promise.resolve({ code: 200, data: { list, total: list.length } })
  }
  return api.get('/products', { params: { status } })
}

export const getProductDetailApi = (id) => {
  if (USE_MOCK) {
    const p = mockProducts.find(p => p.id === Number(id))
    return Promise.resolve({ code: 200, data: p || null })
  }
  return api.get(`/products/${id}`)
}

export const searchProductsApi = (params) => {
  if (USE_MOCK) {
    const { keyword = '', tags, page = 1, size = 10 } = params || {}
    let list = [...mockProducts].filter(p => p.status === 'PUBLISHED')
    if (keyword) list = list.filter(p => p.title.includes(keyword) || p.description?.includes(keyword))
    if (tags && tags.length) list = list.filter(p => p.tags?.some(t => tags.includes(t)))
    return Promise.resolve({ code: 200, data: { list: list.slice(0, size), total: list.length } })
  }
  return api.get('/search', { params })
}

export const publishProductApi = (data) => {
  if (USE_MOCK) return Promise.resolve({ code: 200, data: { id: Date.now() } })
  return api.post('/products/publish', data)
}

export const updateProductApi = (id, data) => {
  if (USE_MOCK) return Promise.resolve({ code: 200, data: null })
  return api.put(`/products/${id}`, data)
}

export const updateProductStatusApi = (id, status) => {
  if (USE_MOCK) return Promise.resolve({ code: 200, data: null })
  return api.put(`/products/${id}/status`, { status })
}

export const deleteProductApi = (id) => {
  if (USE_MOCK) return Promise.resolve({ code: 200, data: null })
  return api.delete(`/products/${id}`)
}
