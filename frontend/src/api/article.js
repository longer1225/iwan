import { api } from '@/utils/request'

export const articleApi = {
  list: (params) => api.get('/articles', params),
  detail: (id) => api.get(`/articles/${id}`),
  create: (data) => api.post('/articles', data),
  update: (id, data) => api.put(`/articles/${id}`, data),
  delete: (id) => api.delete(`/articles/${id}`),
  updateStatus: (id, status) => api.patch('/articles/status', { id, status }),
  getUserArticles: (params) => api.get('/articles/user', params)
}
