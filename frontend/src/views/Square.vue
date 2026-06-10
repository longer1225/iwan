<template>
  <div class="square-container">
    <header class="header">
      <div class="logo">iwan</div>
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
        <el-button v-else type="success" @click="goToWrite">写文章</el-button>
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
  background-color: var(--bg-primary, #f5f5f5);
  padding-bottom: 80px;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: var(--bg-secondary, white);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  color: #667eea;
}

.search-box {
  flex: 1;
  max-width: 500px;
  margin: 0 40px;
}

.header-right {
  display: flex;
  gap: 10px;
}

.content-area {
  display: flex;
  padding: 80px 20px 20px;
  max-width: 1400px;
  margin: 0 auto;
  gap: 20px;
}

.sidebar {
  width: 240px;
  flex-shrink: 0;
}

.section {
  background: var(--bg-secondary, white);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.section h3 {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary, #333);
  margin-bottom: 12px;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.category-item {
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 14px;
  color: var(--text-secondary, #666);
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    background-color: var(--hover-bg, #f5f5f5);
  }
  
  &.active {
    background-color: #667eea;
    color: white;
  }
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-list .el-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.tag-list .el-tag.active {
  background-color: #667eea;
  color: white;
}

.main-content {
  flex: 1;
  min-width: 0;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.sort-tabs {
  display: flex;
  gap: 4px;
  background-color: var(--bg-secondary, #f5f5f5);
  padding: 4px;
  border-radius: 8px;
}

.sort-tabs button {
  padding: 8px 20px;
  border: none;
  background: transparent;
  border-radius: 6px;
  font-size: 14px;
  color: var(--text-secondary, #666);
  cursor: pointer;
  transition: all 0.3s;
  
  &.active {
    background-color: var(--bg-primary, white);
    color: #667eea;
    font-weight: 500;
  }
}

.result-count {
  font-size: 14px;
  color: var(--text-tertiary, #999);
}

/* 小红书风格网格布局 */
.article-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.article-card {
  background: var(--bg-secondary, white);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
    transform: translateY(-4px);
  }
}

.article-cover-wrapper {
  position: relative;
  width: 100%;
  padding-top: 100%; /* 正方形比例 */
}

.article-cover {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  &.has-video {
    img {
      filter: brightness(0.8);
    }
  }
}

.default-cover {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.8);
  font-size: 48px;
}

.video-overlay, .audio-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 60px;
  height: 60px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  
  .el-icon-video-play {
    margin-left: 4px;
  }
}

.audio-overlay {
  font-size: 20px;
}

.cover-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
}

.stats {
  display: flex;
  gap: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: white;
  
  i {
    font-size: 12px;
  }
}

.article-info {
  padding: 16px;
}

.article-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary, #333);
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-summary {
  font-size: 14px;
  color: var(--text-secondary, #666);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 12px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-info .avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
}

.author-name {
  font-size: 13px;
  color: var(--text-secondary, #666);
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
