<template>
  <div class="article-page">
    <div class="article-header">
      <button class="back-btn" @click="goBack">
        <span class="back-icon">←</span>
        返回
      </button>
    </div>
    
    <div v-if="article" class="article-content">
      <h1 class="article-title">{{ article.title }}</h1>
      
      <div class="article-meta">
        <img :src="article.authorAvatar || defaultAvatar" class="author-avatar" />
        <div class="author-info">
          <span class="author-name">{{ article.authorName || '匿名用户' }}</span>
          <span class="publish-time">{{ formatDate(article.createTime) }}</span>
        </div>
        <div class="article-stats">
          <span class="stat-item">👁️ {{ article.readCount || 0 }}</span>
          <span class="stat-item">❤️ {{ article.likeCount || 0 }}</span>
          <span class="stat-item">💬 {{ article.commentCount || 0 }}</span>
        </div>
      </div>
      
      <div v-if="article.cover" class="article-cover">
        <img :src="article.cover" alt="封面" />
      </div>
      
      <div class="article-body" v-html="article.content"></div>
      
      <div v-if="article.mediaList && article.mediaList.length > 0" class="article-media">
        <div v-for="(media, index) in article.mediaList" :key="index" class="media-item">
          <video v-if="media.type === 'video'" :src="media.url" controls class="media-video"></video>
          <audio v-else-if="media.type === 'audio'" :src="media.url" controls class="media-audio"></audio>
          <img v-else :src="media.url" class="media-image" />
        </div>
      </div>
    </div>
    
    <div v-else class="loading">
      <el-loading text="加载中..." />
    </div>
    
    <div class="article-actions">
      <button class="action-btn" :class="{ active: isLiked }" @click="toggleLike">
        <span class="action-icon">♥</span>
        {{ isLiked ? '已点赞' : '点赞' }}
      </button>
      <button class="action-btn" :class="{ active: isCollected }" @click="toggleCollect">
        <span class="action-icon">★</span>
        {{ isCollected ? '已收藏' : '收藏' }}
      </button>
      <button class="action-btn" @click="showCommentInput = !showCommentInput">
        <span class="action-icon">💬</span>
        评论
      </button>
    </div>
    
    <div v-if="showCommentInput" class="comment-input-area">
      <el-input v-model="commentText" placeholder="写下你的评论..." @keyup.enter="submitComment" />
      <el-button type="primary" @click="submitComment">发表</el-button>
    </div>
    
    <div class="comments-section">
      <h3 class="comments-title">评论 ({{ comments.length }})</h3>
      <div v-if="comments.length === 0" class="no-comments">
        <p>暂无评论，来发表第一条评论吧！</p>
      </div>
      <div v-else class="comments-list">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <img :src="comment.userAvatar" class="comment-avatar" />
          <div class="comment-content">
            <div class="comment-header">
              <span class="comment-author">{{ comment.userName }}</span>
              <span class="comment-time">{{ comment.createTime }}</span>
            </div>
            <p class="comment-text">{{ comment.content }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { articleApi } from '@/api/article'
import { actionApi } from '@/api/action'
import { commentApi } from '@/api/comment'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const defaultAvatar = 'https://neeko-copilot.bytedance.net/api/text_to_image?prompt=professional%20avatar%20portrait%20minimalist&image_size=square'

const article = ref(null)
const comments = ref([])
const isLiked = ref(false)
const isCollected = ref(false)
const showCommentInput = ref(false)
const commentText = ref('')

const goBack = () => {
  router.back()
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const loadArticle = async () => {
  const articleId = route.params.id
  
  if (!articleId || articleId === 'undefined') {
    console.error('文章ID无效:', articleId)
    return
  }
  
  try {
    const response = await articleApi.detail(articleId)
    if (response.code === 200) {
      article.value = response.data
      await loadComments()
      await checkLikeStatus()
      await checkCollectStatus()
    }
  } catch (error) {
    console.error('加载文章失败:', error)
  }
}

const loadComments = async () => {
  const articleId = route.params.id
  
  if (!articleId || articleId === 'undefined') {
    console.error('文章ID无效:', articleId)
    return
  }
  
  try {
    const response = await commentApi.list({ articleId })
    if (response.code === 200) {
      comments.value = response.data.records || response.data.list || []
    }
  } catch (error) {
    console.error('加载评论失败:', error)
  }
}

const checkLikeStatus = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const response = await actionApi.checkLike({ targetId: route.params.id, targetType: 'article' })
    if (response.code === 200) {
      isLiked.value = response.data
    }
  } catch (error) {
    console.error('检查点赞状态失败:', error)
  }
}

const checkCollectStatus = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const response = await actionApi.checkCollect({ targetId: route.params.id, targetType: 'article' })
    if (response.code === 200) {
      isCollected.value = response.data
    }
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

const toggleLike = async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  
  try {
    await actionApi.like({ targetId: route.params.id, targetType: 'article' })
    isLiked.value = !isLiked.value
  } catch (error) {
    console.error('点赞操作失败:', error)
  }
}

const toggleCollect = async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  
  try {
    await actionApi.collect({ targetId: route.params.id, targetType: 'article' })
    isCollected.value = !isCollected.value
  } catch (error) {
    console.error('收藏操作失败:', error)
  }
}

const submitComment = async () => {
  if (!commentText.value.trim() || !userStore.isLoggedIn) return
  
  try {
    await commentApi.create({
      articleId: route.params.id,
      content: commentText.value.trim(),
      userName: userStore.user?.nickname || '匿名用户',
      userAvatar: userStore.user?.avatar || ''
    })
    commentText.value = ''
    showCommentInput.value = false
    await loadComments()
  } catch (error) {
    console.error('发表评论失败:', error)
  }
}

onMounted(() => {
  loadArticle()
})

watch(() => route.params.id, () => {
  loadArticle()
})
</script>

<style scoped>
.article-page {
  min-height: 100vh;
  padding-bottom: 120px;
}

.article-header {
  padding: 16px 20px;
  background: #fff;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  z-index: 100;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
  color: #333;
}

.back-btn:hover {
  color: #666;
}

.back-icon {
  font-size: 18px;
}

.article-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

.article-title {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
  line-height: 1.4;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-weight: 600;
  color: #333;
}

.publish-time {
  font-size: 12px;
  color: #999;
}

.article-stats {
  margin-left: auto;
  display: flex;
  gap: 16px;
}

.stat-item {
  font-size: 12px;
  color: #999;
}

.article-cover {
  margin-bottom: 24px;
}

.article-cover img {
  width: 100%;
  border-radius: 8px;
}

.article-body {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
  word-break: break-word;
}

.article-body p {
  margin-bottom: 16px;
}

.article-body img {
  max-width: 100%;
  border-radius: 8px;
}

.article-media {
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.media-item {
  width: 100%;
}

.media-video,
.media-audio {
  width: 100%;
  max-height: 400px;
  border-radius: 8px;
}

.media-image {
  width: 100%;
  border-radius: 8px;
}

.article-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  gap: 32px;
  padding: 16px;
  background: #fff;
  border-top: 1px solid #eee;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: none;
  border: 1px solid #ddd;
  border-radius: 24px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.3s;
}

.action-btn:hover {
  border-color: #007bff;
  color: #007bff;
}

.action-btn.active {
  border-color: #ff6b6b;
  color: #ff6b6b;
}

.action-icon {
  font-size: 16px;
}

.comment-input-area {
  position: fixed;
  bottom: 70px;
  left: 0;
  right: 0;
  display: flex;
  gap: 12px;
  padding: 12px 24px;
  background: #fff;
  border-top: 1px solid #eee;
}

.comment-input-area input {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid #ddd;
  border-radius: 20px;
  font-size: 14px;
}

.comment-input-area button {
  padding: 10px 24px;
  border-radius: 20px;
}

.comments-section {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 24px;
}

.comments-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
}

.no-comments {
  padding: 40px;
  text-align: center;
  color: #999;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-text {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.dark-theme .article-header,
.dark-theme .article-content,
.dark-theme .article-actions,
.dark-theme .comment-input-area {
  background: #1a1a2e;
}

.dark-theme .article-title,
.dark-theme .author-name,
.dark-theme .comment-author {
  color: #fff;
}

.dark-theme .stat-item,
.dark-theme .publish-time,
.dark-theme .article-body,
.dark-theme .comment-text,
.dark-theme .comment-time,
.dark-theme .no-comments {
  color: #ccc;
}

.dark-theme .article-meta,
.dark-theme .comment-item {
  border-color: #333;
}

.dark-theme .action-btn {
  border-color: #444;
  color: #ccc;
}

.dark-theme .action-btn:hover {
  border-color: #007bff;
  color: #007bff;
}

.dark-theme .comment-input-area input {
  background: #2a2a4e;
  border-color: #444;
  color: #fff;
}
</style>