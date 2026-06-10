<template>
  <div class="chat-container">
    <div class="chat-sidebar">
      <div class="sidebar-header">
        <h3>好友聊天</h3>
        <el-button size="small" @click="showAddFriend = true">添加好友</el-button>
      </div>
      
      <div class="search-box">
        <el-input v-model="searchKeyword" placeholder="搜索好友..." size="small" />
      </div>
      
      <div class="friend-list">
        <div 
          v-for="friend in filteredFriends" 
          :key="friend.id"
          @click="selectFriend(friend)"
          :class="{ active: selectedFriend?.id === friend.id }"
          class="friend-item"
        >
          <div class="friend-avatar">
            <img :src="friend.avatar" />
            <span :class="{ online: friend.online }" class="status-dot"></span>
          </div>
          <div class="friend-info">
            <div class="friend-name">{{ friend.nickname }}</div>
            <div class="last-message">{{ friend.lastMessage || '暂无消息' }}</div>
          </div>
          <div class="friend-meta">
            <span class="message-time">{{ friend.lastTime }}</span>
            <span v-if="friend.unreadCount > 0" class="unread-badge">{{ friend.unreadCount }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="chat-main" v-if="selectedFriend">
      <div class="chat-header">
        <div class="friend-info">
          <img :src="selectedFriend.avatar" class="avatar" />
          <div>
            <span class="friend-name">{{ selectedFriend.nickname }}</span>
            <span :class="{ online: selectedFriend.online }" class="status">{{ selectedFriend.online ? '在线' : '离线' }}</span>
          </div>
        </div>
        <div class="chat-actions">
          <el-button size="small">资料</el-button>
          <el-button size="small">设置</el-button>
        </div>
      </div>
      
      <div class="message-list" ref="messageList">
        <div 
          v-for="message in messages" 
          :key="message.id"
          :class="{ 'self': message.isSelf, 'other': !message.isSelf }"
          class="message-item"
        >
          <img :src="message.avatar" class="message-avatar" />
          <div class="message-content">
            <div class="message-text">{{ message.content }}</div>
            <span class="message-time">{{ message.time }}</span>
          </div>
        </div>
      </div>
      
      <div class="message-input">
        <el-input 
          v-model="inputMessage" 
          placeholder="输入消息..."
          @keyup.enter="sendMessage"
        />
        <el-button type="primary" @click="sendMessage">发送</el-button>
      </div>
    </div>
    
    <div class="chat-empty" v-else>
      <div class="empty-icon">💬</div>
      <p>选择一个好友开始聊天</p>
    </div>
    
    <el-dialog title="添加好友" v-model="showAddFriend">
      <el-form :model="addFriendForm">
        <el-form-item label="好友账号">
          <el-input v-model="addFriendForm.username" placeholder="输入好友账号" />
        </el-form-item>
        <el-form-item label="验证消息">
          <el-input v-model="addFriendForm.remark" placeholder="输入验证消息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddFriend = false">取消</el-button>
        <el-button type="primary" @click="sendFriendRequest">发送请求</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { friendApi } from '@/api/friend'

const searchKeyword = ref('')
const selectedFriend = ref(null)
const messages = ref([])
const inputMessage = ref('')
const showAddFriend = ref(false)

const addFriendForm = reactive({
  username: '',
  remark: ''
})

const friends = ref([
  { id: 1, nickname: '张三', avatar: 'https://neeko-copilot.bytedance.net/api/text_to_image?prompt=young%20man%20avatar%20friendly&image_size=square', online: true, lastMessage: '最近忙什么呢？', lastTime: '刚刚', unreadCount: 2 },
  { id: 2, nickname: '李四', avatar: 'https://neeko-copilot.bytedance.net/api/text_to_image?prompt=professional%20woman%20avatar&image_size=square', online: false, lastMessage: '好的，明天见', lastTime: '2小时前', unreadCount: 0 },
  { id: 3, nickname: '王五', avatar: 'https://neeko-copilot.bytedance.net/api/text_to_image?prompt=casual%20person%20avatar&image_size=square', online: true, lastMessage: '文章写得很棒！', lastTime: '昨天', unreadCount: 0 }
])

const filteredFriends = computed(() => {
  if (!searchKeyword.value) return friends.value
  return friends.value.filter(f => f.nickname.includes(searchKeyword.value))
})

const selectFriend = (friend) => {
  selectedFriend.value = friend
  friend.unreadCount = 0
  loadMessages(friend.id)
}

const loadMessages = (friendId) => {
  messages.value = [
    { id: 1, content: '最近在忙什么呢？', time: '10:00', avatar: friends.value[0].avatar, isSelf: false },
    { id: 2, content: '在做一个博客项目，用Vue3开发', time: '10:01', avatar: 'https://neeko-copilot.bytedance.net/api/text_to_image?prompt=user%20avatar%20default&image_size=square', isSelf: true },
    { id: 3, content: '听起来很不错！我也想学一下', time: '10:02', avatar: friends.value[0].avatar, isSelf: false },
    { id: 4, content: '可以啊，Vue3挺好用的，配合Vite很快', time: '10:03', avatar: 'https://neeko-copilot.bytedance.net/api/text_to_image?prompt=user%20avatar%20default&image_size=square', isSelf: true }
  ]
}

const sendMessage = () => {
  if (!inputMessage.value.trim() || !selectedFriend.value) return
  
  const newMessage = {
    id: Date.now(),
    content: inputMessage.value,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
    avatar: 'https://neeko-copilot.bytedance.net/api/text_to_image?prompt=user%20avatar%20default&image_size=square',
    isSelf: true
  }
  
  messages.value.push(newMessage)
  inputMessage.value = ''
  
  setTimeout(() => {
    const reply = {
      id: Date.now() + 1,
      content: '好的，收到！',
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
      avatar: selectedFriend.value.avatar,
      isSelf: false
    }
    messages.value.push(reply)
  }, 1000)
}

const sendFriendRequest = async () => {
  try {
    await friendApi.apply({
      targetUserId: addFriendForm.username,
      applyRemark: addFriendForm.remark
    })
    showAddFriend.value = false
    addFriendForm.username = ''
    addFriendForm.remark = ''
    alert('好友请求已发送')
  } catch (error) {
    console.error('发送请求失败:', error)
  }
}

onMounted(() => {
  if (friends.value.length > 0) {
    selectFriend(friends.value[0])
  }
})
</script>

<style scoped>
.chat-container {
  display: flex;
  height: calc(100vh - 60px);
  padding-top: 60px;
  background-color: #f5f5f5;
}

.chat-sidebar {
  width: 320px;
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

.search-box {
  padding: 12px;
}

.friend-list {
  flex: 1;
  overflow-y: auto;
}

.friend-item {
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

.friend-avatar {
  position: relative;
  margin-right: 12px;
  
  img {
    width: 48px;
    height: 48px;
    border-radius: 50%;
  }
  
  .status-dot {
    position: absolute;
    bottom: 0;
    right: 0;
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: #999;
    border: 2px solid white;
    
    &.online {
      background: #52c41a;
    }
  }
}

.friend-info {
  flex: 1;
  min-width: 0;
}

.friend-name {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
}

.last-message {
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.friend-meta {
  text-align: right;
}

.message-time {
  font-size: 11px;
  color: #999;
}

.unread-badge {
  display: inline-block;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  background: #f5222d;
  color: white;
  font-size: 12px;
  border-radius: 9px;
  margin-top: 4px;
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

.chat-header .friend-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-header .avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
}

.chat-header .friend-name {
  font-size: 16px;
}

.chat-header .status {
  font-size: 12px;
  color: #999;
  
  &.online {
    color: #52c41a;
  }
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
  }
  
  &.other {
    .message-content {
      background: #f0f0f0;
      border-radius: 12px 12px 12px 0;
    }
  }
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
}

.message-content {
  max-width: 60%;
  padding: 10px 14px;
}

.message-text {
  font-size: 14px;
  line-height: 1.5;
}

.message-time {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
  display: block;
}

.message-input {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  border-top: 1px solid #eee;
}

.message-input .el-input {
  flex: 1;
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
}
</style>
