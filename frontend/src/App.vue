<template>
  <div class="app-container" :class="{ 'dark-theme': isDark }">
    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useThemeStore } from './stores/theme'

const themeStore = useThemeStore()
const isDark = computed(() => themeStore.isDark)
</script>

<style>
:root {
  /* 主背景色 - 温暖的浅灰 */
  --bg-primary: #f8f9fa;
  /* 卡片背景 - 纯净白色 */
  --bg-secondary: #ffffff;
  /* 辅助背景 - 极浅灰色 */
  --bg-tertiary: #f0f2f5;
  /* 悬停背景 */
  --hover-bg: #f0f0f0;
  /* 主文字 - 深邃黑色 */
  --text-primary: #1c1e21;
  /* 次要文字 - 中等灰色 */
  --text-secondary: #65676b;
  /* 辅助文字 - 浅灰色 */
  --text-tertiary: #8a8d91;
  /* 边框颜色 - 极浅灰色 */
  --border-color: #e4e6eb;
  /* 强调色 - Instagram风格渐变主色 */
  --accent-color: #0095f6;
  /* 渐变色1 */
  --accent-gradient-1: #4facfe;
  /* 渐变色2 */
  --accent-gradient-2: #00f2fe;
  /* 点赞红色 */
  --like-color: #ed4956;
  /* 收藏金色 */
  --collect-color: #f5af19;
  /* 阴影 - 柔和自然 */
  --shadow-sm: rgba(0, 0, 0, 0.05);
  --shadow-md: rgba(0, 0, 0, 0.08);
  --shadow-lg: rgba(0, 0, 0, 0.12);
  /* 圆角 - 现代风格 */
  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 16px;
  --radius-xl: 20px;
}

.dark-theme {
  --bg-primary: #18191a;
  --bg-secondary: #242526;
  --bg-tertiary: #3a3b3c;
  --hover-bg: #3a3b3c;
  --text-primary: #e4e6eb;
  --text-secondary: #b0b3b8;
  --text-tertiary: #8a8d91;
  --border-color: #3a3b3c;
  --accent-color: #1877f2;
  --accent-gradient-1: #4facfe;
  --accent-gradient-2: #00f2fe;
  --like-color: #ed4956;
  --collect-color: #f5af19;
  --shadow-sm: rgba(0, 0, 0, 0.2);
  --shadow-md: rgba(0, 0, 0, 0.3);
  --shadow-lg: rgba(0, 0, 0, 0.4);
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: var(--bg-primary);
  min-height: 100vh;
  transition: background-color 0.3s, color 0.3s;
}

.app-container {
  min-height: 100vh;
  transition: background-color 0.3s, color 0.3s;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Element Plus 深色模式覆盖 */
.dark-theme .el-switch__core {
  background-color: #444;
}

.dark-theme .el-switch__core::after {
  background-color: #fff;
}

.dark-theme .el-input__inner {
  background-color: var(--bg-tertiary);
  border-color: var(--border-color);
  color: var(--text-primary);
}

.dark-theme .el-textarea__inner {
  background-color: var(--bg-tertiary);
  border-color: var(--border-color);
  color: var(--text-primary);
}

.dark-theme .el-button {
  background-color: var(--accent-color);
  border-color: var(--accent-color);
}

.dark-theme .el-button--default {
  background-color: var(--bg-tertiary);
  border-color: var(--border-color);
  color: var(--text-primary);
}

/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: var(--bg-tertiary);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: var(--text-tertiary);
}

/* 渐变按钮 */
.gradient-btn {
  background: linear-gradient(135deg, var(--accent-gradient-1), var(--accent-gradient-2));
  border: none;
  color: white;
  font-weight: 600;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 15px rgba(79, 172, 254, 0.4);
  }
}

/* 玻璃拟态效果 */
.glass-effect {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.dark-theme .glass-effect {
  background: rgba(36, 37, 38, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

/* 卡片悬浮效果 */
.card-hover {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-lg);
  }
}
</style>
