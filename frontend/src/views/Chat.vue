<template>
  <div class="chat-container">
    <div class="chat-sidebar">
      <div class="sidebar-header">
        <h3>好友聊天</h3>
        <el-button size="small" @click="showAddFriend = true">添加好友</el-button>
      </div>
      
      <!-- 标签切换：好友列表 / 好友请求 -->
      <div class="sidebar-tabs">
        <button 
          :class="{ active: activeTab === 'friends' }" 
          @click="activeTab = 'friends'"
        >
          好友列表
        </button>
        <button 
          :class="{ active: activeTab === 'requests' }" 
          @click="activeTab = 'requests'; loadFriendRequests()"
        >
          好友请求
          <span v-if="friendRequests.length > 0" class="badge">{{ friendRequests.length }}</span>
        </button>
      </div>
      
      <div v-if="activeTab === 'friends'" class="search-box">
        <el-input v-model="searchKeyword" placeholder="搜索好友..." size="small" />
      </div>
      
      <!-- 好友列表 -->
      <div v-if="activeTab === 'friends'" class="friend-list">
        <div 
          v-for="friend in filteredFriends" 
          :key="friend.id"
          @click="selectFriend(friend)"
          :class="{ active: selectedFriend?.id === friend.id }"
          class="friend-item"
        >
          <div class="friend-avatar">
            <img :src="friend.userAvatar || '/api/v1/upload/avatar/default'" />
            <span :class="{ online: friend.online }" class="status-dot"></span>
          </div>
          <div class="friend-info">
            <div class="friend-name">{{ friend.userName }}</div>
            <div class="last-message">{{ friend.lastMessage || '暂无消息' }}</div>
          </div>
          <div class="friend-meta">
            <span class="message-time">{{ formatTime(friend.createTime) }}</span>
          </div>
        </div>
      </div>
      
      <!-- 好友请求列表 -->
      <div v-if="activeTab === 'requests'" class="request-list">
        <div v-if="friendRequests.length === 0" class="no-requests">
          <p>暂无好友请求</p>
        </div>
        <div 
          v-for="request in friendRequests" 
          :key="request.id"
          class="request-item"
        >
          <div class="request-avatar">
            <img :src="request.userAvatar || '/api/v1/upload/avatar/default'" />
          </div>
          <div class="request-info">
            <div class="request-name">{{ request.userName }}</div>
            <div class="request-time">{{ formatTime(request.createTime) }} 发送请求</div>
          </div>
          <div class="request-actions">
            <el-button size="small" type="primary" @click="acceptRequest(request.id)">同意</el-button>
            <el-button size="small" @click="rejectRequest(request.id)">拒绝</el-button>
          </div>
        </div>
      </div>
    </div>
    
    <div class="chat-main" v-if="selectedFriend">
      <div class="chat-header">
        <div class="friend-info">
          <img :src="selectedFriend.userAvatar || '/api/avatar/default'" class="avatar" />
          <div>
            <span class="friend-name">{{ selectedFriend.userName }}</span>
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
          :class="{ 'self': message.fromUserId === currentUserId, 'other': message.fromUserId !== currentUserId }"
          class="message-item"
        >
          <img :src="message.userAvatar || '/api/avatar/default'" class="message-avatar" />
          <div class="message-content">
            <div class="message-text">{{ message.content }}</div>
            <span class="message-time">{{ formatTime(message.createTime) }}</span>
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
        <el-form-item label="用户名或用户ID">
          <el-input v-model="addFriendForm.targetUserId" placeholder="输入用户名或用户ID" />
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
import { messageApi } from '@/api/message'
import { userApi } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const currentUserId = userStore.user?.id

const searchKeyword = ref('')
const selectedFriend = ref(null)
const messages = ref([])
const inputMessage = ref('')
const showAddFriend = ref(false)
const friends = ref([])
const friendRequests = ref([])
const activeTab = ref('friends')

const addFriendForm = reactive({
  targetUserId: '',
  message: ''
})

const filteredFriends = computed(() => {
  if (!searchKeyword.value) return friends.value
  return friends.value.filter(f => f.userName.includes(searchKeyword.value))
})

const selectFriend = (friend) => {
  selectedFriend.value = friend
  loadMessages(friend.friendId)
}

const loadMessages = async (friendId) => {
  try {
    const response = await messageApi.getHistory(friendId)
    if (response.code === 200) {
      messages.value = response.data.reverse()
    }
  } catch (error) {
    console.error('加载消息失败:', error)
    messages.value = []
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || !selectedFriend.value) return
  
  try {
    const response = await messageApi.send({
      toUserId: selectedFriend.value.friendId,
      content: inputMessage.value,
      type: 'TEXT'
    })
    
    if (response.code === 200) {
      const newMessage = {
        id: Date.now(),
        content: inputMessage.value,
        fromUserId: currentUserId,
        toUserId: selectedFriend.value.friendId,
        userAvatar: userStore.user?.avatar,
        createTime: new Date()
      }
      
      messages.value.push(newMessage)
      inputMessage.value = ''
    }
  } catch (error) {
    console.error('发送消息失败:', error)
  }
}

const sendFriendRequest = async () => {
  if (!addFriendForm.targetUserId.trim()) {
    alert('请输入用户名或用户ID')
    return
  }

  try {
    // 获取输入值并去除前后空格
    let targetUserId = addFriendForm.targetUserId.trim()

    // 判断输入是否为纯数字（用户ID）
    const isNumeric = /^\d+$/.test(targetUserId)

    if (!isNumeric) {
      // 如果是用户名，先调用搜索接口查询用户ID
      const searchResponse = await userApi.search(targetUserId)
      if (searchResponse.code === 200 && searchResponse.data && searchResponse.data.users && searchResponse.data.users.length > 0) {
        // 后端已返回字符串类型的ID，避免精度丢失
        targetUserId = String(searchResponse.data.users[0].id)
      } else {
        alert('未找到该用户')
        return
      }
    }

    // 发送好友请求，targetUserId必须为字符串类型，防止大数字精度丢失
    const response = await friendApi.sendRequest({
      targetUserId: targetUserId.toString()
    })

    if (response.code === 200) {
      showAddFriend.value = false
      addFriendForm.targetUserId = ''
      alert('好友请求已发送，等待对方确认')
    } else {
      // 处理业务错误
      alert(response.msg || '发送失败')
    }
  } catch (error) {
    console.error('发送请求失败:', error)
    alert('发送失败')
  }
}

const loadFriends = async () => {
  try {
    const response = await friendApi.getFriends()
    if (response.code === 200) {
      // 默认显示所有好友为在线状态（暂无WebSocket实时状态功能）
      friends.value = response.data.map(f => ({ ...f, online: true }))
    }
  } catch (error) {
    console.error('加载好友列表失败:', error)
  }
}

const loadFriendRequests = async () => {
  try {
    const response = await friendApi.getRequests()
    if (response.code === 200) {
      friendRequests.value = response.data
    }
  } catch (error) {
    console.error('加载好友请求失败:', error)
    friendRequests.value = []
  }
}

const acceptRequest = async (requestId) => {
  try {
    const response = await friendApi.acceptRequest(requestId)
    if (response.code === 200) {
      alert('已同意好友请求')
      // 移除已处理的请求
      friendRequests.value = friendRequests.value.filter(r => r.id !== requestId)
      // 刷新好友列表
      loadFriends()
    }
  } catch (error) {
    console.error('同意好友请求失败:', error)
    alert('操作失败')
  }
}

const rejectRequest = async (requestId) => {
  try {
    const response = await friendApi.rejectRequest(requestId)
    if (response.code === 200) {
      alert('已拒绝好友请求')
      // 移除已处理的请求
      friendRequests.value = friendRequests.value.filter(r => r.id !== requestId)
    }
  } catch (error) {
    console.error('拒绝好友请求失败:', error)
    alert('操作失败')
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return date.toLocaleDateString('zh-CN')
}

onMounted(() => {
  loadFriends()
})
</script>

<style scoped>
.chat-container {
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
}

.sidebar-tabs {
  display: flex;
  border-bottom: 1px solid var(--border-color, #eee);
}

.sidebar-tabs button {
  flex: 1;
  padding: 12px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 13px;
  color: var(--text-secondary, #666);
  position: relative;
  transition: all 0.2s;
  
  &:hover {
    background: var(--bg-primary, #f8f9fa);
  }
  
  &.active {
    color: #667eea;
    font-weight: 600;
    
    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 40px;
      height: 2px;
      background: #667eea;
      border-radius: 2px;
    }
  }
  
  .badge {
    display: inline-block;
    background: #ff4d4f;
    color: white;
    font-size: 11px;
    padding: 1px 5px;
    border-radius: 10px;
    margin-left: 4px;
    min-width: 16px;
    text-align: center;
  }
}

.search-box {
  padding: 12px;
}

.friend-list {
  flex: 1;
  overflow-y: auto;
}

.request-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.no-requests {
  padding: 40px 20px;
  text-align: center;
  color: var(--text-tertiary, #999);
}

.request-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: var(--bg-primary, #fafafa);
  border-radius: 8px;
  margin-bottom: 8px;
}

.request-avatar {
  margin-right: 12px;
  
  img {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    object-fit: cover;
  }
}

.request-info {
  flex: 1;
  min-width: 0;
}

.request-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary, #333);
  margin-bottom: 4px;
}

.request-time {
  font-size: 12px;
  color: var(--text-tertiary, #999);
}

.request-actions {
  display: flex;
  gap: 6px;
}

.request-actions .el-button {
  padding: 4px 10px;
  font-size: 12px;
}

.friend-item {
  display: flex;
  align-items: center;
  padding: 12px;
  cursor: pointer;
  transition: background-color 0.2s;
  
  &:hover {
    background: var(--bg-primary, #f8f9fa);
  }
  
  &.active {
    background: #eef2ff;
  }
}

.friend-avatar {
  position: relative;
  margin-right: 12px;
  
  img {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    object-fit: cover;
  }
  
  .status-dot {
    position: absolute;
    bottom: 0;
    right: 0;
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: #999;
    border: 2px solid var(--bg-secondary, white);
    
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
  color: var(--text-primary, #333);
}

.last-message {
  font-size: 12px;
  color: var(--text-tertiary, #999);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.friend-meta {
  text-align: right;
}

.message-time {
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
  padding: 16px;
  border-bottom: 1px solid var(--border-color, #eee);
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
  object-fit: cover;
}

.chat-header .friend-name {
  font-size: 16px;
}

.chat-header .status {
  font-size: 12px;
  color: var(--text-tertiary, #999);
  
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
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border-radius: 12px 12px 0 12px;
    }
  }
  
  &.other {
    .message-content {
      background: var(--bg-primary, #f0f0f0);
      border-radius: 12px 12px 12px 0;
    }
  }
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
  object-fit: cover;
}

.message-content {
  max-width: 60%;
  padding: 10px 14px;
}

.message-text {
  font-size: 14px;
  line-height: 1.5;
  color: inherit;
}

.message-time {
  font-size: 11px;
  color: var(--text-tertiary, #999);
  margin-top: 4px;
  display: block;
}

.message-input {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  border-top: 1px solid var(--border-color, #eee);
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
  background: var(--bg-primary, #fafafa);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.chat-empty p {
  color: var(--text-tertiary, #999);
}
</style>
