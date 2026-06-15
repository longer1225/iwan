import { api } from '@/utils/request'

export const userApi = {
  // 获取当前用户信息
  getInfo: () => api.get('/user/info'),
  
  // 更新用户信息
  update: (data) => api.put('/user', data),
  
  // 更新头像
  updateAvatar: (data) => api.put('/user/avatar', data),
  
  // 更新密码
  updatePassword: (data) => api.put('/user/password', data),
  
  // 搜索用户
  search: (keyword) => api.get('/search', { keyword, type: 'user' }),
  
  // 获取用户详情
  getDetail: (userId) => api.get(`/user/detail/${userId}`)
}
