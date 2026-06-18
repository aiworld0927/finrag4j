<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo">
          <el-icon :size="48" color="#1890ff">
            <component :is="icons.Brain" />
          </el-icon>
        </div>
        <h1>FinRag4j</h1>
        <p>金融大模型RAG应用框架</p>
      </div>
      
      <el-form ref="loginForm" :model="form" class="login-form" @submit.prevent="handleLogin">
        <el-form-item prop="username" label="用户名">
          <el-input 
            v-model="form.username" 
            placeholder="请输入用户名"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password" label="密码">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码"
            prefix-icon="Lock"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item prop="tenantId" label="租户ID">
          <el-input 
            v-model.number="form.tenantId" 
            placeholder="请输入租户ID"
            prefix-icon="Building"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" class="login-btn" @click="handleLogin">
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <span>开源版 | 企业版</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { icons } from '@element-plus/icons-vue'
import { login } from '@/api/auth'

const router = useRouter()
const loginForm = ref(null)

const form = reactive({
  username: '',
  password: '',
  tenantId: 1
})

const handleLogin = async () => {
  try {
    const response = await login(form)
    if (response.success) {
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('user', JSON.stringify(response.data.user))
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
  .login-btn {
    width: 100%;
    height: 44px;
    font-size: 16px;
    margin-top: 16px;
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