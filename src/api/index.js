import axios from 'axios'
import { showToast } from 'vant'

const api = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器：自动带token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('gaocui_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 响应拦截器：统一错误处理
api.interceptors.response.use(
  response => response.data,
  error => {
    const msg = error.response?.data?.message || '网络异常，请重试'
    if (error.response?.status === 401) {
      localStorage.removeItem('gaocui_token')
      localStorage.removeItem('gaocui_auth')
      window.location.hash = '#/merchant/login'
    }
    showToast(msg)
    return Promise.reject(error)
  }
)

export default api
