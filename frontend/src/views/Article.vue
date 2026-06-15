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
          <span class="stat-item">★ {{ article.collectCount || 0 }}</span>
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
      <div v-if="userStore.isLoggedIn" class="comment-options">
        <label class="anonymous-comment-label">
          <el-switch v-model="isAnonymousComment" />
          <span>匿名评论</span>
        </label>
      </div>
      <el-button type="primary" @click="submitComment">发表</el-button>
    </div>
    
    <div class="comments-section">
      <h3 class="comments-title">评论 ({{ comments.length }})</h3>
      <div v-if="comments.length === 0" class="no-comments">
        <p>暂无评论，来发表第一条评论吧！</p>
      </div>
      <div v-else class="comments-list">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <img :src="comment.userAvatar || defaultAvatar" class="comment-avatar" />
          <div class="comment-content">
            <div class="comment-header">
              <span class="comment-author">{{ comment.userName }}</span>
              <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
            </div>
            <p class="comment-text">{{ comment.content }}</p>
            <div class="comment-actions">
              <button class="comment-action-btn" @click="toggleCommentLike(comment)">
                <span class="action-icon">❤️</span>
                <span>{{ comment.likeCount || 0 }}</span>
              </button>
              <button class="comment-action-btn" @click="replyToComment(comment)">
                <span class="action-icon">💬</span>
                <span>回复</span>
              </button>
            </div>
            
            <!-- 回复列表 -->
            <div v-if="comment.replies && comment.replies.length > 0" class="replies-list">
              <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                <img :src="reply.userAvatar || defaultAvatar" class="reply-avatar" />
                <div class="reply-content">
                  <div class="reply-header">
                    <span class="reply-author">{{ reply.userName }}</span>
                    <span class="reply-time">{{ formatDate(reply.createTime) }}</span>
                  </div>
                  <p class="reply-text">{{ reply.content }}</p>
                  <button class="reply-action-btn" @click="toggleCommentLike(reply)">
                    <span>❤️</span>
                    <span>{{ reply.likeCount || 0 }}</span>
                  </button>
                </div>
              </div>
            </div>
            
            <!-- 回复输入框 -->
            <div v-if="replyTargetId === comment.id" class="reply-input-area">
              <input v-model="replyText" placeholder="写下你的回复..." @keyup.enter="submitReply" />
              <button class="reply-submit-btn" @click="submitReply">发送</button>
            </div>
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

const defaultAvatar = '/api/v1/upload/avatar/default'

const article = ref(null)
const comments = ref([])
const isLiked = ref(false)
const isCollected = ref(false)
const showCommentInput = ref(false)
const commentText = ref('')
const isAnonymousComment = ref(false)
const replyTargetId = ref(null)
const replyText = ref('')
const isAnonymousReply = ref(false)

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
      // 加载每个评论的回复
      for (const comment of comments.value) {
        await loadReplies(comment)
      }
    }
  } catch (error) {
    console.error('加载评论失败:', error)
  }
}

const loadReplies = async (comment) => {
  try {
    const response = await commentApi.getReplies(comment.id)
    if (response.code === 200) {
      comment.replies = response.data || []
    }
  } catch (error) {
    console.error('加载回复失败:', error)
    comment.replies = []
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
    // 更新文章点赞数
    if (article.value) {
      article.value.likeCount = (article.value.likeCount || 0) + (isLiked.value ? 1 : -1)
    }
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
    // 更新文章收藏数
    if (article.value) {
      article.value.collectCount = (article.value.collectCount || 0) + (isCollected.value ? 1 : -1)
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
  }
}

const submitComment = async () => {
  if (!commentText.value.trim()) return
  
  try {
    // 未登录用户默认匿名评论，已登录用户可选择匿名或非匿名
    const isAnonymous = userStore.isLoggedIn ? isAnonymousComment.value : true
    const userName = isAnonymous ? '匿名用户' : (userStore.user?.nickname || '匿名用户')
    const userAvatar = isAnonymous ? '' : (userStore.user?.avatar || '')
    
    await commentApi.create({
      articleId: route.params.id,
      content: commentText.value.trim(),
      userName,
      userAvatar,
      anonymous: isAnonymous
    })
    commentText.value = ''
    showCommentInput.value = false
    isAnonymousComment.value = false
    // 更新文章评论数
    if (article.value) {
      article.value.commentCount = (article.value.commentCount || 0) + 1
    }
    await loadComments()
  } catch (error) {
    console.error('发表评论失败:', error)
  }
}

const toggleCommentLike = async (commentItem) => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  
  try {
    const response = await commentApi.like(commentItem.id)
    if (response.code === 200) {
      commentItem.likeCount = response.data.likeCount
    }
  } catch (error) {
    console.error('评论点赞失败:', error)
  }
}

const replyToComment = (comment) => {
  if (replyTargetId.value === comment.id) {
    replyTargetId.value = null
    replyText.value = ''
  } else {
    replyTargetId.value = comment.id
    replyText.value = ''
  }
}

const submitReply = async () => {
  if (!replyText.value.trim() || !replyTargetId.value) return
  
  try {
    const isAnonymous = userStore.isLoggedIn ? isAnonymousReply.value : true
    const userName = isAnonymous ? '匿名用户' : (userStore.user?.nickname || '匿名用户')
    const userAvatar = isAnonymous ? '' : (userStore.user?.avatar || '')
    
    await commentApi.create({
      articleId: route.params.id,
      parentId: replyTargetId.value,
      content: replyText.value.trim(),
      userName,
      userAvatar,
      anonymous: isAnonymous
    })
    replyText.value = ''
    isAnonymousReply.value = false
    // 更新文章评论数
    if (article.value) {
      article.value.commentCount = (article.value.commentCount || 0) + 1
    }
    await loadComments()
  } catch (error) {
    console.error('回复评论失败:', error)
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
  background-color: var(--bg-primary);
  padding-bottom: 140px;
}

.article-header {
  padding: 16px 24px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: var(--shadow-sm);
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  background: var(--bg-tertiary);
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  transition: all 0.25s ease;
}

.back-btn:hover {
  background: var(--hover-bg);
  transform: translateX(-4px);
}

.back-icon {
  font-size: 18px;
}

.article-content {
  max-width: 850px;
  margin: 0 auto;
  padding: 32px 24px;
}

.article-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 24px;
  line-height: 1.4;
  letter-spacing: -0.5px;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 28px;
  padding: 20px;
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.author-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid var(--border-color);
  transition: all 0.3s ease;
}

.author-avatar:hover {
  border-color: var(--accent-color);
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.author-name {
  font-weight: 600;
  font-size: 16px;
  color: var(--text-primary);
}

.publish-time {
  font-size: 13px;
  color: var(--text-tertiary);
}

.article-stats {
  margin-left: auto;
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  padding: 8px 14px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
  transition: all 0.25s ease;
  
  &:hover {
    background: var(--hover-bg);
  }
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
  gap: 24px;
  padding: 16px 24px;
  background: var(--bg-secondary);
  border-top: 1px solid var(--border-color);
  box-shadow: 0 -4px 20px var(--shadow-sm);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 32px;
  background: var(--bg-tertiary);
  border: none;
  border-radius: var(--radius-xl);
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  color: var(--text-secondary);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.action-btn:hover {
  background: var(--hover-bg);
  transform: translateY(-2px);
}

.action-btn.active {
  background: linear-gradient(135deg, rgba(237, 73, 86, 0.15), rgba(237, 73, 86, 0.05));
  color: var(--like-color);
}

.action-icon {
  font-size: 18px;
}

.comment-input-area {
  position: fixed;
  bottom: 80px;
  left: 0;
  right: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px 24px;
  background: var(--bg-secondary);
  border-top: 1px solid var(--border-color);
  box-shadow: 0 -4px 20px var(--shadow-sm);
}

.comment-input-area input {
  flex: 1;
  padding: 14px 20px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  font-size: 15px;
  background: var(--bg-tertiary);
  transition: all 0.25s ease;
  
  &:focus {
    outline: none;
    border-color: var(--accent-color);
    background: var(--bg-secondary);
    box-shadow: 0 0 0 3px rgba(79, 172, 254, 0.1);
  }
}

.comment-input-area button {
  padding: 14px 32px;
  border-radius: var(--radius-xl);
  font-weight: 600;
  font-size: 15px;
  background: linear-gradient(135deg, var(--accent-gradient-1), var(--accent-gradient-2));
  border: none;
  color: white;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 15px rgba(79, 172, 254, 0.4);
  }
}

.comment-options {
  display: flex;
  align-items: center;
  padding: 0 8px;
}

.anonymous-comment-label {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
}

.comments-section {
  max-width: 850px;
  margin: 0 auto;
  padding: 32px 24px;
}

.comments-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 24px;
}

.no-comments {
  padding: 60px 24px;
  text-align: center;
  color: var(--text-tertiary);
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.comment-item {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  transition: all 0.25s ease;
  
  &:hover {
    box-shadow: var(--shadow-md);
    transform: translateX(4px);
  }
}

.comment-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--border-color);
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.comment-author {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 15px;
}

.comment-time {
  font-size: 13px;
  color: var(--text-tertiary);
}

.comment-text {
  font-size: 15px;
  color: var(--text-secondary);
  line-height: 1.7;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  gap: 20px;
  margin-top: 12px;
}

.comment-action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: none;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: 14px;
  color: var(--text-tertiary);
  transition: all 0.25s ease;
  
  &:hover {
    background: var(--hover-bg);
    color: var(--text-secondary);
  }
}

.replies-list {
  margin-top: 16px;
  padding-left: 32px;
  border-left: 2px solid var(--border-color);
}

.reply-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid var(--border-color);
  
  &:last-child {
    border-bottom: none;
  }
}

.reply-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid var(--border-color);
  flex-shrink: 0;
}

.reply-content {
  flex: 1;
  min-width: 0;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.reply-author {
  font-weight: 500;
  color: var(--text-primary);
  font-size: 14px;
}

.reply-time {
  font-size: 12px;
  color: var(--text-tertiary);
}

.reply-text {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  word-break: break-word;
}

.reply-action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  padding: 4px 10px;
  background: none;
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 13px;
  color: var(--text-tertiary);
  transition: all 0.25s ease;
  
  &:hover {
    background: var(--hover-bg);
    color: var(--text-secondary);
  }
}

.reply-input-area {
  display: flex;
  gap: 10px;
  margin-top: 16px;
  padding: 12px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-lg);
}

.reply-input-area input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: 14px;
  background: var(--bg-secondary);
  transition: all 0.25s ease;
  
  &:focus {
    outline: none;
    border-color: var(--accent-color);
    box-shadow: 0 0 0 2px rgba(79, 172, 254, 0.1);
  }
}

.reply-submit-btn {
  padding: 10px 20px;
  border-radius: var(--radius-md);
  font-weight: 500;
  font-size: 14px;
  background: linear-gradient(135deg, var(--accent-gradient-1), var(--accent-gradient-2));
  border: none;
  color: white;
  cursor: pointer;
  transition: all 0.25s ease;
  
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 2px 10px rgba(79, 172, 254, 0.3);
  }
}

.dark-theme .article-header,
.dark-theme .article-content,
.dark-theme .article-actions,
.dark-theme .comment-input-area {
  background: var(--bg-secondary);
}

.dark-theme .article-title,
.dark-theme .author-name,
.dark-theme .comment-author {
  color: var(--text-primary);
}

.dark-theme .stat-item,
.dark-theme .publish-time,
.dark-theme .article-body,
.dark-theme .comment-text,
.dark-theme .comment-time,
.dark-theme .no-comments {
  color: var(--text-secondary);
}

.dark-theme .article-meta,
.dark-theme .comment-item {
  border-color: var(--border-color);
}

.dark-theme .action-btn {
  background: var(--bg-tertiary);
  color: var(--text-secondary);
}

.dark-theme .action-btn:hover {
  background: var(--hover-bg);
}

.dark-theme .comment-input-area input {
  background: var(--bg-tertiary);
  border-color: var(--border-color);
}
</style>