<template>
  <div class="chat-history">
    <div class="page-header">
      <div class="header-left">
        <h2>对话历史</h2>
        <p>查看和管理所有对话记录</p>
      </div>
      <div class="header-right">
        <el-button type="primary" icon="Plus" @click="createNewChat">新对话</el-button>
        <el-button icon="Delete" type="danger" @click="batchDelete">批量删除</el-button>
      </div>
    </div>
    
    <div class="filter-bar">
      <el-input v-model="filter.keyword" placeholder="搜索对话标题" clearable style="width: 300px;">
        <template #prefix>
          <el-icon><component :is="icons.Search" /></el-icon>
        </template>
      </el-input>
      <el-select v-model="filter.dateRange" placeholder="时间范围" clearable style="margin-left: 16px;">
        <el-option label="今天" value="today" />
        <el-option label="本周" value="week" />
        <el-option label="本月" value="month" />
        <el-option label="全部" value="all" />
      </el-select>
    </div>
    
    <div class="history-list">
      <div
        v-for="chat in filteredChats"
        :key="chat.id"
        class="history-card"
        :class="{ selected: selectedIds.includes(chat.id) }"
        @click="toggleSelect(chat.id)"
        @dblclick="openChat(chat)"
      >
        <div class="card-header">
          <div class="checkbox-wrapper" @click.stop>
            <el-checkbox v-model="selectedIds" :label="chat.id" />
          </div>
          <div class="chat-info">
            <h3>{{ chat.title }}</h3>
            <p class="last-message">{{ chat.lastMessage }}</p>
          </div>
          <div class="chat-meta">
            <span class="chat-time">{{ chat.time }}</span>
            <el-button size="small" icon="Delete" type="text" @click.stop="deleteChat(chat.id)">删除</el-button>
          </div>
        </div>
        <div class="card-footer">
          <el-tag size="small" :type="getKBType(chat.kbName)">{{ chat.kbName }}</el-tag>
          <span class="message-count">{{ chat.messageCount }} 条消息</span>
        </div>
      </div>
    </div>
    
    <div class="pagination" v-if="filteredChats.length > 0">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
      />
    </div>
    
    <el-empty v-else description="暂无对话记录" />
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { icons } from '@/utils/icons'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

const selectedIds = ref([])

const filter = reactive({
  keyword: '',
  dateRange: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const chatHistory = ref([
  { id: 1, title: '信贷业务合规要求', lastMessage: '根据《商业银行信贷业务管理办法》的规定...', time: '今天 10:30', kbName: '金融法规知识库', messageCount: 12 },
  { id: 2, title: '监管政策解读', lastMessage: '最新监管政策主要包括以下几个方面...', time: '昨天 14:20', kbName: '金融法规知识库', messageCount: 8 },
  { id: 3, title: '风险控制措施', lastMessage: '风险控制的主要措施包括...', time: '2024-01-13', kbName: '信贷业务知识库', messageCount: 15 },
  { id: 4, title: '合同条款分析', lastMessage: '合同中的关键条款包括...', time: '2024-01-12', kbName: '信贷业务知识库', messageCount: 6 },
  { id: 5, title: '贷前审查要点', lastMessage: '贷前审查需要关注以下要点...', time: '2024-01-11', kbName: '信贷业务知识库', messageCount: 9 },
  { id: 6, title: '反洗钱合规要求', lastMessage: '反洗钱合规的主要要求包括...', time: '2024-01-10', kbName: '金融法规知识库', messageCount: 11 }
])

const filteredChats = computed(() => {
  let result = chatHistory.value
  if (filter.keyword) {
    result = result.filter(chat => 
      chat.title.includes(filter.keyword) || chat.lastMessage.includes(filter.keyword)
    )
  }
  pagination.total = result.length
  return result.slice((pagination.page - 1) * pagination.pageSize, pagination.page * pagination.pageSize)
})

const getKBType = (kbName) => {
  return kbName.includes('法规') ? 'primary' : 'success'
}

const toggleSelect = (id) => {
  const index = selectedIds.value.indexOf(id)
  if (index > -1) {
    selectedIds.value.splice(index, 1)
  } else {
    selectedIds.value.push(id)
  }
}

const createNewChat = () => {
  router.push('/chat')
}

const openChat = (chat) => {
  router.push('/chat')
}

const deleteChat = (id) => {
  chatHistory.value = chatHistory.value.filter(c => c.id !== id)
  ElMessage.success('删除成功')
}

const batchDelete = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的对话')
    return
  }
  chatHistory.value = chatHistory.value.filter(c => !selectedIds.value.includes(c.id))
  selectedIds.value = []
  ElMessage.success(`成功删除 ${selectedIds.value.length} 条对话`)
}
</script>

<style lang="scss">
.chat-history {
  padding: 20px;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 20px;
    
    .header-left {
      h2 {
        font-size: 24px;
        color: #1f2937;
        margin: 0 0 8px 0;
      }
      
      p {
        color: #6b7280;
        margin: 0;
      }
    }
  }
  
  .filter-bar {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
  }
  
  .history-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  
  .history-card {
    background: #fff;
    border-radius: 8px;
    padding: 16px;
    cursor: pointer;
    border: 2px solid transparent;
    
    &:hover {
      background: #f9fafb;
    }
    
    &.selected {
      border-color: #1890ff;
      background: #eff6ff;
    }
    
    .card-header {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .checkbox-wrapper {
        flex-shrink: 0;
      }
      
      .chat-info {
        flex: 1;
        
        h3 {
          font-size: 14px;
          color: #1f2937;
          margin: 0 0 4px 0;
        }
        
        .last-message {
          font-size: 13px;
          color: #6b7280;
          margin: 0;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
      
      .chat-meta {
        text-align: right;
        
        .chat-time {
          display: block;
          font-size: 12px;
          color: #9ca3af;
          margin-bottom: 4px;
        }
      }
    }
    
    .card-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 12px;
      padding-top: 12px;
      border-top: 1px solid #f3f4f6;
      
      .message-count {
        font-size: 12px;
        color: #9ca3af;
      }
    }
  }
  
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>