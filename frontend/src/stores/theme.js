import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const isDark = ref(false)
  
  const initTheme = () => {
    const stored = localStorage.getItem('isDark')
    if (stored !== null) {
      isDark.value = stored === 'true'
    } else {
      // 默认检查系统主题偏好
      if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
        isDark.value = true
      }
    }
  }
  
  const setDarkMode = (value) => {
    const newValue = !!value
    if (isDark.value !== newValue) {
      isDark.value = newValue
      localStorage.setItem('isDark', String(newValue))
    }
  }
  
  const toggleTheme = () => {
    setDarkMode(!isDark.value)
  }
  
  watch(isDark, (val) => {
    if (val) {
      document.documentElement.classList.add('dark-theme')
    } else {
      document.documentElement.classList.remove('dark-theme')
    }
  }, { immediate: true })
  
  // 初始化时加载主题
  initTheme()
  
  return {
    isDark,
    toggleTheme,
    setDarkMode,
    initTheme
  }
})
