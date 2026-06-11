import { api } from '@/utils/request'

export const commentApi = {
  list: (params) => api.get('/comments', params),
  create: (data) => api.post('/comments', data),
  delete: (id) => api.delete(`/comments/${id}`),
  like: (id) => api.post(`/comments/${id}/like`),
  isLiked: (id) => api.get(`/comments/${id}/liked`),
  getReplies: (id) => api.get(`/comments/${id}/replies`)
}
