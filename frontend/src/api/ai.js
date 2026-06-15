import { api } from '@/utils/request'

export const aiApi = {
  // 获取推荐文章
  getRecommendations: (limit = 5) => api.get('/ai/recommend', { params: { limit } }),
  
  // RAG检索
  ragSearch: (query, limit = 3) => api.get('/ai/rag/search', { params: { query, limit } }),
  
  // AI聊天
  chat: (message, useRAG = false) => api.post('/ai/chat', { message, useRAG }),
  
  // 获取创作建议
  suggestTopics: () => api.post('/ai/write/suggest')
}
