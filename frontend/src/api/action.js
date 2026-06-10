import { api } from '@/utils/request'

export const actionApi = {
  like: (data) => api.post('/action/like', data),
  collect: (data) => api.post('/action/collect', data),
  getLikes: (params) => api.get('/action/like/list', params),
  getCollects: (params) => api.get('/action/collect/list', params)
}
