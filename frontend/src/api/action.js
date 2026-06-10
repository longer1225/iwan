import { api } from '@/utils/request'

export const actionApi = {
  like: (data) => api.post('/action/like', data),
  collect: (data) => api.post('/action/collect', data),
  getLikes: (params) => api.get('/action/likes', params),
  getCollects: (params) => api.get('/action/collects', params),
  checkLike: (params) => api.get('/action/likes/check', params),
  checkCollect: (params) => api.get('/action/collects/check', params)
}
