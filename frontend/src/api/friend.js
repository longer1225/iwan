import { api } from '@/utils/request'

export const friendApi = {
  list: (params) => api.get('/friends', params),
  apply: (data) => api.post('/friends', data),
  audit: (data) => api.put('/friends/audit', data),
  delete: (id) => api.delete(`/friends/${id}`),
  updateRemark: (data) => api.patch('/friends/remark', data),
  getGroups: () => api.get('/friend/groups'),
  createGroup: (data) => api.post('/friend/groups', data),
  updateGroup: (id, data) => api.put(`/friend/groups/${id}`, data),
  deleteGroup: (id) => api.delete(`/friend/groups/${id}`),
  moveToGroup: (data) => api.patch('/friend/group/move', data)
}
