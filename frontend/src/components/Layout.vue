<template>
  <div class="app-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-logo">
        <div class="logo-icon">
          <el-icon :size="32" color="#1890ff">
            <component :is="Database" />
          </el-icon>
        </div>
        <span class="logo-text">FinRag4j</span>
      </div>
      
      <nav class="sidebar-nav">
        <div
          v-for="item in menuItems"
          :key="item.path"
          class="nav-item"
          :class="{ active: currentPath === item.path || currentPath.startsWith(item.path) }"
          @click="navigate(item.path)"
        >
          <el-icon :size="20">
            <component :is="item.icon" />
          </el-icon>
          <span>{{ item.label }}</span>
          <el-icon v-if="item.children" class="expand-icon" :size="16">
            <component :is="expandIcons[item.path] ? ChevronDown : ChevronRight" />
          </el-icon>
          
          <div v-if="item.children && expandIcons[item.path]" class="sub-menu">
            <div
              v-for="child in item.children"
              :key="child.path"
              class="sub-item"
              :class="{ active: currentPath === child.path }"
              @click.stop="navigate(child.path)"
            >
              {{ child.label }}
            </div>
          </div>
        </div>
      </nav>
    </aside>
    
    <!-- 主内容区域 -->
    <main class="main-content">
      <!-- 顶部导航栏 -->
      <header class="top-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentPageName }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-button icon="Bell" class="header-btn" />
          <el-button icon="User" class="header-btn" />
          <el-dropdown>
            <span class="user-info">
              <el-avatar size="32" icon="User" />
              <span>{{ userInfo.nickname }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item icon="User">个人中心</el-dropdown-item>
                <el-dropdown-item icon="Settings">系统设置</el-dropdown-item>
                <el-dropdown-item icon="LogOut" @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      
      <!-- 页面内容 -->
      <div class="page-container">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { icons } from '@/utils/icons'
import { ElMessage } from 'element-plus'

const { 
  LayoutDashboard, 
  Database, 
  MessageSquare, 
  Robot, 
  FlowChart, 
  Settings, 
  Activity,
  ChevronDown,
  ChevronRight
} = icons

const router = useRouter()
const route = useRoute()

const currentPath = ref('/')
const userInfo = reactive({
  nickname: '管理员'
})

const expandIcons = reactive({})

const menuItems = [
  { path: '/', label: '工作台', icon: LayoutDashboard },
  { path: '/knowledge', label: '知识库管理', icon: Database, children: [
    { path: '/knowledge', label: '知识库列表' },
    { path: '/documents', label: '文档管理' }
  ]},
  { path: '/chat', label: '问答中心', icon: MessageSquare, children: [
    { path: '/chat', label: 'RAG问答' },
    { path: '/chat-history', label: '对话历史' }
  ]},
  { path: '/agent', label: 'Agent中心', icon: Robot },
  { path: '/workflow', label: '工作流编排', icon: FlowChart },
  { path: '/tenant', label: '系统管理', icon: Settings, children: [
    { path: '/tenant', label: '租户管理' },
    { path: '/users', label: '用户管理' },
    { path: '/roles', label: '角色管理' },
    { path: '/audit', label: '审计日志' },
    { path: '/settings', label: '系统配置' }
  ]},
  { path: '/monitor', label: '运维监控', icon: Activity }
]

const currentPageName = computed(() => {
  const findItem = (items) => {
    for (const item of items) {
      if (currentPath.value === item.path || currentPath.value.startsWith(item.path)) {
        return item.label
      }
      if (item.children) {
        const found = findItem(item.children)
        if (found) return found
      }
    }
    return ''
  }
  return findItem(menuItems)
})

const navigate = (path) => {
  // 处理有子菜单的情况
  const item = menuItems.find(i => i.path === path)
  if (item && item.children) {
    expandIcons[path] = !expandIcons[path]
  } else {
    router.push(path)
  }
}

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
  ElMessage.success('退出成功')
}

onMounted(() => {
  currentPath.value = route.path
  router.afterEach((to) => {
    currentPath.value = to.path
  })
})
</script>

<style lang="scss">
.app-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

.sidebar {
  width: 220px;
  background: #0f172a;
  color: #fff;
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  height: 100vh;
  z-index: 100;
  
  .sidebar-logo {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 20px;
    border-bottom: 1px solid #1e293b;
    
    .logo-icon {
      width: 40px;
      height: 40px;
      background: #1890ff;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .logo-text {
      font-size: 18px;
      font-weight: bold;
    }
  }
  
  .sidebar-nav {
    flex: 1;
    padding: 12px;
    overflow-y: auto;
    
    .nav-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 12px 16px;
      border-radius: 8px;
      cursor: pointer;
      margin-bottom: 4px;
      transition: all 0.2s;
      
      &:hover {
        background: #1e293b;
      }
      
      &.active {
        background: #1890ff;
      }
      
      .expand-icon {
        margin-left: auto;
        color: #94a3b8;
      }
      
      span {
        font-size: 14px;
      }
    }
    
    .sub-menu {
      margin-top: 4px;
      padding-left: 20px;
      
      .sub-item {
        padding: 10px 16px;
        border-radius: 6px;
        font-size: 13px;
        color: #94a3b8;
        cursor: pointer;
        
        &:hover {
          background: #1e293b;
          color: #fff;
        }
        
        &.active {
          background: #1e293b;
          color: #1890ff;
        }
      }
    }
  }
}

.main-content {
  flex: 1;
  margin-left: 220px;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  
  .top-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
    background: #fff;
    border-bottom: 1px solid #e5e7eb;
    
    .header-left {
      .el-breadcrumb {
        font-size: 14px;
      }
    }
    
    .header-right {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .header-btn {
        padding: 8px;
      }
      
      .user-info {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 8px 12px;
        cursor: pointer;
        
        span {
          font-size: 14px;
          color: #374151;
        }
      }
    }
  }
  
  .page-container {
    flex: 1;
    padding: 24px;
    overflow-y: auto;
  }
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>