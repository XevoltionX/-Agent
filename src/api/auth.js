import api from './index'
import { USE_MOCK } from './config'

export const sendCodeApi = (data) => {
  if (USE_MOCK) {
    return new Promise(resolve =>
      setTimeout(() => resolve({ code: 200, message: '验证码已发送', data: null }), 300)
    )
  }
  return api.post('/send-code', data)
}

export const loginApi = (data) => {
  if (USE_MOCK) {
    return new Promise(resolve => setTimeout(() => resolve({
      code: 200, message: 'success',
      data: {
        token: 'mock_token_' + Date.now(),
        email: data.email,
        isVip: false,
        vipStartDate: null,
        vipEndDate: null
      }
    }), 500))
  }
  return api.post('/login', data)
}

export const getMeApi = () => {
  if (USE_MOCK) return Promise.resolve({ code: 200, data: {} })
  return api.get('/me')
}
