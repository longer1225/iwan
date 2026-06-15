<template>
  <div class="friends-page">
    <div class="friends-header">
      <h1>好友管理</h1>
      <div class="search-bar">
        <el-input 
          v-model="searchKeyword" 
          placeholder="搜索用户..." 
          class="search-input"
          @keyup.enter="searchUsers"
        />
        <el-button @click="searchUsers" class="search-btn">搜索</el-button>
      </div>
    </div>

    <div class="friends-tabs">
      <el-tabs v-model="activeTab" class="tabs">
        <el-tab-pane label="好友列表" name="friends">
          <div class="friends-content">
            <div class="friends-list">
              <div 
                v-for="friend in friendList" 
                :key="friend.id" 
                class="friend-item"
              >
                <div class="friend-avatar">
                  <img :src="friend.userAvatar || '/api/avatar/default'" :alt="friend.userName" />
                </div>
                <div class="friend-info">
                  <h3>{{ friend.userName }}</h3>
                  <p class="friend-id">用户ID: {{ friend.friendId }}</p>
                </div>
                <div class="friend-actions">
                  <el-button @click="viewProfile(friend.friendId)" class="action-btn">查看资料</el-button>
                  <el-button @click="startChat(friend.friendId)" class="action-btn primary">发消息</el-button>
                  <el-button @click="deleteFriend(friend.id)" class="action-btn danger">删除好友</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="添加好友" name="add">
          <div class="add-friend-content">
            <div class="search-section">
              <el-input 
                v-model="userSearchKeyword" 
                placeholder="输入用户名或ID搜索..." 
                size="large"
                @keyup.enter="searchUsers"
              >
                <template #append>
                  <el-button @click="searchUsers">搜索</el-button>
                </template>
              </el-input>
            </div>
            
            <div v-if="searchResults.length > 0" class="search-results">
              <h3>搜索结果</h3>
              <div 
                v-for="user in searchResults" 
                :key="user.id"
                class="user-result-item"
              >
                <div class="user-avatar">
                  <img :src="user.avatar || '/api/avatar/default'" :alt="user.nickname" />
                </div>
                <div class="user-info">
                  <h3>{{ user.nickname }}</h3>
                  <p v-if="user.bio" class="user-bio">{{ user.bio }}</p>
                  <p class="user-id">ID: {{ user.id }}</p>
                </div>
                <div class="user-actions">
                  <el-button 
                    v-if="user.isFriend" 
                    disabled
                    class="action-btn"
                  >
                    已是好友
                  </el-button>
                  <el-button 
                    v-else-if="user.requestSent" 
                    disabled
                    class="action-btn"
                  >
                    已发送申请
                  </el-button>
                  <el-button 
                    v-else
                    @click="sendFriendRequest(user.id)"
                    type="primary"
                    class="action-btn"
                  >
                    添加好友
                  </el-button>
                  <el-button @click="viewProfile(user.id)" class="action-btn">查看资料</el-button>
                </div>
              </div>
            </div>
            
            <div v-if="searchResults.length === 0 && hasSearched" class="empty-state">
              <p>未找到匹配的用户</p>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="好友申请" name="requests">
          <div class="requests-content">
            <div v-if="pendingRequests.length === 0" class="empty-state">
              <p>暂无待处理的好友申请</p>
            </div>
            <div class="requests-list">
              <div 
                v-for="request in pendingRequests" 
                :key="request.id" 
                class="request-item"
              >
                <div class="request-avatar">
                  <img :src="request.userAvatar || '/api/avatar/default'" :alt="request.userName" />
                </div>
                <div class="request-info">
                  <h3>{{ request.userName }}</h3>
                  <p v-if="request.message" class="request-message">{{ request.message }}</p>
                  <p class="request-time">{{ formatTime(request.createTime) }}</p>
                </div>
                <div class="request-actions">
                  <el-button @click="acceptRequest(request.id)" class="accept-btn">同意</el-button>
                  <el-button @click="rejectRequest(request.id)" class="reject-btn">拒绝</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="好友分组" name="groups">
          <div class="groups-content">
            <div class="groups-header">
              <el-button @click="showCreateGroupModal = true" class="create-group-btn">创建分组</el-button>
            </div>
            <div class="groups-list">
              <div 
                v-for="group in groupList" 
                :key="group.id" 
                class="group-item"
              >
                <div class="group-info">
                  <h3>{{ group.name }}</h3>
                  <p v-if="group.description" class="group-desc">{{ group.description }}</p>
                </div>
                <div class="group-actions">
                  <el-button @click="viewGroupMembers(group.id)" class="action-btn">查看成员</el-button>
                  <el-button @click="deleteGroup(group.id)" class="action-btn danger">删除分组</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 创建分组弹窗 -->
    <el-dialog title="创建分组" v-model="showCreateGroupModal">
      <el-form :model="groupForm" class="group-form">
        <el-form-item label="分组名称">
          <el-input v-model="groupForm.name" placeholder="请输入分组名称" />
        </el-form-item>
        <el-form-item label="分组描述">
          <el-input v-model="groupForm.description" placeholder="请输入分组描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateGroupModal = false">取消</el-button>
        <el-button @click="createGroup" type="primary">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { friendApi } from '@/api/friend'
import { userApi } from '@/api/user'

const router = useRouter()

const activeTab = ref('friends')
const searchKeyword = ref('')
const userSearchKeyword = ref('')
const friendList = ref([])
const pendingRequests = ref([])
const groupList = ref([])
const searchResults = ref([])
const hasSearched = ref(false)
const showCreateGroupModal = ref(false)

const groupForm = reactive({
  name: '',
  description: ''
})

const loadFriends = async () => {
  try {
    const response = await friendApi.getFriends()
    if (response.code === 200) {
      friendList.value = response.data
    }
  } catch (error) {
    console.error('加载好友列表失败:', error)
  }
}

const loadRequests = async () => {
  try {
    const response = await friendApi.getRequests()
    if (response.code === 200) {
      pendingRequests.value = response.data
    }
  } catch (error) {
    console.error('加载好友申请失败:', error)
  }
}

const loadGroups = async () => {
  try {
    const response = await friendApi.getGroups()
    if (response.code === 200) {
      groupList.value = response.data
    }
  } catch (error) {
    console.error('加载分组列表失败:', error)
  }
}

const searchUsers = async () => {
  if (!userSearchKeyword.value.trim()) {
    searchResults.value = []
    hasSearched.value = false
    return
  }
  
  hasSearched.value = true
  
  try {
    // 使用搜索API
    const response = await userApi.search(userSearchKeyword.value)
    if (response.code === 200 && response.data) {
      const users = response.data.users || []
      
      // 检查每个用户的好友状态
      const usersWithStatus = await Promise.all(
        users.map(async (user) => {
          try {
            const friendResponse = await friendApi.isFriend(user.id)
            return {
              ...user,
              isFriend: friendResponse.code === 200 && friendResponse.data === true,
              requestSent: false
            }
          } catch (error) {
            return {
              ...user,
              isFriend: false,
              requestSent: false
            }
          }
        })
      )
      
      searchResults.value = usersWithStatus
    } else {
      searchResults.value = []
    }
  } catch (error) {
    console.error('搜索用户失败:', error)
    searchResults.value = []
  }
}

const sendFriendRequest = async (targetUserId) => {
  try {
    const response = await friendApi.sendRequest({
      targetUserId: targetUserId
    })
    
    if (response.code === 200) {
      alert('好友申请已发送，等待对方确认')
      
      // 更新搜索结果中的状态
      const userIndex = searchResults.value.findIndex(u => u.id === targetUserId)
      if (userIndex !== -1) {
        searchResults.value[userIndex].requestSent = true
      }
    } else {
      alert(response.msg || '发送好友申请失败')
    }
  } catch (error) {
    console.error('发送好友申请失败:', error)
    alert('发送好友申请失败')
  }
}

const searchFriends = () => {
  if (!searchKeyword.value.trim()) {
    loadFriends()
    return
  }
  const filtered = friendList.value.filter(friend => 
    friend.userName.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
  friendList.value = filtered
}

const acceptRequest = async (id) => {
  try {
    const response = await friendApi.acceptRequest(id)
    if (response.code === 200) {
      await loadRequests()
      await loadFriends()
      alert('已同意好友申请')
    }
  } catch (error) {
    console.error('同意好友申请失败:', error)
    alert('操作失败')
  }
}

const rejectRequest = async (id) => {
  try {
    const response = await friendApi.rejectRequest(id)
    if (response.code === 200) {
      await loadRequests()
      alert('已拒绝好友申请')
    }
  } catch (error) {
    console.error('拒绝好友申请失败:', error)
    alert('操作失败')
  }
}

const deleteFriend = async (id) => {
  if (!confirm('确定要删除这位好友吗？')) return
  
  try {
    const response = await friendApi.deleteFriend(id)
    if (response.code === 200) {
      await loadFriends()
      alert('已删除好友')
    }
  } catch (error) {
    console.error('删除好友失败:', error)
    alert('操作失败')
  }
}

const createGroup = async () => {
  if (!groupForm.name.trim()) {
    alert('请输入分组名称')
    return
  }
  
  try {
    const response = await friendApi.createGroup({
      name: groupForm.name,
      description: groupForm.description
    })
    if (response.code === 200) {
      await loadGroups()
      showCreateGroupModal.value = false
      groupForm.name = ''
      groupForm.description = ''
      alert('分组创建成功')
    }
  } catch (error) {
    console.error('创建分组失败:', error)
    alert('操作失败')
  }
}

const deleteGroup = async (id) => {
  if (!confirm('确定要删除这个分组吗？')) return
  
  try {
    const response = await friendApi.deleteGroup(id)
    if (response.code === 200) {
      await loadGroups()
      alert('已删除分组')
    }
  } catch (error) {
    console.error('删除分组失败:', error)
    alert('操作失败')
  }
}

const viewProfile = (userId) => {
  router.push(`/profile/${userId}`)
}

const startChat = (userId) => {
  router.push(`/chat/${userId}`)
}

const viewGroupMembers = (groupId) => {
  alert(`查看分组 ${groupId} 的成员`)
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

onMounted(() => {
  loadFriends()
  loadRequests()
  loadGroups()
})
</script>

<style scoped>
.friends-page {
  min-height: 100vh;
  background-color: var(--bg-primary, #f5f5f5);
  padding: 20px;
}

.friends-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.friends-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary, #333);
}

.search-bar {
  display: flex;
  gap: 10px;
}

.search-input {
  width: 250px;
  border-radius: 8px;
}

.search-btn {
  border-radius: 8px;
}

/* 添加好友页面样式 */
.add-friend-content {
  padding: 20px 0;
}

.search-section {
  margin-bottom: 30px;
}

.search-results {
  margin-top: 20px;
}

.search-results h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 15px;
  color: var(--text-primary, #333);
}

.user-result-item {
  display: flex;
  align-items: center;
  padding: 15px;
  background: var(--bg-secondary, #fff);
  border-radius: 12px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.user-result-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.user-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 15px;
  flex-shrink: 0;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-info h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 5px 0;
  color: var(--text-primary, #333);
}

.user-bio {
  font-size: 13px;
  color: var(--text-secondary, #666);
  margin: 0 0 5px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-id {
  font-size: 12px;
  color: var(--text-tertiary, #999);
  margin: 0;
}

.user-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.user-actions .action-btn {
  border-radius: 8px;
  padding: 8px 16px;
  font-size: 14px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary, #666);
}

.empty-state p {
  font-size: 16px;
  margin: 0;
}

.search-btn {
  border-radius: 8px;
}

.friends-tabs {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.tabs {
  height: calc(100vh - 150px);
}

.friends-content,
.requests-content,
.groups-content {
  padding: 20px;
  height: calc(100% - 60px);
  overflow-y: auto;
}

.friends-list,
.requests-list,
.groups-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.friend-item,
.request-item,
.group-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: var(--bg-secondary, #f9f9f9);
  border-radius: 12px;
  transition: all 0.3s;
  
  &:hover {
    background: var(--bg-primary, #f0f0f0);
    transform: translateX(4px);
  }
}

.friend-avatar,
.request-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 16px;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.friend-info,
.request-info {
  flex: 1;
}

.friend-info h3,
.request-info h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary, #333);
  margin: 0 0 4px 0;
}

.friend-id {
  font-size: 12px;
  color: var(--text-tertiary, #999);
  margin: 0;
}

.request-message {
  font-size: 14px;
  color: var(--text-secondary, #666);
  margin: 4px 0;
}

.request-time {
  font-size: 12px;
  color: var(--text-tertiary, #999);
  margin: 0;
}

.friend-actions,
.request-actions,
.group-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 6px 12px;
  font-size: 13px;
  border-radius: 6px;
  
  &.primary {
    background: #667eea;
    border-color: #667eea;
    color: white;
  }
  
  &.danger {
    background: #ff6b6b;
    border-color: #ff6b6b;
    color: white;
  }
}

.accept-btn {
  background: #10b981;
  border-color: #10b981;
  color: white;
  padding: 6px 16px;
  border-radius: 6px;
}

.reject-btn {
  background: #ef4444;
  border-color: #ef4444;
  color: white;
  padding: 6px 16px;
  border-radius: 6px;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: var(--text-tertiary, #999);
}

.groups-header {
  margin-bottom: 16px;
}

.create-group-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  padding: 8px 20px;
  border-radius: 8px;
}

.group-info h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary, #333);
  margin: 0 0 4px 0;
}

.group-desc {
  font-size: 14px;
  color: var(--text-secondary, #666);
  margin: 0;
}

.group-form {
  padding: 20px;
}
</style>
