<template>
  <div class="register-container">
    <div class="register-card">
      <div class="logo-section">
        <div class="logo">iwan</div>
        <p class="slogan">创建您的账号</p>
      </div>
      
      <el-form ref="formRef" :model="form" :rules="rules" class="register-form">
        <el-form-item label="账号" prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="请输入账号（小写字母+数字）"
            prefix-icon="user"
            size="large"
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码（6-20位）"
            prefix-icon="lock"
            size="large"
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="form.confirmPassword" 
            type="password" 
            placeholder="请确认密码"
            prefix-icon="lock"
            size="large"
          />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input 
            v-model="form.email" 
            placeholder="请输入邮箱（选填）"
            prefix-icon="mail"
            size="large"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading" size="large" class="register-btn">
            注册
          </el-button>
        </el-form-item>
        
        <div class="link-section">
          <span class="link" @click="goToLogin">已有账号？立即登录</span>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: ''
})

const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { pattern: /^[a-z0-9]+$/, message: '账号只能包含小写字母和数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (rule, value, callback) => {
      if (value !== form.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }, trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return
  
  const valid = await formRef.value.validate()
  if (!valid) return
  
  loading.value = true
  
  try {
    // 使用username作为默认昵称
    const nickname = form.username
    const response = await userStore.register(
      form.username, 
      form.password, 
      nickname,
      form.email
    )
    
    if (response.code === 200) {
      router.push('/login')
    }
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-card {
  width: 450px;
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.logo-section {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  font-size: 48px;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 8px;
}

.slogan {
  font-size: 14px;
  color: #999;
}

.register-form {
  margin-top: 20px;
}

.register-btn {
  width: 100%;
}

.link-section {
  text-align: center;
  margin-top: 20px;
}

.link {
  color: #667eea;
  font-size: 14px;
  cursor: pointer;
  
  &:hover {
    text-decoration: underline;
  }
}
</style>
