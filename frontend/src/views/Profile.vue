<template>
  <div class="profile-container">
    <!-- 顶部个人信息卡片 -->
    <div class="profile-header">
      <div class="cover-photo"></div>
      <div class="profile-info">
        <img :src="user.avatar" class="profile-avatar" />
        <h2 class="profile-name">{{ user.nickname }}</h2>
        <p class="profile-bio">{{ user.bio || '暂无简介' }}</p>
        <p class="profile-date">注册于 {{ formatDate(user.registerTime) }}</p>
        <div class="profile-stats">
          <div class="stat-item">
            <span class="stat-value">{{ articleCount }}</span>
            <span class="stat-label">文章</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ likeCount }}</span>
            <span class="stat-label">获赞</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ collectCount }}</span>
            <span class="stat-label">收藏</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ friendCount }}</span>
            <span class="stat-label">好友</span>
          </div>
        </div>
        <div class="profile-actions">
          <el-button type="primary" @click="goToWrite">写文章</el-button>
          <el-button @click="goToManage">管理作品</el-button>
        </div>
      </div>
    </div>
    
    <div class="profile-main">
      <!-- 左侧菜单栏 -->
      <aside class="profile-sidebar">
        <nav class="sidebar-nav">
          <button 
            v-for="menu in menuItems" 
            :key="menu.key" 
            @click="activeMenu = menu.key"
            :class="{ active: activeMenu === menu.key }"
          >
            <span class="menu-icon">{{ menu.icon }}</span>
            <span class="menu-label">{{ menu.label }}</span>
          </button>
        </nav>
      </aside>
      
      <!-- 右侧主体内容区 -->
      <main class="profile-content">
        <!-- 个人主页 -->
        <div v-if="activeMenu === 'home'" class="home-section">
          <h3 class="section-title">最近发布</h3>
          <div class="article-grid">
            <article 
              v-for="article in myArticles.slice(0, 5)" 
              :key="article.id" 
              class="article-card"
              @click="goToDetail(article.id)"
            >
              <img :src="article.cover" class="card-cover" />
              <div class="card-content">
                <h4>{{ article.title }}</h4>
                <p>{{ article.summary }}</p>
                <div class="card-meta">
                  <span>{{ formatRelativeTime(article.createTime) }}</span>
                  <span>{{ article.readCount }}阅读</span>
                  <span>{{ article.likeCount }}点赞</span>
                </div>
              </div>
            </article>
            
            <div v-if="myArticles.length === 0" class="empty-state">
              <p>暂无文章</p>
              <el-button type="primary" @click="goToWrite">写第一篇文章</el-button>
            </div>
          </div>
        </div>
        
        <!-- 我的文章 -->
        <div v-if="activeMenu === 'articles'" class="articles-section">
          <div class="section-header">
            <h3 class="section-title">我的文章</h3>
            <div class="filter-tabs">
              <button 
                v-for="tab in articleTabs" 
                :key="tab.key" 
                @click="activeArticleTab = tab.key"
                :class="{ active: activeArticleTab === tab.key }"
              >
                {{ tab.label }}
              </button>
            </div>
          </div>
          <div class="article-list">
            <article 
              v-for="article in myArticles" 
              :key="article.id" 
              class="article-item"
              @click="goToDetail(article.id)"
            >
              <img :src="article.cover" class="article-thumb" />
              <div class="article-info">
                <h4>{{ article.title }}</h4>
                <p>{{ article.summary }}</p>
                <div class="article-meta">
                  <span>{{ formatRelativeTime(article.createTime) }}</span>
                  <span>{{ article.readCount }}阅读</span>
                  <span>{{ article.likeCount }}点赞</span>
                </div>
              </div>
              <div class="article-actions">
                <el-button size="small" @click.stop="editArticle(article.id)">编辑</el-button>
                <el-button size="small" type="danger" @click.stop="deleteArticle(article.id)">删除</el-button>
              </div>
            </article>
            
            <div v-if="myArticles.length === 0" class="empty-state">
              <p>暂无文章</p>
              <el-button type="primary" @click="goToWrite">写文章</el-button>
            </div>
          </div>
        </div>
        
        <!-- 我的点赞 -->
        <div v-if="activeMenu === 'likes'" class="likes-section">
          <h3 class="section-title">我的点赞</h3>
          <div class="article-list">
            <article 
              v-for="article in likedArticles" 
              :key="article.id" 
              class="article-item"
              @click="goToDetail(article.id)"
            >
              <img :src="article.cover" class="article-thumb" />
              <div class="article-info">
                <h4>{{ article.title }}</h4>
                <p>{{ article.summary }}</p>
                <div class="article-meta">
                  <span>{{ article.authorName }}</span>
                  <span>{{ formatRelativeTime(article.createTime) }}</span>
                </div>
              </div>
            </article>
            
            <div v-if="likedArticles.length === 0" class="empty-state">
              <p>暂无点赞内容</p>
            </div>
          </div>
        </div>
        
        <!-- 我的收藏 -->
        <div v-if="activeMenu === 'collects'" class="collects-section">
          <h3 class="section-title">我的收藏</h3>
          <div class="article-list">
            <article 
              v-for="article in collectedArticles" 
              :key="article.id" 
              class="article-item"
              @click="goToDetail(article.id)"
            >
              <img :src="article.cover" class="article-thumb" />
              <div class="article-info">
                <h4>{{ article.title }}</h4>
                <p>{{ article.summary }}</p>
                <div class="article-meta">
                  <span>{{ article.authorName }}</span>
                  <span>{{ formatRelativeTime(article.createTime) }}</span>
                </div>
              </div>
            </article>
            
            <div v-if="collectedArticles.length === 0" class="empty-state">
              <p>暂无收藏内容</p>
            </div>
          </div>
        </div>
        
        <!-- 账号设置 -->
        <div v-if="activeMenu === 'account'" class="account-section">
          <h3 class="section-title">账号设置</h3>
          <el-form :model="accountForm" class="settings-form">
            <el-form-item label="头像">
              <div class="avatar-upload">
                <img :src="accountForm.avatar" class="preview-avatar" @click="uploadAvatar" />
                <el-button size="small" @click="uploadAvatar">上传头像</el-button>
                <p class="avatar-hint">点击头像或按钮上传</p>
              </div>
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="accountForm.nickname" />
            </el-form-item>
            <el-form-item label="个人简介">
              <el-textarea v-model="accountForm.bio" rows="3" placeholder="介绍一下自己..." />
            </el-form-item>
            <el-form-item label="修改密码">
              <el-input v-model="accountForm.oldPassword" type="password" placeholder="旧密码" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="accountForm.newPassword" type="password" placeholder="新密码" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="accountForm.confirmPassword" type="password" placeholder="确认新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveAccount">保存修改</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 隐私设置 -->
        <div v-if="activeMenu === 'privacy'" class="privacy-section">
          <h3 class="section-title">隐私设置</h3>
          <div class="settings-card">
            <div class="setting-item">
              <div class="setting-info">
                <h4>默认文章公开状态</h4>
                <p>新发布的文章默认是否公开</p>
              </div>
              <el-switch v-model="privacyForm.defaultPublic" />
            </div>
            <div class="setting-item">
              <div class="setting-info">
                <h4>允许陌生人私信</h4>
                <p>是否允许非好友用户发送私信</p>
              </div>
              <el-switch v-model="privacyForm.allowStrangerMessage" />
            </div>
            <div class="setting-item">
              <div class="setting-info">
                <h4>展示点赞收藏记录</h4>
                <p>是否公开显示你的点赞和收藏</p>
              </div>
              <el-switch v-model="privacyForm.showLikesCollects" />
            </div>
            <el-button type="primary" @click="savePrivacy">保存设置</el-button>
          </div>
        </div>
        
        <!-- 系统设置 -->
        <div v-if="activeMenu === 'system'" class="system-section">
          <h3 class="section-title">系统设置</h3>
          <div class="settings-card">
            <div class="setting-item">
              <div class="setting-info">
                <h4>暗黑模式</h4>
                <p>切换深色/浅色主题</p>
              </div>
              <el-switch v-model="systemForm.darkMode" />
            </div>
            <div class="setting-item">
              <div class="setting-info">
                <h4>评论通知</h4>
                <p>接收文章评论通知</p>
              </div>
              <el-switch v-model="systemForm.commentNotify" />
            </div>
            <div class="setting-item">
              <div class="setting-info">
                <h4>点赞通知</h4>
                <p>接收文章点赞通知</p>
              </div>
              <el-switch v-model="systemForm.likeNotify" />
            </div>
            <div class="setting-item">
              <div class="setting-info">
                <h4>好友申请通知</h4>
                <p>接收好友申请通知</p>
              </div>
              <el-switch v-model="systemForm.friendNotify" />
            </div>
            <div class="setting-item">
              <div class="setting-info">
                <h4>AI消息推送</h4>
                <p>接收AI助手消息推送</p>
              </div>
              <el-switch v-model="systemForm.aiNotify" />
            </div>
            <el-button type="primary" @click="saveSystem">保存设置</el-button>
          </div>
        </div>
      </main>
    </div>
    
    <BottomNav active="profile" />
    
    <!-- 头像裁剪弹窗 -->
    <AvatarCropper 
      :visible="showCropper" 
      :image-src="cropperImage"
      @close="handleCropperClose"
      @confirm="handleCropperConfirm"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { articleApi } from '@/api/article'
import { actionApi } from '@/api/action'
import { formatRelativeTime, formatDate } from '@/utils/format'
import BottomNav from '@/components/BottomNav.vue'
import AvatarCropper from '@/components/AvatarCropper.vue'
import { useThemeStore } from '@/stores/theme'

const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

const user = reactive({
  avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=1',
  nickname: '加载中...',
  bio: '',
  registerTime: ''
})

// 左侧菜单
const activeMenu = ref('home')
const menuItems = [
  { key: 'home', label: '个人主页', icon: '🏠' },
  { key: 'articles', label: '我的文章', icon: '📝' },
  { key: 'likes', label: '我的点赞', icon: '❤️' },
  { key: 'collects', label: '我的收藏', icon: '⭐' },
  { key: 'account', label: '账号设置', icon: '👤' },
  { key: 'privacy', label: '隐私设置', icon: '🔒' },
  { key: 'system', label: '系统设置', icon: '⚙️' }
]

// 文章筛选标签
const activeArticleTab = ref('all')
const articleTabs = [
  { key: 'all', label: '全部' },
  { key: 'public', label: '公开' },
  { key: 'private', label: '私密' },
  { key: 'draft', label: '草稿' }
]

// 文章数据
const myArticles = ref([])
const likedArticles = ref([])
const collectedArticles = ref([])
const articleCount = ref(0)
const likeCount = ref(0)
const collectCount = ref(0)
const friendCount = ref(0)

// 设置表单
const accountForm = reactive({
  avatar: '',
  nickname: '',
  bio: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const privacyForm = reactive({
  defaultPublic: true,
  allowStrangerMessage: true,
  showLikesCollects: true
})

const systemForm = reactive({
  darkMode: false,
  commentNotify: true,
  likeNotify: true,
  friendNotify: true,
  aiNotify: true
})

// 头像裁剪相关
const showCropper = ref(false)
const cropperImage = ref('')
const avatarFileInput = ref(null)

const loadUserInfo = async () => {
  try {
    const response = await userStore.getUserInfo()
    if (response.code === 200) {
      Object.assign(user, response.data)
      // 初始化账号设置表单
      accountForm.avatar = user.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=1'
      accountForm.nickname = user.nickname || ''
      accountForm.bio = user.bio || ''
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

const loadMyArticles = async () => {
  try {
    const response = await articleApi.getUserArticles({ pageNum: 1, pageSize: 20 })
    if (response.code === 200) {
      myArticles.value = response.data.records
      articleCount.value = response.data.total
    }
  } catch (error) {
    console.error('加载文章失败:', error)
  }
}

const loadLikedArticles = async () => {
  try {
    const response = await actionApi.getLikes({ pageNum: 1, pageSize: 20 })
    if (response.code === 200) {
      likedArticles.value = response.data.records
      likeCount.value = response.data.total
    }
  } catch (error) {
    console.error('加载点赞失败:', error)
  }
}

const loadCollectedArticles = async () => {
  try {
    const response = await actionApi.getCollects({ pageNum: 1, pageSize: 20 })
    if (response.code === 200) {
      collectedArticles.value = response.data.records
      collectCount.value = response.data.total
    }
  } catch (error) {
    console.error('加载收藏失败:', error)
  }
}

const goToDetail = (id) => {
  router.push(`/article/${id}`)
}

const goToWrite = () => {
  alert('文章编辑功能开发中...')
}

const goToManage = () => {
  activeMenu.value = 'articles'
}

const editArticle = (id) => {
  alert(`编辑文章: ${id}`)
}

const deleteArticle = (id) => {
  if (confirm('确定要删除这篇文章吗？')) {
    alert(`删除文章: ${id}`)
  }
}

const uploadAvatar = () => {
  // 创建隐藏的文件输入
  if (!avatarFileInput.value) {
    const input = document.createElement('input')
    input.type = 'file'
    input.accept = 'image/*'
    input.style.display = 'none'
    input.onchange = handleAvatarSelect
    avatarFileInput.value = input
    document.body.appendChild(input)
  }
  avatarFileInput.value.click()
}

const handleAvatarSelect = (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  
  if (!file.type.startsWith('image/')) {
    alert('请选择图片文件')
    return
  }
  
  if (file.size > 5 * 1024 * 1024) {
    alert('图片大小不能超过5MB')
    return
  }
  
  const reader = new FileReader()
  reader.onload = (event) => {
    cropperImage.value = event.target?.result
    showCropper.value = true
  }
  reader.readAsDataURL(file)
}

const handleCropperClose = () => {
  showCropper.value = false
}

const handleCropperConfirm = async (croppedImage) => {
  try {
    // 将base64转换为blob
    const blob = await fetch(croppedImage).then(res => res.blob())
    const file = new File([blob], 'avatar.png', { type: 'image/png' })
    
    // 创建FormData
    const formData = new FormData()
    formData.append('file', file)
    
    // 上传头像
    const response = await fetch('/iwan/api/v1/upload/avatar', {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer ' + localStorage.getItem('token')
      },
      body: formData
    })
    
    const result = await response.json()
    
    if (result.code === 200) {
      // 更新头像URL（使用相对路径通过Vite代理访问）
      const avatarUrl = result.data.url
      accountForm.avatar = avatarUrl
      user.avatar = avatarUrl
      
      // 保存到用户信息
      await userStore.updateUserInfo({ avatar: avatarUrl })
      
      showCropper.value = false
      alert('头像上传成功！')
    } else {
      alert(result.msg || '上传失败')
    }
  } catch (error) {
    console.error('上传失败:', error)
    alert('上传失败')
  }
}

const saveAccount = async () => {
  try {
    // 如果填写了密码相关字段，先验证并修改密码
    if (accountForm.oldPassword || accountForm.newPassword || accountForm.confirmPassword) {
      if (!accountForm.oldPassword) {
        alert('请输入旧密码')
        return
      }
      if (!accountForm.newPassword) {
        alert('请输入新密码')
        return
      }
      if (!accountForm.confirmPassword) {
        alert('请确认新密码')
        return
      }
      if (accountForm.newPassword !== accountForm.confirmPassword) {
        alert('两次输入的新密码不一致')
        return
      }

      const passwordResponse = await fetch('/iwan/api/v1/user/password', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        body: JSON.stringify({
          oldPassword: accountForm.oldPassword,
          newPassword: accountForm.newPassword
        })
      })
      const passwordResult = await passwordResponse.json()
      if (passwordResult.code !== 200) {
        alert(passwordResult.msg || '修改密码失败')
        return
      }
      
      // 清空密码字段
      accountForm.oldPassword = ''
      accountForm.newPassword = ''
      accountForm.confirmPassword = ''
    }

    // 保存昵称和简介
    const response = await userStore.updateUserInfo({
      nickname: accountForm.nickname,
      bio: accountForm.bio
    })
    if (response.code === 200) {
      user.nickname = accountForm.nickname
      user.bio = accountForm.bio
      alert('保存成功！')
    }
  } catch (error) {
    console.error('保存失败:', error)
    alert('保存失败')
  }
}

const savePrivacy = () => {
  alert('隐私设置已保存！')
}

const saveSystem = () => {
  // 直接设置主题状态
  if (systemForm.darkMode !== themeStore.isDark) {
    themeStore.setDarkMode(systemForm.darkMode)
  }
  // 保存其他设置到本地存储
  localStorage.setItem('commentNotify', String(systemForm.commentNotify))
  localStorage.setItem('likeNotify', String(systemForm.likeNotify))
  localStorage.setItem('friendNotify', String(systemForm.friendNotify))
  localStorage.setItem('aiNotify', String(systemForm.aiNotify))
  alert('系统设置已保存！')
}

onMounted(() => {
  loadUserInfo()
  loadMyArticles()
  loadLikedArticles()
  loadCollectedArticles()
  
  // 初始化系统设置表单
  systemForm.darkMode = themeStore.isDark
  systemForm.commentNotify = localStorage.getItem('commentNotify') === 'true' || true
  systemForm.likeNotify = localStorage.getItem('likeNotify') === 'true' || true
  systemForm.friendNotify = localStorage.getItem('friendNotify') === 'true' || true
  systemForm.aiNotify = localStorage.getItem('aiNotify') === 'true' || true
})
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background-color: var(--bg-primary);
  padding-bottom: 80px;
  transition: background-color 0.3s;
}

.profile-header {
  position: relative;
  background: var(--bg-secondary);
  border-radius: 0 0 20px 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px var(--shadow);
  transition: background-color 0.3s, box-shadow 0.3s;
}

.cover-photo {
  height: 220px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.profile-info {
  position: relative;
  text-align: center;
  margin-top: -60px;
  padding: 0 20px 20px;
}

.profile-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 4px solid var(--bg-secondary);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.profile-name {
  font-size: 24px;
  font-weight: bold;
  margin: 16px 0 8px;
  color: var(--text-primary);
  transition: color 0.3s;
}

.profile-bio {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 8px;
  transition: color 0.3s;
}

.profile-date {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-bottom: 16px;
  transition: color 0.3s;
}

.profile-stats {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 20px;
  font-weight: bold;
  color: var(--text-primary);
  transition: color 0.3s;
}

.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
  transition: color 0.3s;
}

.profile-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  
  button {
    padding: 8px 24px;
    border-radius: 20px;
    font-size: 14px;
  }
}

.profile-main {
  display: flex;
  max-width: 1000px;
  margin: 20px auto;
  gap: 20px;
}

.profile-sidebar {
  width: 200px;
  flex-shrink: 0;
}

.sidebar-nav {
  background: var(--bg-secondary);
  border-radius: 12px;
  padding: 12px;
  box-shadow: 0 2px 10px var(--shadow);
  transition: background-color 0.3s, box-shadow 0.3s;
}

.sidebar-nav button {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 12px 16px;
  background: none;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  text-align: left;
  color: var(--text-primary);
  
  &:hover {
    background: var(--hover-bg);
  }
  
  &.active {
    background: var(--accent-color);
    color: white;
  }
}

.menu-icon {
  margin-right: 12px;
  font-size: 18px;
}

.menu-label {
  font-size: 14px;
}

.profile-content {
  flex: 1;
  background: var(--bg-secondary);
  border-radius: 12px;
  padding: 20px;
  min-height: 500px;
  box-shadow: 0 2px 10px var(--shadow);
  transition: background-color 0.3s, box-shadow 0.3s;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  color: var(--text-primary);
  transition: color 0.3s;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-tabs {
  display: flex;
  gap: 8px;
}

.filter-tabs button {
  padding: 6px 16px;
  border: 1px solid var(--border-color);
  border-radius: 16px;
  background: var(--bg-secondary);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
  color: var(--text-primary);
  
  &.active {
    background: var(--accent-color);
    color: white;
    border-color: var(--accent-color);
  }
  
  &:hover:not(.active) {
    border-color: var(--accent-color);
    color: var(--accent-color);
  }
}

.article-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.article-card {
  background: var(--bg-tertiary);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 20px var(--shadow);
  }
}

.card-cover {
  width: 100%;
  height: 150px;
  object-fit: cover;
}

.card-content {
  padding: 12px;
}

.card-content h4 {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-primary);
  transition: color 0.3s;
}

.card-content p {
  font-size: 12px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 12px;
  transition: color 0.3s;
}

.card-meta {
  display: flex;
  gap: 12px;
  font-size: 11px;
  color: var(--text-tertiary);
  transition: color 0.3s;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.article-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: var(--bg-tertiary);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    background: var(--hover-bg);
  }
}

.article-thumb {
  width: 100px;
  height: 70px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.article-info {
  flex: 1;
  min-width: 0;
}

.article-info h4 {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-primary);
  transition: color 0.3s;
}

.article-info p {
  font-size: 12px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 8px;
  transition: color 0.3s;
}

.article-meta {
  display: flex;
  gap: 12px;
  font-size: 11px;
  color: var(--text-tertiary);
  transition: color 0.3s;
}

.article-actions {
  flex-shrink: 0;
  
  button {
    margin-left: 8px;
  }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary);
  transition: color 0.3s;
  
  p {
    margin-bottom: 16px;
  }
}

.settings-form {
  max-width: 500px;
}

.settings-form .el-form-item {
  margin-bottom: 20px;
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
  object-fit: cover;
}

.settings-card {
  background: var(--bg-tertiary);
  border-radius: 12px;
  padding: 20px;
  transition: background-color 0.3s;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid var(--border-color);
  
  &:last-of-type {
    border-bottom: none;
    padding-bottom: 0;
  }
}

.setting-info h4 {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
  color: var(--text-primary);
  transition: color 0.3s;
}

.setting-info p {
  font-size: 12px;
  color: var(--text-secondary);
  transition: color 0.3s;
}

.settings-card .el-button {
  margin-top: 20px;
  width: 100%;
}

/* 输入框纯色样式 */
.settings-card .el-input__wrapper {
  background-color: var(--bg-secondary) !important;
  box-shadow: none !important;
  border: 1px solid var(--border-color);
  border-radius: 8px;
}

.settings-card .el-input__inner {
  color: var(--text-primary) !important;
}

.settings-card .el-textarea__inner {
  background-color: var(--bg-secondary) !important;
  box-shadow: none !important;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-primary) !important;
}

/* 深色模式下的输入框样式 */
.dark-theme .settings-card .el-input__wrapper {
  background-color: #2a2a4e !important;
  border-color: #333333;
}

.dark-theme .settings-card .el-textarea__inner {
  background-color: #2a2a4e !important;
  border-color: #333333;
}
</style>
