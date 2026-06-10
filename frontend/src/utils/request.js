import axios from 'axios'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const baseURL = import.meta.env.VITE_API_BASE_URL || '/iwan/api/v1'

const instance = axios.create({
  baseURL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

instance.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

instance.interceptors.response.use(
  (response) => {
    const { code, msg, data } = response.data
    if (code === 200) {
      return response.data
    } else {
      ElMessage.error(msg || '请求失败')
      return Promise.reject(new Error(msg))
    }
  },
  (error) => {
    const { response } = error
    if (response) {
      const { status, data } = response
      let message = '请求失败'
      
      switch (status) {
        case 400:
          message = data?.msg || '参数错误'
          break
        case 401:
          message = '登录已失效，请重新登录'
          const userStore = useUserStore()
          userStore.logout()
          window.location.href = '/login'
          break
        case 403:
          message = data?.msg || '权限不足'
          break
        case 404:
          message = data?.msg || '资源不存在'
          break
        case 409:
          message = data?.msg || '操作冲突'
          break
        case 429:
          message = '请求过于频繁，请稍后重试'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = data?.msg || `未知错误 ${status}`
      }
      
      ElMessage.error(message)
    } else {
      ElMessage.error('网络连接失败')
    }
    
    return Promise.reject(error)
  }
)

export const api = {
  get: (url, params) => instance.get(url, { params }),
  post: (url, data) => instance.post(url, data),
  put: (url, data) => instance.put(url, data),
  patch: (url, data) => instance.patch(url, data),
  delete: (url) => instance.delete(url)
}

export const aiApi = axios.create({
  baseURL: '/iwan/api/ai/v1',
  timeout: 60000
})

aiApi.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  }
)

export default instance
