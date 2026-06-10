export const storage = {
  get: (key, defaultValue = null) => {
    try {
      const value = localStorage.getItem(key)
      if (value === null) return defaultValue
      try {
        return JSON.parse(value)
      } catch {
        return value
      }
    } catch {
      return defaultValue
    }
  },
  
  set: (key, value) => {
    try {
      const serialized = typeof value === 'string' ? value : JSON.stringify(value)
      localStorage.setItem(key, serialized)
      return true
    } catch {
      return false
    }
  },
  
  remove: (key) => {
    try {
      localStorage.removeItem(key)
      return true
    } catch {
      return false
    }
  },
  
  clear: () => {
    try {
      localStorage.clear()
      return true
    } catch {
      return false
    }
  }
}
