import { aiApi } from '@/utils/request'

export const aiApiService = {
  getSessions: () => aiApi.get('/chat/sessions'),
  createSession: (data) => aiApi.post('/chat/sessions', data),
  deleteSession: (id) => aiApi.delete(`/chat/sessions/${id}`),
  getMessages: (sessionId) => aiApi.get('/chat/messages', { params: { sessionId } }),
  sendMessage: (data) => aiApi.post('/chat/messages', data),
  ragSearch: (data) => aiApi.post('/rag/search', data),
  generateArticle: (data) => aiApi.post('/article/generate', data),
  rewriteArticle: (data) => aiApi.post('/article/rewrite', data),
  getUserStat: () => aiApi.get('/stat/user')
}
