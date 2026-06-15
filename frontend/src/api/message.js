import { api } from '@/utils/request'

export const messageApi = {
  // 发送消息
  send: (data) => api.post('/messages', data),
  
  // 获取聊天记录
  getHistory: (targetUserId, params = {}) => api.get(`/messages/chat/${targetUserId}`, params),
  
  // 获取未读消息数量
  getUnreadCount: () => api.get('/messages/unread'),
  
  // 标记消息为已读
  markAsRead: (targetUserId) => api.post(`/messages/read/${targetUserId}`),
  
  // 获取聊天会话列表
  getSessions: () => api.get('/messages/sessions')
}
