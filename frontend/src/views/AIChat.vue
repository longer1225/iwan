<template>
  <div class="ai-chat-container">
    <div class="chat-sidebar">
      <div class="sidebar-header">
        <h3>🤖 Iwan AI</h3>
        <el-button size="small" @click="newSession">+ 新会话</el-button>
      </div>
      
      <div class="session-list">
        <div 
          v-for="session in sessions" 
          :key="session.id"
          @click="selectSession(session)"
          :class="{ active: selectedSession?.id === session.id }"
          class="session-item"
        >
          <div class="session-icon">💬</div>
          <div class="session-info">
            <div class="session-name">{{ session.name }}</div>
            <div class="session-preview">{{ session.lastMessage }}</div>
          </div>
          <el-button size="mini" @click.stop="deleteSession(session.id)">删除</el-button>
        </div>
      </div>
      
      <div class="toolbox">
        <h4>AI工具</h4>
        <div class="tool-list">
          <button @click="useTool('write')" class="tool-btn">
            <span>✍️</span>
            <span>文章创作</span>
          </button>
          <button @click="useTool('rewrite')" class="tool-btn">
            <span>✨</span>
            <span>文章润色</span>
          </button>
          <button @click="useTool('summarize')" class="tool-btn">
            <span>📝</span>
            <span>文章摘要</span>
          </button>
          <button @click="useTool('rag')" class="tool-btn">
            <span>📚</span>
            <span>知识库问答</span>
          </button>
        </div>
      </div>
      
      <!-- 推荐文章 -->
      <div class="recommendations" v-if="recommendations.length > 0">
        <h4>📚 推荐文章</h4>
        <div class="recommend-list">
          <div 
            v-for="article in recommendations" 
            :key="article.id"
            class="recommend-item"
            @click="goToArticle(article.id)"
          >
            <img :src="article.cover" class="recommend-cover" />
            <div class="recommend-info">
              <div class="recommend-title">{{ article.title }}</div>
              <div class="recommend-author">{{ article.authorName }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="chat-main" v-if="selectedSession">
      <div class="chat-header">
        <div class="header-left">
          <span class="ai-avatar">🤖</span>
          <input 
            v-model="selectedSession.name" 
            class="session-name-input"
            @blur="renameSession"
          />
        </div>
        <div class="chat-actions">
          <el-switch v-model="useRAG" active-text="RAG" inactive-text="普通" />
          <el-select v-model="selectedModel" placeholder="选择模型" size="small">
            <el-option label="Iwan AI" value="default" />
            <el-option label="GPT-4 Turbo" value="gpt4" />
            <el-option label="Claude 3" value="claude" />
          </el-select>
        </div>
      </div>
      
      <div class="message-list" ref="messageList">
        <!-- RAG检索提示 -->
        <div v-if="useRAG && messages.length === 0" class="rag-tip">
          <span>🔍</span>
          <span>已启用RAG模式，AI将基于你的博客内容进行回答</span>
        </div>
        
        <div 
          v-for="message in messages" 
          :key="message.id"
          :class="{ 'self': message.role === 'user', 'ai': message.role === 'assistant' }"
          class="message-item"
        >
          <div class="message-avatar">
            {{ message.role === 'user' ? '👤' : '🤖' }}
          </div>
          <div class="message-content">
            <div v-if="message.role === 'assistant'" class="message-header">
              <span class="ai-label">Iwan AI</span>
              <span v-if="message.isRAG" class="rag-badge">RAG</span>
              <span class="model-tag">{{ getModelName(message.model) }}</span>
            </div>
            <div class="message-text" v-html="message.content"></div>
            <div v-if="message.role === 'assistant'" class="message-meta">
              <span>⏱️ {{ message.time }}s</span>
              <span v-if="message.tokens">📊 {{ message.tokens }} tokens</span>
              <span v-if="message.sources?.length" @click="showSources(message.sources)" class="sources-link">📚 {{ message.sources.length }} 来源</span>
            </div>
          </div>
        </div>
        
        <div v-if="isTyping" class="typing-indicator">
          <span class="typing-dot"></span>
          <span class="typing-dot"></span>
          <span class="typing-dot"></span>
          <span>{{ typingText }}</span>
        </div>
      </div>
      
      <div class="message-input">
        <el-input 
          v-model="inputMessage" 
          type="textarea"
          :rows="2"
          placeholder="输入你的问题...（Ctrl+Enter发送）"
          @keyup.enter.ctrl="sendMessage"
        />
        <div class="input-actions">
          <el-button size="small" icon="el-icon-paperclip" @click="uploadFile">上传文件</el-button>
          <el-button type="primary" @click="sendMessage">发送</el-button>
        </div>
      </div>
    </div>
    
    <div class="chat-empty" v-else>
      <div class="empty-icon">🤖</div>
      <h3>欢迎使用 Iwan AI</h3>
      <p>我可以帮你写文章、润色内容、回答问题</p>
      <div class="quick-actions">
        <button @click="quickStart('write')" class="quick-btn">✍️ 写文章</button>
        <button @click="quickStart('analyze')" class="quick-btn">📊 分析文章</button>
        <button @click="quickStart('chat')" class="quick-btn">💬 随便聊聊</button>
      </div>
      <div class="feature-list">
        <div class="feature-item">
          <span>🎯</span>
          <span>智能推荐文章</span>
        </div>
        <div class="feature-item">
          <span>📚</span>
          <span>RAG知识库问答</span>
        </div>
        <div class="feature-item">
          <span>✨</span>
          <span>文章创作与润色</span>
        </div>
      </div>
    </div>
    
    <!-- 来源弹窗 -->
    <el-dialog title="参考来源" :visible.sync="showSourcesModal" width="500px">
      <div class="sources-list">
        <div v-for="(source, index) in currentSources" :key="index" class="source-item">
          <div class="source-header">
            <span class="source-num">{{ index + 1 }}</span>
            <span class="source-title">{{ source.title }}</span>
          </div>
          <p class="source-excerpt">{{ source.excerpt }}</p>
          <a :href="`/article/${source.id}`" target="_blank" class="source-link">查看原文</a>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { aiApi } from '@/api/ai'

const router = useRouter()

const sessions = ref([
  { id: 1, name: '默认会话', lastMessage: '你好！我是Iwan AI，有什么可以帮你的？' },
  { id: 2, name: '文章创作', lastMessage: '帮我写一篇关于Vue3的博客...' }
])

const selectedSession = ref(null)
const messages = ref([])
const inputMessage = ref('')
const isTyping = ref(false)
const useRAG = ref(false)
const selectedModel = ref('default')
const typingText = ref('AI正在思考...')
const recommendations = ref([])
const showSourcesModal = ref(false)
const currentSources = ref([])

// 模拟数据（备用）
const mockRecommendations = [
  { id: 1, title: 'Vue3 Composition API 完全指南', cover: 'https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=120', authorName: '张三' },
  { id: 2, title: 'Spring Boot 3.x 新特性详解', cover: 'https://images.unsplash.com/photo-1526379095098-d400fd0bf935?w=120', authorName: '李四' },
  { id: 3, title: 'PostgreSQL JSONB 实战教程', cover: 'https://images.unsplash.com/photo-1633356122102-3fe601e05bd2?w=120', authorName: '王五' }
]

const getModelName = (model) => {
  const names = {
    default: 'Iwan AI',
    gpt4: 'GPT-4 Turbo',
    claude: 'Claude 3'
  }
  return names[model] || model
}

const selectSession = (session) => {
  selectedSession.value = session
  loadMessages(session.id)
}

const loadMessages = (sessionId) => {
  messages.value = [
    { 
      id: 1, 
      role: 'assistant', 
      content: '你好！我是Iwan AI，很高兴为你服务。我可以帮你写文章、润色内容、回答技术问题，还能基于你的博客内容进行问答。有什么可以帮你的吗？',
      model: 'default',
      time: 1.2,
      tokens: 23
    }
  ]
}

const newSession = () => {
  const newId = Date.now()
  const session = {
    id: newId,
    name: `会话 ${sessions.value.length + 1}`,
    lastMessage: ''
  }
  sessions.value.push(session)
  selectSession(session)
}

const renameSession = () => {
  // 模拟保存
}

const deleteSession = (id) => {
  const index = sessions.value.findIndex(s => s.id === id)
  if (index > -1) {
    sessions.value.splice(index, 1)
    if (selectedSession.value?.id === id) {
      selectedSession.value = sessions.value[0] || null
      if (selectedSession.value) {
        loadMessages(selectedSession.value.id)
      }
    }
  }
}

const getAIResponse = (message) => {
  for (const item of aiResponses) {
    if (item.pattern.test(message)) {
      return item.response
    }
  }
  
  // 默认响应
  return `<p>感谢你的提问！关于"${message}"，这是我的回答：</p>
  <p>这是一个很好的话题。根据我的知识库，我可以为你提供相关信息和建议。</p>
  <p>如果你有更具体的问题，欢迎继续提问！</p>`
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || !selectedSession.value) return
  
  const userMessage = {
    id: Date.now(),
    role: 'user',
    content: inputMessage.value
  }
  
  messages.value.push(userMessage)
  selectedSession.value.lastMessage = inputMessage.value.slice(0, 30) + '...'
  inputMessage.value = ''
  isTyping.value = true
  
  // 模拟思考过程
  const thinkingTexts = ['AI正在思考...', '正在查阅知识库...', '整理回答中...', '马上就好...']
  let index = 0
  const thinkingInterval = setInterval(() => {
    typingText.value = thinkingTexts[index % thinkingTexts.length]
    index++
  }, 800)
  
  try {
    // 调用真实API
    const response = await aiApi.chat(userMessage.content, useRAG.value)
    
    if (response.code === 200) {
      const aiResponse = {
        id: Date.now() + 1,
        role: 'assistant',
        content: response.data.content,
        model: response.data.model,
        time: response.data.time,
        tokens: response.data.tokens,
        isRAG: response.data.isRAG,
        sources: response.data.sources
      }
      
      messages.value.push(aiResponse)
    } else {
      // API失败时使用模拟响应
      const aiResponse = {
        id: Date.now() + 1,
        role: 'assistant',
        content: `<p>感谢你的提问！关于"${userMessage.content}"，这是我的回答：</p><p>这是一个很好的话题。根据我的知识库，我可以为你提供相关信息和建议。</p>`,
        model: selectedModel.value,
        time: (Math.random() * 2 + 1).toFixed(1),
        tokens: Math.floor(Math.random() * 100 + 50),
        isRAG: useRAG.value
      }
      messages.value.push(aiResponse)
    }
    
    // 滚动到底部
    nextTick(() => {
      const messageList = document.querySelector('.message-list')
      if (messageList) {
        messageList.scrollTop = messageList.scrollHeight
      }
    })
  } catch (error) {
    console.error('发送消息失败:', error)
    // 网络失败时使用模拟响应
    const aiResponse = {
      id: Date.now() + 1,
      role: 'assistant',
      content: `<p>感谢你的提问！关于"${userMessage.content}"，这是我的回答：</p><p>这是一个很好的话题。根据我的知识库，我可以为你提供相关信息和建议。</p>`,
      model: selectedModel.value,
      time: (Math.random() * 2 + 1).toFixed(1),
      tokens: Math.floor(Math.random() * 100 + 50),
      isRAG: useRAG.value
    }
    messages.value.push(aiResponse)
  } finally {
    isTyping.value = false
    typingText.value = 'AI正在思考...'
    clearInterval(thinkingInterval)
  }
}

const useTool = (toolType) => {
  switch (toolType) {
    case 'write':
      inputMessage.value = '帮我写一篇关于前端开发的博客文章，要求结构清晰，包含代码示例和实践经验分享。'
      break
    case 'rewrite':
      inputMessage.value = '请帮我润色以下内容，使其更加流畅自然，提升专业度：[请粘贴需要润色的文本]'
      break
    case 'summarize':
      inputMessage.value = '请帮我总结以下文章的核心内容，生成一篇简明扼要的摘要：[请粘贴文章内容]'
      break
    case 'rag':
      useRAG.value = true
      inputMessage.value = '根据我的博客内容，回答以下问题：'
      break
  }
}

const quickStart = (type) => {
  newSession()
  switch (type) {
    case 'write':
      inputMessage.value = '帮我写一篇技术博客文章'
      break
    case 'analyze':
      inputMessage.value = '分析一下当前的前端技术趋势'
      break
    case 'chat':
      inputMessage.value = '你好！介绍一下你自己'
      break
  }
}

const showSources = (sources) => {
  currentSources.value = sources
  showSourcesModal.value = true
}

const goToArticle = (id) => {
  router.push(`/article/${id}`)
}

const uploadFile = () => {
  alert('文件上传功能开发中...')
}

const loadRecommendations = async () => {
  try {
    const response = await aiApi.getRecommendations(3)
    if (response.code === 200) {
      recommendations.value = response.data
    } else {
      recommendations.value = mockRecommendations
    }
  } catch (error) {
    console.error('加载推荐文章失败:', error)
    recommendations.value = mockRecommendations
  }
}

onMounted(() => {
  loadRecommendations()
  if (sessions.value.length > 0) {
    selectSession(sessions.value[0])
  }
})
</script>

<style scoped>
.ai-chat-container {
  display: flex;
  height: calc(100vh - 60px);
  padding-top: 60px;
  background-color: var(--bg-primary, #f5f5f5);
}

.chat-sidebar {
  width: 320px;
  background: var(--bg-secondary, white);
  border-right: 1px solid var(--border-color, #eee);
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--border-color, #eee);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  h3 {
    color: white;
    margin: 0;
    font-size: 16px;
  }
  
  .el-button {
    background: rgba(255,255,255,0.2);
    border: none;
    color: white;
    
    &:hover {
      background: rgba(255,255,255,0.3);
    }
  }
}

.session-list {
  flex: 1;
  overflow-y: auto;
}

.session-item {
  display: flex;
  align-items: center;
  padding: 12px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background: var(--hover-bg, #f8f9fa);
  }
  
  &.active {
    background: #eef2ff;
    border-left: 3px solid #667eea;
  }
}

.session-icon {
  font-size: 24px;
  margin-right: 10px;
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-name {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
  color: var(--text-primary, #333);
}

.session-preview {
  font-size: 12px;
  color: var(--text-tertiary, #999);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.toolbox {
  padding: 16px;
  border-top: 1px solid var(--border-color, #eee);
}

.toolbox h4 {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary, #666);
  margin-bottom: 12px;
}

.tool-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tool-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: var(--hover-bg, #f8f9fa);
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  color: var(--text-secondary, #666);
  transition: all 0.2s;
  
  &:hover {
    background: #eef2ff;
    color: #667eea;
    transform: translateX(4px);
  }
}

.recommendations {
  padding: 16px;
  border-top: 1px solid var(--border-color, #eee);
  background: #fafafa;
}

.recommendations h4 {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary, #666);
  margin-bottom: 12px;
}

.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recommend-item {
  display: flex;
  gap: 10px;
  padding: 8px;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    transform: translateX(4px);
    box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  }
}

.recommend-cover {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  object-fit: cover;
}

.recommend-info {
  flex: 1;
  min-width: 0;
}

.recommend-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary, #333);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommend-author {
  font-size: 11px;
  color: var(--text-tertiary, #999);
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--bg-secondary, white);
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color, #eee);
  background: rgba(102, 126, 234, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ai-avatar {
  font-size: 28px;
}

.session-name-input {
  font-size: 16px;
  font-weight: 600;
  border: none;
  background: none;
  outline: none;
  width: 200px;
  color: var(--text-primary, #333);
}

.chat-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.rag-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 8px;
  font-size: 13px;
  color: #667eea;
}

.message-item {
  display: flex;
  gap: 12px;
  
  &.self {
    flex-direction: row-reverse;
    
    .message-content {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border-radius: 16px 16px 0 16px;
    }
    
    .message-text {
      color: white;
    }
  }
  
  &.ai {
    .message-content {
      background: var(--hover-bg, #f8f9fa);
      border-radius: 16px 16px 16px 0;
      max-width: 75%;
    }
  }
}

.message-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--hover-bg, #f8f9fa);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.message-content {
  padding: 14px 18px;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.ai-label {
  font-size: 14px;
  font-weight: 600;
  color: #667eea;
}

.rag-badge {
  font-size: 11px;
  color: #fff;
  background: #48dbfb;
  padding: 2px 6px;
  border-radius: 4px;
}

.model-tag {
  font-size: 11px;
  color: var(--text-tertiary, #999);
  background: var(--hover-bg, #eee);
  padding: 2px 6px;
  border-radius: 4px;
}

.message-text {
  font-size: 15px;
  line-height: 1.7;
  color: var(--text-primary, #333);
}

.message-meta {
  display: flex;
  gap: 12px;
  margin-top: 10px;
  font-size: 12px;
  color: var(--text-tertiary, #999);
}

.sources-link {
  color: #667eea;
  cursor: pointer;
  
  &:hover {
    text-decoration: underline;
  }
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 18px;
  background: var(--hover-bg, #f8f9fa);
  border-radius: 16px;
  width: fit-content;
}

.typing-dot {
  width: 8px;
  height: 8px;
  background: #667eea;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
  
  &:nth-child(2) {
    animation-delay: 0.2s;
  }
  
  &:nth-child(3) {
    animation-delay: 0.4s;
  }
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.message-input {
  padding: 16px 20px;
  border-top: 1px solid var(--border-color, #eee);
  background: var(--bg-secondary, white);
}

.message-input textarea {
  margin-bottom: 12px;
  border-radius: 12px;
  resize: none;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
}

.chat-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea10 0%, #764ba210 100%);
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
}

.chat-empty h3 {
  font-size: 24px;
  color: var(--text-primary, #333);
  margin: 0 0 10px 0;
}

.chat-empty p {
  color: var(--text-secondary, #666);
  margin: 0 0 30px 0;
}

.quick-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 40px;
}

.quick-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: white;
  border: 1px solid var(--border-color, #eee);
  border-radius: 25px;
  cursor: pointer;
  font-size: 14px;
  color: var(--text-secondary, #666);
  transition: all 0.3s;
  
  &:hover {
    background: #f0f4ff;
    border-color: #667eea;
    color: #667eea;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
  }
}

.feature-list {
  display: flex;
  gap: 30px;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  
  span:first-child {
    font-size: 24px;
  }
  
  span:last-child {
    font-size: 13px;
    color: var(--text-secondary, #666);
  }
}

.sources-list {
  max-height: 400px;
  overflow-y: auto;
}

.source-item {
  padding: 16px;
  border-bottom: 1px solid #eee;
  
  &:last-child {
    border-bottom: none;
  }
}

.source-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.source-num {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #667eea;
  color: white;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.source-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.source-excerpt {
  font-size: 13px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 10px;
}

.source-link {
  font-size: 12px;
  color: #667eea;
}
</style>
