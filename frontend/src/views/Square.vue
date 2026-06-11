<template>
  <div class="square-container">
    <header class="header glass-effect">
      <div class="logo-container">
        <div class="logo">iwan</div>
        <span class="logo-subtitle">分享你的故事</span>
      </div>
      <div class="search-box">
        <el-input 
          v-model="searchKeyword" 
          placeholder="搜索文章、作者、标签..."
          prefix-icon="search"
          size="large"
          @keyup.enter="handleSearch"
        />
      </div>
      <div class="header-right">
        <el-button v-if="!isLoggedIn" type="primary" @click="goToLogin">登录</el-button>
        <el-button v-else class="gradient-btn" @click="goToWrite">写文章</el-button>
      </div>
    </header>
    
    <div class="content-area">
      <aside class="sidebar">
        <div class="section">
          <h3>分类</h3>
          <div class="category-list">
            <div 
              v-for="cat in categories" 
              :key="cat.id" 
              class="category-item"
              :class="{ active: selectedCategory === cat.id }"
              @click="handleCategoryClick(cat)"
            >
              {{ cat.name }}
            </div>
          </div>
        </div>
        
        <div class="section">
          <h3>热门标签</h3>
          <div class="tag-list">
            <el-tag 
              v-for="tag in hotTags" 
              :key="tag.id" 
              size="small"
              @click="handleTagClick(tag)"
              :class="{ active: selectedTag === tag.id }"
            >
              {{ tag.name }}
            </el-tag>
          </div>
        </div>
      </aside>
      
      <main class="main-content">
        <div class="filter-bar">
          <div class="sort-tabs">
            <button 
              v-for="tab in sortTabs" 
              :key="tab.value"
              :class="{ active: sortType === tab.value }"
              @click="handleSortChange(tab.value)"
            >
              {{ tab.label }}
            </button>
          </div>
          <span class="result-count">共 {{ total }} 篇文章</span>
        </div>
        
        <!-- 小红书风格封面网格布局 -->
        <div class="article-grid">
          <article 
            v-for="article in articles" 
            :key="article.id" 
            class="article-card"
            @click="goToDetail(article.id)"
          >
            <div class="article-cover-wrapper">
              <div class="article-cover" :class="{ 'has-video': article.mediaType === 'video' }">
                <img v-if="article.cover" :src="article.cover" alt="封面" />
                <div v-else class="default-cover">
                  <i class="el-icon-file-text"></i>
                </div>
                <!-- 视频标识 -->
                <div v-if="article.mediaType === 'video'" class="video-overlay">
                  <i class="el-icon-video-play"></i>
                </div>
                <!-- 音频标识 -->
                <div v-if="article.mediaType === 'audio'" class="audio-overlay">
                  <i class="el-icon-headphones"></i>
                </div>
              </div>
              <!-- 封面底部信息 -->
              <div class="cover-footer">
                <div class="stats">
                  <span class="stat-item">
                    <i class="el-icon-view"></i> {{ article.readCount || 0 }}
                  </span>
                  <span class="stat-item">
                    <i class="el-icon-heart"></i> {{ article.likeCount || 0 }}
                  </span>
                  <span class="stat-item">
                    <i class="el-icon-chat-dot-round"></i> {{ article.commentCount || 0 }}
                  </span>
                </div>
              </div>
            </div>
            <!-- 标题和摘要 -->
            <div class="article-info">
              <h3 class="article-title">{{ article.title }}</h3>
              <p class="article-summary">{{ article.summary }}</p>
              <!-- 作者信息 -->
              <div class="author-info">
                <img :src="article.authorAvatar || defaultAvatar" class="avatar" />
                <span class="author-name">{{ article.authorName || '匿名用户' }}</span>
              </div>
            </div>
          </article>
        </div>
        
        <div v-if="articles.length === 0" class="empty-state">
          <el-empty description="暂无文章" />
        </div>
        
        <el-pagination
          v-if="total > pageSize"
          :total="total"
          :page-size="pageSize"
          v-model:current-page="pageNum"
          layout="prev, pager, next, jumper"
          @current-change="handlePageChange"
          class="pagination"
        />
      </main>
    </div>
    
    <BottomNav active="square" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { articleApi } from '@/api/article'
import BottomNav from '@/components/BottomNav.vue'

const router = useRouter()
const userStore = useUserStore()

const defaultAvatar = 'https://neeko-copilot.bytedance.net/api/text_to_image?prompt=professional%20avatar%20portrait%20minimalist&image_size=square'

const searchKeyword = ref('')
const sortType = ref('time')
const selectedTag = ref(null)
const selectedCategory = ref(null)
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)

const articles = ref([])
const categories = ref([])
const hotTags = ref([])

const isLoggedIn = userStore.isLoggedIn

const sortTabs = [
  { label: '最新', value: 'time' },
  { label: '最热', value: 'hot' }
]

const loadArticles = async () => {
  const params = {
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    keyword: searchKeyword.value,
    categoryId: selectedCategory.value,
    tagId: selectedTag.value,
    sortBy: sortType.value
  }
  
  try {
    const response = await articleApi.list(params)
    if (response.code === 200) {
      articles.value = response.data.records
      total.value = response.data.total
    }
  } catch (error) {
    console.error('加载文章失败:', error)
  }
}

const loadCategories = async () => {
  categories.value = [
    { id: null, name: '全部' },
    { id: '1', name: '技术博客' },
    { id: '2', name: '生活随笔' },
    { id: '3', name: '读书笔记' },
    { id: '4', name: 'AI人工智能' }
  ]
}

const loadHotTags = async () => {
  hotTags.value = [
    { id: '1', name: 'Vue' },
    { id: '2', name: 'React' },
    { id: '3', name: 'Java' },
    { id: '4', name: 'Python' },
    { id: '5', name: 'AI' },
    { id: '6', name: '数据库' },
    { id: '7', name: '前端' },
    { id: '8', name: '后端' }
  ]
}

const handleSearch = () => {
  pageNum.value = 1
  loadArticles()
}

const handleCategoryClick = (cat) => {
  selectedCategory.value = selectedCategory.value === cat.id ? null : cat.id
  pageNum.value = 1
  loadArticles()
}

const handleTagClick = (tag) => {
  selectedTag.value = selectedTag.value === tag.id ? null : tag.id
  pageNum.value = 1
  loadArticles()
}

const handleSortChange = (value) => {
  sortType.value = value
  pageNum.value = 1
  loadArticles()
}

const handlePageChange = (page) => {
  pageNum.value = page
  loadArticles()
}

const goToDetail = (id) => {
  router.push(`/article/${id}`)
}

const goToLogin = () => {
  router.push('/login')
}

const goToWrite = () => {
  router.push('/write')
}

onMounted(() => {
  loadArticles()
  loadCategories()
  loadHotTags()
})
</script>

<style scoped>
.square-container {
  min-height: 100vh;
  background-color: var(--bg-primary);
  padding-bottom: 80px;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  z-index: 100;
}

.logo-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.logo {
  font-size: 26px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--accent-gradient-1), var(--accent-gradient-2));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.logo-subtitle {
  font-size: 11px;
  color: var(--text-tertiary);
  margin-top: 2px;
}

.search-box {
  flex: 1;
  max-width: 520px;
  margin: 0 48px;
}

.header-right {
  display: flex;
  gap: 12px;
}

.content-area {
  display: flex;
  padding: 88px 24px 24px;
  max-width: 1440px;
  margin: 0 auto;
  gap: 24px;
}

.sidebar {
  width: 250px;
  flex-shrink: 0;
}

.section {
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: var(--shadow-sm);
}

.section h3 {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
  padding-left: 8px;
  border-left: 3px solid var(--accent-color);
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.category-item {
  padding: 10px 14px;
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.25s ease;
  display: flex;
  align-items: center;
  
  &:hover {
    background-color: var(--hover-bg);
    padding-left: 18px;
  }
  
  &.active {
    background: linear-gradient(135deg, rgba(79, 172, 254, 0.15), rgba(0, 242, 254, 0.15));
    color: var(--accent-color);
    font-weight: 500;
    padding-left: 18px;
  }
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-list .el-tag {
  cursor: pointer;
  transition: all 0.25s ease;
  background-color: var(--bg-tertiary);
  border: none;
  color: var(--text-secondary);
  
  &:hover {
    transform: translateY(-2px);
    background-color: var(--hover-bg);
  }
  
  &.active {
    background: linear-gradient(135deg, var(--accent-gradient-1), var(--accent-gradient-2));
    color: white;
    transform: translateY(-2px);
  }
}

.main-content {
  flex: 1;
  min-width: 0;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.sort-tabs {
  display: flex;
  gap: 4px;
  background-color: var(--bg-tertiary);
  padding: 4px;
  border-radius: var(--radius-sm);
}

.sort-tabs button {
  padding: 10px 24px;
  border: none;
  background: transparent;
  border-radius: calc(var(--radius-sm) - 2px);
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.25s ease;
  
  &:hover {
    background-color: var(--hover-bg);
  }
  
  &.active {
    background-color: var(--bg-secondary);
    color: var(--accent-color);
    font-weight: 600;
    box-shadow: var(--shadow-sm);
  }
}

.result-count {
  font-size: 14px;
  color: var(--text-tertiary);
  font-weight: 500;
}

/* Instagram风格网格布局 */
.article-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(290px, 1fr));
  gap: 24px;
}

.article-card {
  background: var(--bg-secondary, white);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:hover {
    box-shadow: var(--shadow-lg);
    transform: translateY(-6px) scale(1.01);
  }
}

.article-cover-wrapper {
  position: relative;
  width: 100%;
  padding-top: 100%; /* 正方形比例 - Instagram风格 */
}

.article-cover {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--accent-gradient-1) 0%, var(--accent-gradient-2) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s ease;
  }
  
  &.has-video {
    img {
      filter: brightness(0.85);
    }
  }
  
  &:hover img {
    transform: scale(1.05);
  }
}

.default-cover {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.9);
  font-size: 56px;
}

.video-overlay, .audio-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 68px;
  height: 68px;
  background: rgba(0, 0, 0, 0.7);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translate(-50%, -50%) scale(1.1);
    background: rgba(0, 0, 0, 0.85);
  }
  
  .el-icon-video-play {
    margin-left: 5px;
  }
}

.audio-overlay {
  font-size: 22px;
}

.cover-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.75), transparent);
}

.stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  font-weight: 500;
  color: white;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  
  i {
    font-size: 14px;
  }
}

.article-info {
  padding: 18px;
}

.article-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

.article-summary {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 14px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-info .avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 2px solid var(--border-color);
  transition: all 0.3s ease;
}

.author-info:hover .avatar {
  border-color: var(--accent-color);
}

.author-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  transition: color 0.3s ease;
}

.author-info:hover .author-name {
  color: var(--accent-color);
}

.empty-state {
  padding: 60px;
  text-align: center;
}

.pagination {
  margin-top: 30px;
  text-align: center;
}
</style>
