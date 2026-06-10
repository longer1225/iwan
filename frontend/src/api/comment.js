import { api } from '@/utils/request'

export const commentApi = {
  list: (params) => api.get('/comments', params),
  create: (data) => api.post('/comments', data),
  delete: (id) => api.delete(`/comments/${id}`)
}
