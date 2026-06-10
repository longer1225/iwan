<template>
  <div class="square-container">
    <header class="header">
      <div class="logo">Iwan</div>
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
          <el-tree 
            :data="categories" 
            :props="treeProps"
            :expand-on-click-node="false"
            @node-click="handleCategoryClick"
          />
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
          <el-select 
            v-model="sortType" 
            placeholder="排序方式" 
            size="small"
          >
            <el-option label="最新发布" value="newest" />
            <el-option label="最热点赞" value="likes" />
            <el-option label="最多收藏" value="collects" />
          </el-select>
          <span class="result-count">共 {{ total }} 篇文章</span>
        </div>
        
        <div class="article-list">
          <article 
            v-for="article in articles" 
            :key="article.id" 
            class="article-card"
            @click="goToDetail(article.id)"
          >
            <div class="article-cover">
              <img :src="article.cover || defaultCover" alt="封面" />
            </div>
            <div class="article-content">
              <h3 class="article-title">{{ article.title }}</h3>
              <p class="article-summary">{{ article.summary }}</p>
              <div class="article-meta">
                <span class="author">
                  <img :src="article.authorAvatar" class="avatar" />
                  {{ article.authorName }}
                </span>
                <span class="time">{{ formatRelativeTime(article.createTime) }}</span>
                <span class="category">{{ article.categoryName }}</span>
              </div>
              <div class="article-tags">
                <el-tag 
                  v-for="tag in article.tags" 
                  :key="tag" 
                  size="small" 
                  type="info"
                >
                  {{ tag }}
                </el-tag>
              </div>
              <div class="article-stats">
                <span><i class="eye"></i> {{ article.readCount }}</span>
                <span><i class="heart"></i> {{ article.likeCount }}</span>
                <span><i class="bookmark"></i> {{ article.collectCount }}</span>
                <span><i class="message"></i> {{ article.commentCount }}</span>
              </div>
            </div>
          </article>
        </div>
        
        <el-pagination
          v-if="total > pageSize"
          :total="total"
          :page-size="pageSize"
          v-model:current-page="pageNum"
          layout="prev, pager, next, jumper"
          @current-change="handlePageChange"
        />
      </main>
    </div>
    
    <BottomNav active="square" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { articleApi } from '@/api/article'
import { formatRelativeTime } from '@/utils/format'
import BottomNav from '@/components/BottomNav.vue'

const router = useRouter()
const userStore = useUserStore()

const defaultCover = 'https://neeko-copilot.bytedance.net/api/text_to_image?prompt=minimalist%20blog%20cover%20art%20abstract%20gradient&image_size=landscape_16_9'

const searchKeyword = ref('')
const sortType = ref('newest')
const selectedTag = ref(null)
const selectedCategory = ref(null)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const articles = ref([])
const categories = ref([])
const hotTags = ref([])

const isLoggedIn = userStore.isLoggedIn

const treeProps = {
  label: 'name',
  children: 'children'
}

const loadArticles = async () => {
  const params = {
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    sortField: sortType.value === 'newest' ? 'create_time' : sortType.value === 'likes' ? 'like_count' : 'collect_count',
    sortOrder: 'desc',
    filter: {
      keyword: searchKeyword.value,
      tagId: selectedTag.value,
      categoryId: selectedCategory.value
    }
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
    { id: null, name: '全部', children: [] },
    { id: 1, name: '技术博客', children: [{ id: 11, name: '前端开发' }, { id: 12, name: '后端开发' }] },
    { id: 2, name: '生活随笔', children: [] },
    { id: 3, name: '读书笔记', children: [] },
    { id: 4, name: 'AI人工智能', children: [] }
  ]
}

const loadHotTags = async () => {
  hotTags.value = [
    { id: 1, name: 'Vue' },
    { id: 2, name: 'React' },
    { id: 3, name: 'Java' },
    { id: 4, name: 'Python' },
    { id: 5, name: 'AI' },
    { id: 6, name: '数据库' },
    { id: 7, name: '前端' },
    { id: 8, name: '后端' }
  ]
}

const handleSearch = () => {
  pageNum.value = 1
  loadArticles()
}

const handleCategoryClick = (data) => {
  selectedCategory.value = data.id
  pageNum.value = 1
  loadArticles()
}

const handleTagClick = (tag) => {
  selectedTag.value = selectedTag.value === tag.id ? null : tag.id
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
  router.push('/profile')
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
  background-color: #f5f5f5;
  padding-bottom: 80px;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: white;
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
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.section h3 {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
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

.result-count {
  font-size: 14px;
  color: #999;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.article-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
  }
}

.article-cover {
  width: 200px;
  height: 140px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.article-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.article-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-summary {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 12px;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.author {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #666;
  
  .avatar {
    width: 24px;
    height: 24px;
    border-radius: 50%;
  }
}

.time, .category {
  font-size: 13px;
  color: #999;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.article-stats {
  display: flex;
  gap: 16px;
  margin-top: auto;
  
  span {
    font-size: 13px;
    color: #999;
    display: flex;
    align-items: center;
    gap: 4px;
  }
}
</style>
