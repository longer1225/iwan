<template>
  <nav class="bottom-nav">
    <button 
      v-for="item in navItems" 
      :key="item.key"
      @click="navigate(item.key)"
      :class="{ active: active === item.key }"
    >
      <span class="nav-icon">{{ item.icon }}</span>
      <span class="nav-label">{{ item.label }}</span>
    </button>
  </nav>
</template>

<script setup>
import { defineProps } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  active: {
    type: String,
    default: 'square'
  }
})

const router = useRouter()

const navItems = [
  { key: 'square', label: '广场', icon: '🏠' },
  { key: 'chat', label: '聊天', icon: '💬' },
  { key: 'ai', label: 'AI', icon: '🤖' },
  { key: 'profile', label: '我的', icon: '👤' }
]

const navigate = (key) => {
  router.push(`/${key}`)
}
</script>

<style scoped>
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: white;
  display: flex;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.bottom-nav button {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  cursor: pointer;
  transition: all 0.3s;
  
  &.active {
    .nav-icon {
      transform: scale(1.1);
    }
    
    .nav-label {
      color: #667eea;
      font-weight: 600;
    }
  }
  
  &:hover {
    background: #f8f9fa;
  }
}

.nav-icon {
  font-size: 22px;
  margin-bottom: 4px;
  transition: transform 0.3s;
}

.nav-label {
  font-size: 12px;
  color: #999;
  transition: color 0.3s;
}
</style>
