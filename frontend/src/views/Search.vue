<template>
  <div class="search-page">
    <!-- 搜索框 -->
    <div class="search-header">
      <div class="search-bar">
        <el-input 
          v-model="keyword" 
          placeholder="搜索文章、用户..." 
          class="search-input"
          @keyup.enter="handleSearch"
        />
        <el-button @click="handleSearch" class="search-btn">搜索</el-button>
      </div>
      
      <!-- 搜索类型切换 -->
      <div class="search-tabs">
        <button 
          v-for="tab in searchTabs" 
          :key="tab.key"
          @click="searchType = tab.key"
          :class="{ active: searchType === tab.key }"
        >
          {{ tab.label }}
        </button>
      </div>
    </div>

    <!-- 热门搜索 -->
    <div class="hot-search" v-if="!keyword && !searchResults">
      <h3 class="section-title">🔥 热门搜索</h3>
      <div class="hot-keywords">
        <button 
          v-for="(item, index) in hotKeywords" 
          :key="item.keyword"
          @click="keyword = item.keyword; handleSearch()"
          class="keyword-tag"
        >
          <span class="rank" v-if="index < 3">{{ index + 1 }}</span>
          {{ item.keyword }}
          <span class="count">{{ item.count }}</span>
        </button>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div class="search-results" v-if="searchResults">
      <!-- 文章结果 -->
      <div v-if="searchResults.articles && searchResults.articles.length > 0" class="results-section">
        <h3 class="section-title">📝 文章结果 ({{ searchResults.articles.length }})</h3>
        <div class="article-results">
          <article 
            v-for="article in searchResults.articles" 
            :key="article.id"
            class="result-item"
            @click="goToArticle(article.id)"
          >
            <img :src="article.cover" class="result-cover" />
            <div class="result-content">
              <h4 class="result-title">{{ article.title }}</h4>
              <p class="result-summary">{{ article.summary }}</p>
              <div class="result-meta">
                <div class="author-info">
                  <img :src="article.authorAvatar || '/api/avatar/default'" class="author-avatar" />
                  <span>{{ article.authorName }}</span>
                </div>
                <div class="stats">
                  <span>👁️ {{ article.readCount }}</span>
                  <span>❤️ {{ article.likeCount }}</span>
                  <span>💬 {{ article.commentCount }}</span>
                </div>
              </div>
            </div>
          </article>
        </div>
      </div>

      <!-- 用户结果 -->
      <div v-if="searchResults.users && searchResults.users.length > 0" class="results-section">
        <h3 class="section-title">👤 用户结果 ({{ searchResults.users.length }})</h3>
        <div class="user-results">
          <div 
            v-for="user in searchResults.users" 
            :key="user.id"
            class="user-item"
            @click="goToProfile(user.id)"
          >
            <img :src="user.avatar || '/api/avatar/default'" class="user-avatar" />
            <div class="user-info">
              <h4>{{ user.nickname }}</h4>
              <p>{{ user.bio || '暂无简介' }}</p>
            </div>
            <el-button class="follow-btn">关注</el-button>
          </div>
        </div>
      </div>

      <!-- 无结果 -->
      <div v-if="!searchResults.articles?.length && !searchResults.users?.length" class="empty-state">
        <div class="empty-icon">🔍</div>
        <p>未找到相关结果</p>
        <p class="empty-hint">试试其他关键词吧</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { searchApi } from '@/api/search'

const router = useRouter()

const keyword = ref('')
const searchType = ref('all')
const searchResults = ref(null)
const hotKeywords = ref([])

const searchTabs = [
  { key: 'all', label: '全部' },
  { key: 'article', label: '文章' },
  { key: 'user', label: '用户' }
]

const handleSearch = async () => {
  if (!keyword.value.trim()) {
    searchResults.value = null
    return
  }
  
  try {
    const response = await searchApi.search(keyword.value, searchType.value)
    if (response.code === 200) {
      searchResults.value = response.data
    }
  } catch (error) {
    console.error('搜索失败:', error)
    searchResults.value = { articles: [], users: [] }
  }
}

const loadHotKeywords = async () => {
  try {
    const response = await searchApi.hotKeywords()
    if (response.code === 200) {
      hotKeywords.value = response.data
    }
  } catch (error) {
    console.error('加载热门搜索失败:', error)
  }
}

const goToArticle = (id) => {
  router.push(`/article/${id}`)
}

const goToProfile = (id) => {
  router.push(`/profile/${id}`)
}

onMounted(() => {
  loadHotKeywords()
})
</script>

<style scoped>
.search-page {
  min-height: 100vh;
  background-color: var(--bg-primary, #f5f5f5);
  padding: 20px;
}

.search-header {
  max-width: 800px;
  margin: 0 auto 30px;
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.search-input {
  flex: 1;
  border-radius: 25px;
  padding: 12px 20px;
  font-size: 16px;
}

.search-btn {
  border-radius: 25px;
  padding: 0 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
}

.search-tabs {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.search-tabs button {
  padding: 8px 20px;
  border: none;
  border-radius: 20px;
  background: var(--bg-secondary, white);
  color: var(--text-secondary, #666);
  cursor: pointer;
  transition: all 0.3s;
  
  &.active {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
  }
  
  &:hover {
    opacity: 0.8;
  }
}

.hot-search {
  max-width: 800px;
  margin: 0 auto;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary, #333);
  margin-bottom: 16px;
}

.hot-keywords {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.keyword-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: var(--bg-secondary, white);
  border-radius: 20px;
  border: none;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    background: #eef2ff;
    transform: translateY(-2px);
  }
}

.rank {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
  
  &:nth-child(1) {
    background: #ff6b6b;
    color: white;
  }
  
  &:nth-child(2) {
    background: #feca57;
    color: white;
  }
  
  &:nth-child(3) {
    background: #48dbfb;
    color: white;
  }
}

.count {
  font-size: 12px;
  color: var(--text-tertiary, #999);
}

.search-results {
  max-width: 800px;
  margin: 0 auto;
}

.results-section {
  margin-bottom: 30px;
}

.article-results {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: var(--bg-secondary, white);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    transform: translateX(4px);
    box-shadow: var(--shadow-md, 0 2px 8px rgba(0,0,0,0.08));
  }
}

.result-cover {
  width: 120px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.result-content {
  flex: 1;
  min-width: 0;
}

.result-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary, #333);
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-summary {
  font-size: 14px;
  color: var(--text-secondary, #666);
  margin: 0 0 10px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
}

.result-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
}

.author-info span {
  font-size: 13px;
  color: var(--text-secondary, #666);
}

.stats {
  display: flex;
  gap: 12px;
  
  span {
    font-size: 12px;
    color: var(--text-tertiary, #999);
  }
}

.user-results {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--bg-secondary, white);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    background: var(--hover-bg, #f8f9fa);
  }
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.user-info {
  flex: 1;
}

.user-info h4 {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary, #333);
  margin: 0 0 4px 0;
}

.user-info p {
  font-size: 13px;
  color: var(--text-secondary, #666);
  margin: 0;
}

.follow-btn {
  padding: 6px 16px;
  border-radius: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  font-size: 13px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state p {
  color: var(--text-secondary, #666);
  margin: 0 0 8px 0;
}

.empty-hint {
  color: var(--text-tertiary, #999) !important;
}
</style>
