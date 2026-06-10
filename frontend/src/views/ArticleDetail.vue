<template>
  <div class="article-detail-container">
    <article class="article-content">
      <header class="article-header">
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-meta">
          <div class="author-info">
            <img :src="article.authorAvatar" class="avatar" />
            <div>
              <span class="author-name">{{ article.authorName }}</span>
              <span class="publish-time">{{ formatDate(article.createTime) }}</span>
            </div>
          </div>
          <div class="meta-tags">
            <el-tag size="small">{{ article.categoryName }}</el-tag>
            <el-tag 
              v-for="tag in article.tags" 
              :key="tag" 
              size="small" 
              type="info"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>
      </header>
      
      <div class="article-body" v-html="renderedContent"></div>
      
      <div class="article-stats">
        <span><i class="eye"></i> {{ article.readCount }} 阅读</span>
        <span><i class="heart"></i> {{ article.likeCount }} 点赞</span>
        <span><i class="bookmark"></i> {{ article.collectCount }} 收藏</span>
        <span><i class="message"></i> {{ article.commentCount }} 评论</span>
      </div>
      
      <div class="article-actions">
        <el-button 
          @click="handleLike" 
          :type="isLiked ? 'primary' : 'default'"
          icon="heart"
        >
          {{ isLiked ? '已点赞' : '点赞' }}
        </el-button>
        <el-button 
          @click="handleCollect" 
          :type="isCollected ? 'primary' : 'default'"
          icon="bookmark"
        >
          {{ isCollected ? '已收藏' : '收藏' }}
        </el-button>
        <el-button @click="handleShare" icon="share">分享</el-button>
      </div>
    </article>
    
    <div class="comment-section">
      <h3>评论 ({{ comments.length }})</h3>
      
      <el-form v-if="isLoggedIn" class="comment-form">
        <el-input 
          v-model="commentContent" 
          type="textarea" 
          placeholder="写下你的评论..."
          :rows="3"
        />
        <el-button type="primary" @click="submitComment" class="submit-btn">发表评论</el-button>
      </el-form>
      
      <div v-else class="guest-comment">
        <el-input 
          v-model="commentContent" 
          type="textarea" 
          placeholder="匿名评论（登录后可享受完整互动体验）"
          :rows="3"
        />
        <el-button type="primary" @click="submitAnonymousComment" class="submit-btn">匿名发表</el-button>
      </div>
      
      <div class="comment-list">
        <div 
          v-for="comment in comments" 
          :key="comment.id" 
          class="comment-item"
        >
          <div class="comment-header">
            <img :src="comment.avatar || '/avatar-default.png'" class="comment-avatar" />
            <div>
              <span class="comment-author">{{ comment.authorName || '匿名用户' }}</span>
              <span class="comment-time">{{ formatRelativeTime(comment.createTime) }}</span>
            </div>
          </div>
          <p class="comment-content">{{ comment.content }}</p>
          <div class="comment-actions">
            <span @click="handleReply(comment)" class="action-btn">回复</span>
            <span @click="handleCommentLike(comment)" class="action-btn">
              <i class="heart"></i> {{ comment.likeCount }}
            </span>
          </div>
          
          <div v-if="comment.replies && comment.replies.length > 0" class="replies">
            <div 
              v-for="reply in comment.replies" 
              :key="reply.id" 
              class="reply-item"
            >
              <span class="reply-author">{{ reply.authorName || '匿名用户' }}:</span>
              <span class="reply-content">{{ reply.content }}</span>
            </div>
          </div>
          
          <div v-if="replyingId === comment.id" class="reply-form">
            <el-input 
              v-model="replyContent" 
              placeholder="回复 {{ comment.authorName }}..."
              @keyup.enter="submitReply(comment)"
            />
            <el-button size="small" @click="submitReply(comment)">回复</el-button>
          </div>
        </div>
      </div>
    </div>
    
    <BottomNav active="square" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { articleApi } from '@/api/article'
import { commentApi } from '@/api/comment'
import { actionApi } from '@/api/action'
import { formatDate, formatRelativeTime } from '@/utils/format'
import BottomNav from '@/components/BottomNav.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const articleId = route.params.id

const article = ref({})
const comments = ref([])
const commentContent = ref('')
const replyContent = ref('')
const replyingId = ref(null)
const isLiked = ref(false)
const isCollected = ref(false)

const isLoggedIn = userStore.isLoggedIn

const renderedContent = computed(() => {
  if (!article.value.content) return ''
  return article.value.content
})

const loadArticle = async () => {
  try {
    const response = await articleApi.detail(articleId)
    if (response.code === 200) {
      article.value = response.data
    }
  } catch (error) {
    console.error('加载文章失败:', error)
  }
}

const loadComments = async () => {
  try {
    const response = await commentApi.list({ articleId })
    if (response.code === 200) {
      comments.value = response.data.records || []
    }
  } catch (error) {
    console.error('加载评论失败:', error)
  }
}

const handleLike = async () => {
  try {
    const response = await actionApi.like({
      targetId: articleId,
      targetType: 'article'
    })
    if (response.code === 200) {
      isLiked.value = !isLiked.value
      article.value.likeCount += isLiked.value ? 1 : -1
    }
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

const handleCollect = async () => {
  try {
    const response = await actionApi.collect({
      targetId: articleId,
      targetType: 'article'
    })
    if (response.code === 200) {
      isCollected.value = !isCollected.value
      article.value.collectCount += isCollected.value ? 1 : -1
    }
  } catch (error) {
    console.error('收藏失败:', error)
  }
}

const handleShare = () => {
  const url = window.location.href
  navigator.clipboard.writeText(url).then(() => {
    alert('链接已复制到剪贴板')
  })
}

const submitComment = async () => {
  if (!commentContent.value.trim()) return
  
  try {
    const response = await commentApi.create({
      articleId,
      content: commentContent.value
    })
    if (response.code === 200) {
      commentContent.value = ''
      loadComments()
    }
  } catch (error) {
    console.error('发表评论失败:', error)
  }
}

const submitAnonymousComment = async () => {
  if (!commentContent.value.trim()) return
  
  try {
    const response = await commentApi.create({
      articleId,
      content: commentContent.value,
      anonymous: true
    })
    if (response.code === 200) {
      commentContent.value = ''
      loadComments()
    }
  } catch (error) {
    console.error('发表评论失败:', error)
  }
}

const handleReply = (comment) => {
  replyingId.value = replyingId.value === comment.id ? null : comment.id
}

const submitReply = async (comment) => {
  if (!replyContent.value.trim()) return
  
  try {
    const response = await commentApi.create({
      articleId,
      parentId: comment.id,
      content: replyContent.value
    })
    if (response.code === 200) {
      replyContent.value = ''
      replyingId.value = null
      loadComments()
    }
  } catch (error) {
    console.error('回复失败:', error)
  }
}

const handleCommentLike = async (comment) => {
  try {
    await actionApi.like({
      targetId: comment.id,
      targetType: 'comment'
    })
    comment.likeCount++
  } catch (error) {
    console.error('评论点赞失败:', error)
  }
}

onMounted(() => {
  loadArticle()
  loadComments()
})
</script>

<style scoped>
.article-detail-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 60px 20px 80px;
}

.article-content {
  max-width: 800px;
  margin: 0 auto 30px;
  background: white;
  border-radius: 8px;
  padding: 30px;
}

.article-header {
  margin-bottom: 24px;
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
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 16px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
}

.author-name {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.publish-time {
  font-size: 14px;
  color: #999;
}

.meta-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.article-body {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  margin-bottom: 24px;
}

.article-body img {
  max-width: 100%;
  border-radius: 8px;
}

.article-body h1, h2, h3, h4, h5, h6 {
  margin: 20px 0 12px;
  font-weight: 600;
}

.article-body p {
  margin-bottom: 12px;
}

.article-body code {
  background: #f4f4f4;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
}

.article-body pre {
  background: #2d2d2d;
  color: #ccc;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
}

.article-stats {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  
  span {
    font-size: 14px;
    color: #999;
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

.article-actions {
  display: flex;
  gap: 12px;
}

.comment-section {
  max-width: 800px;
  margin: 0 auto;
  background: white;
  border-radius: 8px;
  padding: 24px;
}

.comment-section h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
}

.comment-form, .guest-comment {
  margin-bottom: 24px;
}

.submit-btn {
  margin-top: 12px;
  float: right;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
}

.comment-author {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 10px;
}

.comment-actions {
  display: flex;
  gap: 16px;
}

.action-btn {
  font-size: 13px;
  color: #667eea;
  cursor: pointer;
  
  &:hover {
    text-decoration: underline;
  }
}

.replies {
  margin-top: 12px;
  padding-left: 46px;
  border-left: 2px solid #eee;
}

.reply-item {
  padding: 8px 0;
  font-size: 13px;
  
  .reply-author {
    color: #667eea;
    font-weight: 500;
    margin-right: 8px;
  }
  
  .reply-content {
    color: #666;
  }
}

.reply-form {
  margin-top: 12px;
  padding-left: 46px;
  display: flex;
  gap: 10px;
}
</style>
