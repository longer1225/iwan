<template>
  <div class="ai-chat-container">
    <div class="chat-sidebar">
      <div class="sidebar-header">
        <h3>AI对话</h3>
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
          <div class="session-icon">🤖</div>
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
          <button @click="useTool('rag')" class="tool-btn">
            <span>📚</span>
            <span>知识库问答</span>
          </button>
        </div>
      </div>
    </div>
    
    <div class="chat-main" v-if="selectedSession">
      <div class="chat-header">
        <input 
          v-model="selectedSession.name" 
          class="session-name-input"
          @blur="renameSession"
        />
        <div class="chat-actions">
          <el-switch v-model="useRAG" active-text="RAG开启" inactive-text="RAG关闭" />
        </div>
      </div>
      
      <div class="message-list" ref="messageList">
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
              <span class="model-tag">{{ message.model }}</span>
            </div>
            <div class="message-text" v-html="message.content"></div>
            <div v-if="message.role === 'assistant'" class="message-meta">
              <span>耗时 {{ message.time }}s</span>
              <span v-if="message.tokens">消耗 {{ message.tokens }} tokens</span>
            </div>
          </div>
        </div>
        
        <div v-if="isTyping" class="typing-indicator">
          <span class="typing-dot"></span>
          <span class="typing-dot"></span>
          <span class="typing-dot"></span>
          <span>AI正在思考...</span>
        </div>
      </div>
      
      <div class="message-input">
        <el-input 
          v-model="inputMessage" 
          type="textarea"
          :rows="2"
          placeholder="输入你的问题..."
          @keyup.enter.ctrl="sendMessage"
        />
        <div class="input-actions">
          <el-select v-model="selectedModel" placeholder="选择模型" size="small">
            <el-option label="默认模型" value="default" />
            <el-option label="GPT-4" value="gpt4" />
            <el-option label="Claude" value="claude" />
          </el-select>
          <el-button type="primary" @click="sendMessage">发送</el-button>
        </div>
      </div>
    </div>
    
    <div class="chat-empty" v-else>
      <div class="empty-icon">🤖</div>
      <p>选择一个会话开始AI对话</p>
      <el-button type="primary" @click="newSession">创建新会话</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { aiApiService } from '@/api/ai'

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

const selectSession = (session) => {
  selectedSession.value = session
  loadMessages(session.id)
}

const loadMessages = (sessionId) => {
  messages.value = [
    { 
      id: 1, 
      role: 'assistant', 
      content: '你好！我是Iwan AI，很高兴为你服务。有什么可以帮你的吗？',
      model: 'default',
      time: 1.2,
      tokens: 23
    },
    { 
      id: 2, 
      role: 'user', 
      content: '介绍一下Vue3的新特性' 
    },
    { 
      id: 3, 
      role: 'assistant', 
      content: '<p>Vue3相比Vue2有很多重要的改进：</p><ul><li><strong>Composition API</strong>：更灵活的代码组织方式</li><li><strong>响应式系统重构</strong>：使用Proxy替代Object.defineProperty</li><li><strong>更好的TypeScript支持</strong></li><li><strong>Fragment和Teleport等新特性</strong></li></ul>',
      model: 'default',
      time: 3.5,
      tokens: 156
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
  alert('会话名称已保存')
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
  
  try {
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    const response = {
      id: Date.now() + 1,
      role: 'assistant',
      content: '<p>这是AI的响应内容。根据你的问题，我来为你详细解答...</p>',
      model: selectedModel.value,
      time: (Math.random() * 2 + 1).toFixed(1),
      tokens: Math.floor(Math.random() * 100 + 50)
    }
    
    messages.value.push(response)
  } catch (error) {
    console.error('发送消息失败:', error)
  } finally {
    isTyping.value = false
  }
}

const useTool = (toolType) => {
  switch (toolType) {
    case 'write':
      inputMessage.value = '帮我写一篇关于前端开发的博客文章，要求结构清晰，包含代码示例。'
      break
    case 'rewrite':
      inputMessage.value = '润色以下内容，使其更加流畅自然：[请粘贴需要润色的文本]'
      break
    case 'rag':
      useRAG.value = true
      inputMessage.value = '根据我的博客内容，回答以下问题：'
      break
  }
}

onMounted(() => {
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
  background-color: #f5f5f5;
}

.chat-sidebar {
  width: 300px;
  background: white;
  border-right: 1px solid #eee;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
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
  transition: background-color 0.2s;
  
  &:hover {
    background: #f8f9fa;
  }
  
  &.active {
    background: #f0f4ff;
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
}

.session-preview {
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.toolbox {
  padding: 16px;
  border-top: 1px solid #eee;
}

.toolbox h4 {
  font-size: 13px;
  font-weight: 600;
  color: #666;
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
  background: #f8f9fa;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  color: #666;
  transition: all 0.2s;
  
  &:hover {
    background: #e9ecef;
    color: #667eea;
  }
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.session-name-input {
  font-size: 16px;
  font-weight: 600;
  border: none;
  background: none;
  outline: none;
  width: 200px;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  gap: 10px;
  
  &.self {
    flex-direction: row-reverse;
    
    .message-content {
      background: #667eea;
      color: white;
      border-radius: 12px 12px 0 12px;
    }
    
    .message-text {
      color: white;
    }
  }
  
  &.ai {
    .message-content {
      background: #f8f9fa;
      border-radius: 12px 12px 12px 0;
      max-width: 80%;
    }
  }
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #eee;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.message-content {
  padding: 12px 16px;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.ai-label {
  font-size: 13px;
  font-weight: 600;
  color: #667eea;
}

.model-tag {
  font-size: 11px;
  color: #999;
  background: #eee;
  padding: 2px 6px;
  border-radius: 4px;
}

.message-text {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
}

.message-meta {
  display: flex;
  gap: 12px;
  margin-top: 8px;
  font-size: 11px;
  color: #999;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 12px;
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
  padding: 16px;
  border-top: 1px solid #eee;
}

.message-input textarea {
  margin-bottom: 12px;
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
  background: #fafafa;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.chat-empty p {
  color: #999;
  margin-bottom: 16px;
}
</style>
