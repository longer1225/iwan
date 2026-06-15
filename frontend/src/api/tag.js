import { api } from '@/utils/request'

export const tagApi = {
  // 获取所有标签
  getAll: () => api.get('/tags'),
  
  // 获取热门标签
  getHot: (limit = 10) => api.get('/tags/hot', { params: { limit } }),
  
  // 创建标签
  create: (data) => api.post('/tags', data),
  
  // 删除标签
  delete: (id) => api.delete(`/tags/${id}`)
}