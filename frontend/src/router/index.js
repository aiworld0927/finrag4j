import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  // 登录页
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  // 首页工作台
  {
    path: '/',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  // 租户管理
  {
    path: '/tenant',
    name: 'Tenant',
    component: () => import('@/views/system/TenantManagement.vue'),
    meta: { requiresAuth: true }
  },
  // 用户管理
  {
    path: '/users',
    name: 'Users',
    component: () => import('@/views/system/UserManagement.vue'),
    meta: { requiresAuth: true }
  },
  // 角色管理
  {
    path: '/roles',
    name: 'Roles',
    component: () => import('@/views/system/RoleManagement.vue'),
    meta: { requiresAuth: true }
  },
  // 知识库管理
  {
    path: '/knowledge',
    name: 'Knowledge',
    component: () => import('@/views/knowledge/KnowledgeBase.vue'),
    meta: { requiresAuth: true }
  },
  // 文档管理
  {
    path: '/documents',
    name: 'Documents',
    component: () => import('@/views/knowledge/DocumentManagement.vue'),
    meta: { requiresAuth: true }
  },
  // RAG问答
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('@/views/chat/ChatPage.vue'),
    meta: { requiresAuth: true }
  },
  // 对话历史
  {
    path: '/chat-history',
    name: 'ChatHistory',
    component: () => import('@/views/chat/ChatHistory.vue'),
    meta: { requiresAuth: true }
  },
  // Agent中心
  {
    path: '/agent',
    name: 'Agent',
    component: () => import('@/views/agent/AgentCenter.vue'),
    meta: { requiresAuth: true }
  },
  // 工作流编排
  {
    path: '/workflow',
    name: 'Workflow',
    component: () => import('@/views/workflow/WorkflowDesigner.vue'),
    meta: { requiresAuth: true }
  },
  // 审计日志
  {
    path: '/audit',
    name: 'Audit',
    component: () => import('@/views/system/AuditLog.vue'),
    meta: { requiresAuth: true }
  },
  // 系统配置
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/system/Settings.vue'),
    meta: { requiresAuth: true }
  },
  // 运维监控
  {
    path: '/monitor',
    name: 'Monitor',
    component: () => import('@/views/monitor/Monitor.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router