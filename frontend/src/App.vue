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
  --bg-primary: #f5f5f5;
  --bg-secondary: #ffffff;
  --bg-tertiary: #fafafa;
  --text-primary: #333333;
  --text-secondary: #999999;
  --text-tertiary: #bbbbbb;
  --border-color: #e0e0e0;
  --accent-color: #667eea;
  --hover-bg: #f5f7fa;
  --shadow: rgba(0, 0, 0, 0.05);
}

.dark-theme {
  --bg-primary: #1a1a2e;
  --bg-secondary: #16213e;
  --bg-tertiary: #0f3460;
  --text-primary: #e0e0e0;
  --text-secondary: #888888;
  --text-tertiary: #666666;
  --border-color: #333333;
  --accent-color: #7c8ff0;
  --hover-bg: #2a2a4e;
  --shadow: rgba(0, 0, 0, 0.3);
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
</style>
