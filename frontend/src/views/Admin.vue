<template>
  <div class="admin-container">
    <aside class="admin-sidebar">
      <div class="sidebar-title">管理后台</div>
      <nav class="sidebar-nav">
        <button 
          v-for="item in navItems" 
          :key="item.key"
          @click="activeTab = item.key"
          :class="{ active: activeTab === item.key }"
        >
          {{ item.label }}
        </button>
      </nav>
    </aside>
    
    <main class="admin-main">
      <header class="admin-header">
        <h2>{{ currentTabLabel }}</h2>
        <div class="header-actions">
          <el-button @click="logout">退出</el-button>
        </div>
      </header>
      
      <div v-if="activeTab === 'users'" class="tab-content">
        <div class="search-bar">
          <el-input v-model="userSearch" placeholder="搜索用户..." class="search-input" />
          <el-button type="primary" @click="searchUsers">搜索</el-button>
        </div>
        
        <el-table :data="users" border>
          <el-table-column prop="id" label="用户ID" />
          <el-table-column prop="username" label="账号" />
          <el-table-column prop="nickname" label="昵称" />
          <el-table-column prop="email" label="邮箱" />
          <el-table-column prop="status" label="状态">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">
                {{ scope.row.status === 'active' ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="注册时间" />
          <el-table-column label="操作">
            <template #default="scope">
              <el-button 
                size="small" 
                @click="toggleUserStatus(scope.row)"
                :type="scope.row.status === 'active' ? 'danger' : 'success'"
              >
                {{ scope.row.status === 'active' ? '禁用' : '解禁' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div v-if="activeTab === 'articles'" class="tab-content">
        <div class="search-bar">
          <el-input v-model="articleSearch" placeholder="搜索文章..." class="search-input" />
          <el-select v-model="articleStatus" placeholder="状态筛选">
            <el-option label="全部" value="" />
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
          <el-button type="primary" @click="searchArticles">搜索</el-button>
        </div>
        
        <el-table :data="articles" border>
          <el-table-column prop="id" label="文章ID" />
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="author" label="作者" />
          <el-table-column prop="status" label="状态">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="发布时间" />
          <el-table-column prop="viewCount" label="阅读量" />
          <el-table-column label="操作">
            <template #default="scope">
              <el-button size="small" @click="viewArticle(scope.row.id)">查看</el-button>
              <el-button 
                size="small" 
                type="success" 
                @click="approveArticle(scope.row)"
                v-if="scope.row.status === 'pending'"
              >
                通过
              </el-button>
              <el-button 
                size="small" 
                type="danger" 
                @click="rejectArticle(scope.row)"
                v-if="scope.row.status !== 'rejected'"
              >
                拒绝
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div v-if="activeTab === 'comments'" class="tab-content">
        <div class="search-bar">
          <el-input v-model="commentSearch" placeholder="搜索评论..." class="search-input" />
          <el-button type="primary" @click="searchComments">搜索</el-button>
        </div>
        
        <el-table :data="comments" border>
          <el-table-column prop="id" label="评论ID" />
          <el-table-column prop="articleTitle" label="所属文章" />
          <el-table-column prop="content" label="内容" width="300" />
          <el-table-column prop="author" label="评论者" />
          <el-table-column prop="createTime" label="时间" />
          <el-table-column label="操作">
            <template #default="scope">
              <el-button size="small" type="danger" @click="deleteComment(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div v-if="activeTab === 'statistics'" class="tab-content">
        <div class="stats-cards">
          <div class="stat-card">
            <div class="stat-value">1,234</div>
            <div class="stat-label">用户总数</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">5,678</div>
            <div class="stat-label">文章总数</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">23,456</div>
            <div class="stat-label">评论总数</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">156,789</div>
            <div class="stat-label">总阅读量</div>
          </div>
        </div>
        
        <div class="chart-section">
          <h3>AI使用统计</h3>
          <div class="chart-placeholder">
            <p>AI使用数据图表区域</p>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('users')
const navItems = [
  { key: 'users', label: '用户管理' },
  { key: 'articles', label: '文章审核' },
  { key: 'comments', label: '评论管理' },
  { key: 'statistics', label: '数据统计' }
]

const currentTabLabel = computed(() => {
  const item = navItems.find(i => i.key === activeTab.value)
  return item?.label || ''
})

const userSearch = ref('')
const articleSearch = ref('')
const articleStatus = ref('')
const commentSearch = ref('')

const users = ref([
  { id: 1, username: 'user1', nickname: '张三', email: 'zhangsan@test.com', status: 'active', createTime: '2024-01-15' },
  { id: 2, username: 'user2', nickname: '李四', email: 'lisi@test.com', status: 'active', createTime: '2024-01-16' },
  { id: 3, username: 'user3', nickname: '王五', email: 'wangwu@test.com', status: 'disabled', createTime: '2024-01-17' }
])

const articles = ref([
  { id: 1, title: 'Vue3入门指南', author: '张三', status: 'approved', createTime: '2024-01-15', viewCount: 1234 },
  { id: 2, title: 'Java后端开发实践', author: '李四', status: 'pending', createTime: '2024-01-16', viewCount: 567 },
  { id: 3, title: 'Python数据分析', author: '王五', status: 'rejected', createTime: '2024-01-17', viewCount: 89 }
])

const comments = ref([
  { id: 1, articleTitle: 'Vue3入门指南', content: '写得很棒！', author: '匿名用户', createTime: '2024-01-15 10:30' },
  { id: 2, articleTitle: 'Vue3入门指南', content: '学习了！', author: '李四', createTime: '2024-01-15 11:00' }
])

const toggleUserStatus = (user) => {
  user.status = user.status === 'active' ? 'disabled' : 'active'
  alert(`用户 ${user.nickname} 已${user.status === 'active' ? '解禁' : '禁用'}`)
}

const getStatusType = (status) => {
  switch (status) {
    case 'pending': return 'warning'
    case 'approved': return 'success'
    case 'rejected': return 'danger'
    default: return 'info'
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 'pending': return '待审核'
    case 'approved': return '已通过'
    case 'rejected': return '已拒绝'
    default: return status
  }
}

const viewArticle = (id) => {
  router.push(`/article/${id}`)
}

const approveArticle = (article) => {
  article.status = 'approved'
  alert(`文章 "${article.title}" 已通过审核`)
}

const rejectArticle = (article) => {
  article.status = 'rejected'
  alert(`文章 "${article.title}" 已拒绝`)
}

const deleteComment = (id) => {
  const index = comments.value.findIndex(c => c.id === id)
  if (index > -1) {
    comments.value.splice(index, 1)
    alert('评论已删除')
  }
}

const searchUsers = () => {
  alert('搜索用户功能')
}

const searchArticles = () => {
  alert('搜索文章功能')
}

const searchComments = () => {
  alert('搜索评论功能')
}

const logout = async () => {
  await userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-container {
  display: flex;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.admin-sidebar {
  width: 200px;
  background: #2d3748;
  color: white;
  min-height: 100vh;
}

.sidebar-title {
  padding: 20px;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid #4a5568;
}

.sidebar-nav {
  padding-top: 20px;
}

.sidebar-nav button {
  display: block;
  width: 100%;
  padding: 14px 20px;
  background: none;
  border: none;
  color: #a0aec0;
  text-align: left;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background: #4a5568;
    color: white;
  }
  
  &.active {
    background: #667eea;
    color: white;
  }
}

.admin-main {
  flex: 1;
  padding: 20px;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.admin-header h2 {
  font-size: 24px;
  font-weight: 600;
}

.tab-content {
  background: white;
  border-radius: 8px;
  padding: 20px;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.search-input {
  width: 300px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 24px;
  border-radius: 12px;
  color: white;
  text-align: center;
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.chart-section {
  background: #f8f9fa;
  padding: 24px;
  border-radius: 8px;
}

.chart-section h3 {
  margin-bottom: 20px;
}

.chart-placeholder {
  text-align: center;
  padding: 40px;
  color: #999;
}
</style>
