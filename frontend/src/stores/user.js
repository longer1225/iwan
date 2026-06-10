import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { api } from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(null)
  const isLoggedIn = computed(() => !!token.value && !!user.value)
  
  const login = async (username, password) => {
    const response = await api.post('/user/login', { username, password })
    if (response.code === 200) {
      token.value = response.data.token
      user.value = response.data
      localStorage.setItem('token', token.value)
      localStorage.setItem('user', JSON.stringify(user.value))
    }
    return response
  }
  
  const register = async (username, password, nickname, email) => {
    const response = await api.post('/user/register', { username, password, nickname, email })
    return response
  }
  
  const logout = async () => {
    await api.post('/user/logout')
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }
  
  const getUserInfo = async () => {
    const response = await api.get('/user/info')
    if (response.code === 200) {
      user.value = response.data
      localStorage.setItem('user', JSON.stringify(user.value))
    }
    return response
  }
  
  const updateUserInfo = async (info) => {
    const response = await api.put('/user/info', info)
    if (response.code === 200) {
      user.value = { ...user.value, ...response.data }
      localStorage.setItem('user', JSON.stringify(user.value))
    }
    return response
  }
  
  const updatePassword = async (oldPassword, newPassword) => {
    return await api.patch('/user/password', { oldPassword, newPassword })
  }
  
  const loadUserFromStorage = () => {
    const storedUser = localStorage.getItem('user')
    if (storedUser) {
      try {
        user.value = JSON.parse(storedUser)
      } catch (e) {
        console.error('Failed to parse stored user:', e)
      }
    }
  }
  
  return {
    token,
    user,
    isLoggedIn,
    login,
    register,
    logout,
    getUserInfo,
    updateUserInfo,
    updatePassword,
    loadUserFromStorage
  }
})
