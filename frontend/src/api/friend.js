import { api } from '@/utils/request'

export const friendApi = {
  // 发送好友申请
  sendRequest: (data) => api.post('/friends/request', data),
  
  // 获取待处理好友申请
  getRequests: () => api.get('/friends/requests'),
  
  // 同意好友申请
  acceptRequest: (id) => api.post(`/friends/request/${id}/accept`),
  
  // 拒绝好友申请
  rejectRequest: (id) => api.post(`/friends/request/${id}/reject`),
  
  // 获取好友列表
  getFriends: () => api.get('/friends'),
  
  // 删除好友
  deleteFriend: (id) => api.delete(`/friends/${id}`),
  
  // 创建好友分组
  createGroup: (data) => api.post('/friends/groups', data),
  
  // 获取好友分组列表
  getGroups: () => api.get('/friends/groups'),
  
  // 移动好友到分组
  moveToGroup: (friendId, groupId) => api.put(`/friends/${friendId}/group/${groupId}`),
  
  // 检查是否是好友
  isFriend: (targetUserId) => api.get(`/friends/is-friend/${targetUserId}`)
}
