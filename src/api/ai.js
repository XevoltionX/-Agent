import api from './index'
import { mockProducts } from '@/mock/products'
import { USE_MOCK } from './config'

// 后端Chat API — Agent就绪
export const chatApi = (data) => {
  if (USE_MOCK) {
    return aiMatchMock(data.message)
  }
  return api.post('/chat', data)
}

// AI匹配接口 — P0阶段Mock，P6接入真实Agent
export const aiMatchApi = (query) => {
  if (USE_MOCK) {
    // Mock: 模拟AI提取关键词 + ES搜索
    const jadeKeywords = ['翡翠', '玉', '手镯', '平安扣', '吊坠', '戒面', '帝王绿', '冰种', '玻璃种', '糯种', 'A货', '挂件', '戒指', '项链', '翡翠手镯', '预算', '圈口', '飘花']
    const isJadeQuery = jadeKeywords.some(kw => query.includes(kw))

    if (!isJadeQuery) {
      return Promise.resolve({
        code: 200, data: {
          isJadeQuery: false,
          reply: '我是高翠AI，专业找翡翠助手。您可以试着这样问：\n- 找5万左右的冰种翡翠\n- 我要A货，没预算上限',
          cards: []
        }
      })
    }

    // 从Mock商品中简单关键词匹配
    let list = [...mockProducts].filter(p => p.status === 'PUBLISHED')
    const words = query.split(/[\s，,]+/).filter(w => w.length > 1)
    list = list.filter(p => words.some(w => p.title.includes(w) || p.tags?.some(t => t.includes(w))))
    list = list.slice(0, 3)

    return Promise.resolve({
      code: 200, data: {
        isJadeQuery: true,
        reply: list.length > 0 ? '根据您的需求，我为您匹配到以下货源：' : '暂时没有匹配的货源，请尝试调整您的需求条件~',
        cards: list.map(p => ({ id: p.id, title: p.title, tags: p.tags || [], price: p.price, images: p.images || [] }))
      }
    })
  }
  return api.post('/ai/match', { query })
}
