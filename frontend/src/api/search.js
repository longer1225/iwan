import { api } from '@/utils/request'

export const searchApi = {
  // 全站搜索
  search: (keyword, type = 'all', limit = 10) => api.get('/search', { keyword, type, limit }),
  
  // 热门搜索词
  hotKeywords: () => api.get('/search/hot')
}
