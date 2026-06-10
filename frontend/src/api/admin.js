import axios from 'axios'

const adminApi = axios.create({
  baseURL: '/Iwan/api/admin/v1',
  timeout: 30000
})

adminApi.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export const adminApiService = {
  getUserList: (params) => adminApi.get('/user/list', { params }),
  updateUserStatus: (data) => adminApi.patch('/user/status', data),
  getArticleList: (params) => adminApi.get('/article/list', { params }),
  auditArticle: (data) => adminApi.patch('/article/audit', data),
  getCommentList: (params) => adminApi.get('/comment/list', { params }),
  deleteComment: (id) => adminApi.delete(`/comment/${id}`),
  getAIStat: () => adminApi.get('/ai/stat')
}
