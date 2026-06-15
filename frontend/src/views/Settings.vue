<template>
  <div class="settings-container">
    <div class="settings-nav">
      <button 
        v-for="item in navItems" 
        :key="item.key"
        @click="activeSection = item.key"
        :class="{ active: activeSection === item.key }"
      >
        {{ item.label }}
      </button>
    </div>
    
    <div class="settings-panel">
      <div v-if="activeSection === 'profile'" class="panel-content">
        <h3>个人资料</h3>
        <el-form :model="profileForm" class="setting-form">
          <el-form-item label="头像">
            <div class="avatar-upload">
              <img :src="profileForm.avatar" class="preview-avatar" />
              <el-button size="small" @click="uploadAvatar">上传头像</el-button>
            </div>
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="profileForm.nickname" />
          </el-form-item>
          <el-form-item label="个人简介">
            <el-input v-model="profileForm.bio" type="textarea" :rows="3" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveProfile">保存修改</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <div v-if="activeSection === 'security'" class="panel-content">
        <h3>安全设置</h3>
        <el-form :model="securityForm" class="setting-form">
          <el-form-item label="原密码">
            <el-input v-model="securityForm.oldPassword" type="password" />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="securityForm.newPassword" type="password" />
          </el-form-item>
          <el-form-item label="确认新密码">
            <el-input v-model="securityForm.confirmPassword" type="password" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="changePassword">修改密码</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <div v-if="activeSection === 'privacy'" class="panel-content">
        <h3>隐私设置</h3>
        <div class="privacy-options">
          <div class="option-item">
            <span>默认文章公开状态</span>
            <el-switch v-model="privacySettings.defaultPublic" />
          </div>
          <div class="option-item">
            <span>允许陌生人私信</span>
            <el-switch v-model="privacySettings.allowStrangerMessage" />
          </div>
          <div class="option-item">
            <span>展示点赞收藏记录</span>
            <el-switch v-model="privacySettings.showActions" />
          </div>
        </div>
        <el-button type="primary" @click="savePrivacy">保存设置</el-button>
      </div>
      
      <div v-if="activeSection === 'notification'" class="panel-content">
        <h3>通知设置</h3>
        <div class="notification-options">
          <div class="option-item">
            <span>评论通知</span>
            <el-switch v-model="notificationSettings.comment" />
          </div>
          <div class="option-item">
            <span>点赞通知</span>
            <el-switch v-model="notificationSettings.like" />
          </div>
          <div class="option-item">
            <span>好友申请</span>
            <el-switch v-model="notificationSettings.friendRequest" />
          </div>
          <div class="option-item">
            <span>AI消息推送</span>
            <el-switch v-model="notificationSettings.aiMessage" />
          </div>
        </div>
        <el-button type="primary" @click="saveNotification">保存设置</el-button>
      </div>
      
      <div v-if="activeSection === 'theme'" class="panel-content">
        <h3>主题设置</h3>
        <div class="theme-options">
          <div class="theme-card" :class="{ active: currentTheme === 'light' }" @click="setTheme('light')">
            <div class="theme-preview light"></div>
            <span>浅色模式</span>
          </div>
          <div class="theme-card" :class="{ active: currentTheme === 'dark' }" @click="setTheme('dark')">
            <div class="theme-preview dark"></div>
            <span>暗黑模式</span>
          </div>
        </div>
      </div>
      
      <div v-if="activeSection === 'logout'" class="panel-content">
        <h3>退出登录</h3>
        <p class="logout-warning">退出后将需要重新登录，确定要退出吗？</p>
        <el-button type="danger" @click="handleLogout">退出登录</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'

const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

// 页面加载时获取用户信息
onMounted(async () => {
  await userStore.getUserInfo()
  if (userStore.user) {
    profileForm.avatar = userStore.user.avatar || profileForm.avatar
    profileForm.nickname = userStore.user.nickname || userStore.user.username
    profileForm.bio = userStore.user.bio || ''
  }
})

const activeSection = ref('profile')
const navItems = [
  { key: 'profile', label: '个人资料' },
  { key: 'security', label: '安全设置' },
  { key: 'privacy', label: '隐私设置' },
  { key: 'notification', label: '通知设置' },
  { key: 'theme', label: '主题设置' },
  { key: 'logout', label: '退出登录' }
]

const profileForm = reactive({
  avatar: '/api/v1/upload/avatar/default',
  nickname: '',
  bio: ''
})

const securityForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const privacySettings = reactive({
  defaultPublic: true,
  allowStrangerMessage: true,
  showActions: true
})

const notificationSettings = reactive({
  comment: true,
  like: true,
  friendRequest: true,
  aiMessage: true
})

const currentTheme = ref('light')

const saveProfile = async () => {
  try {
    await userStore.updateUserInfo(profileForm)
    alert('保存成功')
  } catch (error) {
    console.error('保存失败:', error)
  }
}

const changePassword = async () => {
  if (securityForm.newPassword !== securityForm.confirmPassword) {
    alert('两次密码不一致')
    return
  }
  
  try {
    await userStore.updatePassword(securityForm.oldPassword, securityForm.newPassword)
    alert('密码修改成功')
    securityForm.oldPassword = ''
    securityForm.newPassword = ''
    securityForm.confirmPassword = ''
  } catch (error) {
    console.error('修改密码失败:', error)
  }
}

const savePrivacy = () => {
  alert('隐私设置已保存')
}

const saveNotification = () => {
  alert('通知设置已保存')
}

const setTheme = (theme) => {
  currentTheme.value = theme
  if (theme === 'dark') {
    themeStore.toggleTheme()
  }
  alert(`已切换到${theme === 'dark' ? '暗黑' : '浅色'}模式`)
}

const handleLogout = async () => {
  try {
    await userStore.logout()
    router.push('/login')
  } catch (error) {
    console.error('退出失败:', error)
  }
}
</script>

<style scoped>
.settings-container {
  display: flex;
  min-height: 400px;
}

.settings-nav {
  width: 180px;
  border-right: 1px solid #eee;
  padding: 16px 0;
}

.settings-nav button {
  display: block;
  width: 100%;
  padding: 12px 16px;
  background: none;
  border: none;
  text-align: left;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
  
  &.active {
    color: #667eea;
    background: #f0f4ff;
  }
  
  &:hover {
    background: #f8f9fa;
  }
}

.settings-panel {
  flex: 1;
  padding: 20px;
}

.panel-content {
  max-width: 500px;
}

.panel-content h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
}

.setting-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 16px;
}

.preview-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
}

.privacy-options, .notification-options {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
}

.option-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.theme-options {
  display: flex;
  gap: 24px;
}

.theme-card {
  width: 150px;
  text-align: center;
  padding: 16px;
  border: 2px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  
  &.active {
    border-color: #667eea;
    background: #f0f4ff;
  }
}

.theme-preview {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  margin: 0 auto 12px;
}

.theme-preview.light {
  background: linear-gradient(135deg, #f5f5f5 0%, #e0e0e0 100%);
}

.theme-preview.dark {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
}

.logout-warning {
  color: #999;
  margin-bottom: 20px;
}
</style>
