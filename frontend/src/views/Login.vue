<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="#1890ff">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
          </svg>
        </div>
        <h1>FinRag4j</h1>
        <p>金融大模型RAG应用框架</p>
      </div>
      
      <div class="login-form">
        <div class="form-group">
          <label class="form-label">用户名</label>
          <el-input 
            v-model="form.username" 
            placeholder="请输入用户名"
            class="form-input"
          />
        </div>
        
        <div class="form-group">
          <label class="form-label">密码</label>
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码"
            class="form-input"
            @keyup.enter="handleLogin"
          />
        </div>
        
        <div class="form-group">
          <label class="form-label">租户ID</label>
          <el-input 
            v-model.number="form.tenantId" 
            placeholder="请输入租户ID"
            class="form-input"
          />
        </div>
        
        <el-button type="primary" class="login-btn" @click="handleLogin">
          登录
        </el-button>
      </div>
      
      <div class="login-footer">
        <span>开源版 | 企业版</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/api/auth'

const router = useRouter()

const form = reactive({
  username: '',
  password: '',
  tenantId: 1
})

const handleLogin = async () => {
  try {
    const response = await login(form)
    if (response.code === 200) {
      localStorage.setItem('token', response.data.accessToken)
      localStorage.setItem('user', JSON.stringify({ 
        username: response.data.username, 
        userId: response.data.userId 
      }))
      router.push('/')
    }
  } catch (error) {
    console.error('登录失败:', error)
  }
}
</script>

<style lang="scss">
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
  
  .logo {
    margin-bottom: 16px;
  }
  
  h1 {
    font-size: 28px;
    color: #1f2937;
    margin: 0 0 8px 0;
  }
  
  p {
    color: #6b7280;
    font-size: 14px;
    margin: 0;
  }
}

.login-form {
  .form-group {
    margin-bottom: 20px;
  }
  
  .form-label {
    display: block;
    padding: 0 0 8px 0;
    font-weight: 500;
    color: #374151;
    font-size: 14px;
  }
  
  .form-input {
    width: 100%;
    
    :deep(.el-input__wrapper) {
      height: 40px;
      border-radius: 6px;
    }
  }
  
  .login-btn {
    width: 100%;
    height: 44px;
    font-size: 16px;
    margin-top: 8px;
    border-radius: 6px;
  }
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
  
  span {
    color: #9ca3af;
    font-size: 13px;
  }
}
</style>
