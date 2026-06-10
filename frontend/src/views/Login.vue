<template>
  <div class="login-container">
    <div class="login-card">
      <div class="logo-section">
        <div class="logo">iwan</div>
        <p class="slogan">个人社交博客系统</p>
      </div>
      
      <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
        <el-form-item label="账号" prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="请输入账号"
            prefix-icon="user"
            size="large"
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码"
            prefix-icon="lock"
            size="large"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" size="large" class="login-btn">
            登录
          </el-button>
        </el-form-item>
        
        <div class="link-section">
          <span class="link" @click="goToRegister">注册账号</span>
          <span class="link">忘记密码？</span>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
const router = useRouter();
const userStore = useUserStore();
const formRef = ref(null);
const loading = ref(false);
const form = reactive({
 username: '',
 password: ''
});
const rules = {
 username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
 password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};
const handleLogin = async () => {
 if (!formRef.value)
 return;
 const valid = await formRef.value.validate();
 if (!valid)
 return;
 loading.value = true;
 try {
 const response = await userStore.login(form.username, form.password);
 if (response.code === 200) {
 router.push('/square');
 }
 }
 catch (error) {
 console.error('登录失败:', error);
 }
 finally {
 loading.value = false;
 }
};
const goToRegister = () => {
 router.push('/register');
};
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
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

.login-form {
  margin-top: 20px;
}

.login-btn {
  width: 100%;
}

.link-section {
  display: flex;
  justify-content: space-between;
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
