<template>
  <div class="chat-page">
    <div class="chat-sidebar">
      <div class="sidebar-header">
        <h3>对话历史</h3>
        <el-button type="primary" size="small" icon="Plus" @click="newChat">新对话</el-button>
      </div>
      
      <div class="chat-list">
        <div
          v-for="chat in chatHistory"
          :key="chat.id"
          class="chat-item"
          :class="{ active: currentChatId === chat.id }"
          @click="selectChat(chat.id)"
        >
          <div class="chat-title">{{ chat.title }}</div>
          <div class="chat-time">{{ chat.time }}</div>
        </div>
      </div>
      
      <div class="sidebar-footer">
        <el-select v-model="selectedKB" placeholder="选择知识库" size="small">
          <el-option label="金融法规知识库" :value="1" />
          <el-option label="信贷业务知识库" :value="2" />
        </el-select>
      </div>
    </div>
    
    <div class="chat-main">
      <div class="chat-messages" ref="messagesContainer">
        <div v-for="msg in messages" :key="msg.id" class="message" :class="msg.role">
          <div class="message-avatar">
            <el-avatar v-if="msg.role === 'user'" :size="36" icon="User" />
            <el-avatar v-else :size="36" icon="Cpu" style="background: #1890ff;" />
          </div>
          <div class="message-content">
            <div class="message-text">{{ msg.content }}</div>
            <div v-if="msg.role === 'assistant'" class="message-meta">
              <span class="meta-item">
                <el-icon><component :is="icons.Clock" /></el-icon>
                {{ msg.responseTime }}ms
              </span>
              <span class="meta-item">
                <el-icon><component :is="icons.Connection" /></el-icon>
                相似度: {{ (msg.similarity * 100).toFixed(1) }}%
              </span>
              <span class="meta-item">
                <el-icon><component :is="icons.Monitor" /></el-icon>
                {{ msg.model }}
              </span>
            </div>
            <div v-if="msg.references && msg.references.length > 0" class="message-references">
              <div class="ref-title">参考来源:</div>
              <div v-for="ref in msg.references" :key="ref.id" class="ref-item">
                <el-icon><component :is="icons.FileText" /></el-icon>
                {{ ref.fileName }} (第{{ ref.pageNum }}页)
              </div>
            </div>
            <div v-if="msg.role === 'assistant'" class="message-actions">
              <el-button size="small" text icon="Star" @click="collectMessage(msg)">收藏</el-button>
              <el-button size="small" text icon="CopyDocument" @click="copyMessage(msg)">复制</el-button>
            </div>
          </div>
        </div>
      </div>
      
      <div class="chat-input">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="3"
          placeholder="请输入您的问题..."
          @keyup.enter.ctrl="sendMessage"
        />
        <div class="input-actions">
          <div class="input-tips">
            <span>Ctrl + Enter 发送</span>
          </div>
          <el-button type="primary" icon="Position" @click="sendMessage" :loading="sending">
            发送
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick } from 'vue'
import { icons } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const messagesContainer = ref(null)
const inputMessage = ref('')
const sending = ref(false)
const currentChatId = ref(1)
const selectedKB = ref(1)

const messages = ref([
  {
    id: 1,
    role: 'user',
    content: '什么是信贷业务的合规要求？'
  },
  {
    id: 2,
    role: 'assistant',
    content: '根据《商业银行信贷业务管理办法》的规定，信贷业务的合规要求主要包括以下几个方面：\n\n1. **贷前调查**：银行应对借款人的主体资格、资信状况、还款能力等进行全面调查。\n\n2. **贷时审查**：审查贷款用途的合规性，确保贷款用途符合国家产业政策和信贷政策。\n\n3. **贷后管理**：定期检查贷款使用情况，及时发现和处置风险。\n\n4. **档案管理**：建立健全信贷档案管理制度，确保档案完整、准确。',
    responseTime: 1250,
    similarity: 0.85,
    model: 'qwen2:7b',
    references: [
      { id: 1, fileName: '信贷业务管理办法.pdf', pageNum: 12 },
      { id: 2, fileName: '合规指引.docx', pageNum: 5 }
    ]
  }
])

const chatHistory = ref([
  { id: 1, title: '信贷业务合规要求', time: '今天 10:30' },
  { id: 2, title: '监管政策解读', time: '昨天 14:20' },
  { id: 3, title: '风险控制措施', time: '2024-01-13' }
])

const newChat = () => {
  currentChatId.value = Date.now()
  messages.value = []
}

const selectChat = (id) => {
  currentChatId.value = id
}

const sendMessage = async () => {
  if (!inputMessage.value.trim()) return
  
  const userMsg = {
    id: Date.now(),
    role: 'user',
    content: inputMessage.value
  }
  messages.value.push(userMsg)
  
  const question = inputMessage.value
  inputMessage.value = ''
  sending.value = true
  
  // 模拟AI响应
  setTimeout(() => {
    const aiMsg = {
      id: Date.now() + 1,
      role: 'assistant',
      content: '这是对您问题的回答。根据知识库中的相关文档，我为您整理了以下信息...',
      responseTime: Math.floor(Math.random() * 2000) + 500,
      similarity: Math.random() * 0.3 + 0.7,
      model: 'qwen2:7b',
      references: [
        { id: 1, fileName: '相关文档.pdf', pageNum: Math.floor(Math.random() * 50) + 1 }
      ]
    }
    messages.value.push(aiMsg)
    sending.value = false
    
    nextTick(() => {
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      }
    })
  }, 1500)
}

const collectMessage = (msg) => {
  ElMessage.success('已收藏')
}

const copyMessage = (msg) => {
  navigator.clipboard.writeText(msg.content)
  ElMessage.success('已复制到剪贴板')
}
</script>

<style lang="scss">
.chat-page {
  display: flex;
  height: calc(100vh - 60px);
  
  .chat-sidebar {
    width: 280px;
    background: #fff;
    border-right: 1px solid #e5e7eb;
    display: flex;
    flex-direction: column;
    
    .sidebar-header {
      padding: 16px;
      border-bottom: 1px solid #e5e7eb;
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h3 {
        margin: 0;
        font-size: 16px;
        color: #1f2937;
      }
    }
    
    .chat-list {
      flex: 1;
      overflow-y: auto;
      
      .chat-item {
        padding: 12px 16px;
        cursor: pointer;
        border-bottom: 1px solid #f3f4f6;
        
        &:hover {
          background: #f9fafb;
        }
        
        &.active {
          background: #eff6ff;
          border-left: 3px solid #1890ff;
        }
        
        .chat-title {
          font-size: 14px;
          color: #1f2937;
          margin-bottom: 4px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        .chat-time {
          font-size: 12px;
          color: #9ca3af;
        }
      }
    }
    
    .sidebar-footer {
      padding: 16px;
      border-top: 1px solid #e5e7eb;
    }
  }
  
  .chat-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    background: #f9fafb;
    
    .chat-messages {
      flex: 1;
      overflow-y: auto;
      padding: 20px;
      
      .message {
        display: flex;
        gap: 12px;
        margin-bottom: 20px;
        
        &.user {
          flex-direction: row-reverse;
          
          .message-content {
            background: #1890ff;
            color: #fff;
            border-radius: 12px 12px 0 12px;
          }
        }
        
        &.assistant {
          .message-content {
            background: #fff;
            border-radius: 12px 12px 12px 0;
          }
        }
        
        .message-content {
          max-width: 70%;
          padding: 16px;
          
          .message-text {
            font-size: 14px;
            line-height: 1.6;
            white-space: pre-wrap;
          }
          
          .message-meta {
            display: flex;
            gap: 16px;
            margin-top: 12px;
            padding-top: 12px;
            border-top: 1px solid #e5e7eb;
            
            .meta-item {
              display: flex;
              align-items: center;
              gap: 4px;
              font-size: 12px;
              color: #6b7280;
            }
          }
          
          .message-references {
            margin-top: 12px;
            padding: 12px;
            background: #f9fafb;
            border-radius: 8px;
            
            .ref-title {
              font-size: 12px;
              color: #6b7280;
              margin-bottom: 8px;
            }
            
            .ref-item {
              display: flex;
              align-items: center;
              gap: 6px;
              font-size: 13px;
              color: #4b5563;
              margin-bottom: 4px;
            }
          }
          
          .message-actions {
            margin-top: 12px;
            display: flex;
            gap: 8px;
          }
        }
      }
    }
    
    .chat-input {
      padding: 20px;
      background: #fff;
      border-top: 1px solid #e5e7eb;
      
      .input-actions {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: 12px;
        
        .input-tips {
          font-size: 12px;
          color: #9ca3af;
        }
      }
    }
  }
}
</style>