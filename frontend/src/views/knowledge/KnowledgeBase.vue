<template>
  <div class="knowledge-base">
    <div class="page-header">
      <div class="header-left">
        <h2>知识库管理</h2>
        <p>管理您的知识库和文档分类</p>
      </div>
      <div class="header-right">
        <el-button type="primary" icon="Plus" @click="showCreateModal = true">
          创建知识库
        </el-button>
      </div>
    </div>
    
    <div class="kb-list">
      <div v-for="kb in knowledgeBases" :key="kb.id" class="kb-card">
        <div class="kb-header">
          <div class="kb-icon">
            <el-icon :size="32" color="#1890ff">
              <component :is="icons.Database" />
            </el-icon>
          </div>
          <div class="kb-info">
            <h3>{{ kb.kbName }}</h3>
            <p class="kb-code">{{ kb.kbCode }}</p>
          </div>
          <div class="kb-actions">
            <el-button size="small" icon="Edit" @click="editKB(kb)">编辑</el-button>
            <el-button size="small" icon="Delete" type="danger" @click="deleteKB(kb)">删除</el-button>
          </div>
        </div>
        
        <div class="kb-stats">
          <div class="stat-item">
            <span class="stat-value">{{ kb.documentCount }}</span>
            <span class="stat-label">文档数</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ kb.similarityThreshold }}%</span>
            <span class="stat-label">相似度阈值</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ kb.topK }}</span>
            <span class="stat-label">召回数量</span>
          </div>
        </div>
        
        <p class="kb-desc">{{ kb.description }}</p>
        
        <div class="kb-tags">
          <el-tag v-for="tag in kb.tags" :key="tag" size="small">{{ tag }}</el-tag>
        </div>
        
        <div class="kb-footer">
          <span class="kb-status" :class="kb.status">{{ kb.status }}</span>
          <span class="kb-time">{{ kb.createdAt }}</span>
        </div>
      </div>
    </div>
    
    <!-- 创建/编辑模态框 -->
    <el-dialog :title="isEdit ? '编辑知识库' : '创建知识库'" v-model="showCreateModal">
      <el-form :model="form" label-width="120px">
        <el-form-item label="知识库名称">
          <el-input v-model="form.kbName" placeholder="请输入知识库名称" />
        </el-form-item>
        <el-form-item label="知识库编码">
          <el-input v-model="form.kbCode" placeholder="请输入知识库编码" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input type="textarea" v-model="form.description" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="默认模型">
          <el-select v-model="form.defaultModel" placeholder="请选择模型">
            <el-option label="Qwen2-7B" value="qwen2:7b" />
            <el-option label="Qwen2-14B" value="qwen2:14b" />
            <el-option label="Qwen2-72B" value="qwen2:72b" />
          </el-select>
        </el-form-item>
        <el-form-item label="相似度阈值">
          <el-slider v-model="form.similarityThreshold" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="召回数量">
          <el-input-number v-model="form.topK" :min="1" :max="50" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateModal = false">取消</el-button>
        <el-button type="primary" @click="saveKB">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { icons } from '@/utils/icons'

const showCreateModal = ref(false)
const isEdit = ref(false)

const form = reactive({
  kbName: '',
  kbCode: '',
  description: '',
  defaultModel: 'qwen2:7b',
  similarityThreshold: 70,
  topK: 10
})

const knowledgeBases = ref([
  {
    id: 1,
    kbName: '金融法规知识库',
    kbCode: 'finance_regulation',
    description: '收录各类金融监管法规、政策文件',
    status: 'active',
    documentCount: 128,
    similarityThreshold: 70,
    topK: 10,
    defaultModel: 'qwen2:7b',
    tags: ['法规', '政策'],
    createdAt: '2024-01-10'
  },
  {
    id: 2,
    kbName: '信贷业务知识库',
    kbCode: 'credit_business',
    description: '信贷业务相关文档、合同模板',
    status: 'active',
    documentCount: 86,
    similarityThreshold: 75,
    topK: 15,
    defaultModel: 'qwen2:14b',
    tags: ['信贷', '合同'],
    createdAt: '2024-01-12'
  }
])

const editKB = (kb) => {
  isEdit.value = true
  Object.assign(form, kb)
  showCreateModal.value = true
}

const deleteKB = (kb) => {
  knowledgeBases.value = knowledgeBases.value.filter(k => k.id !== kb.id)
}

const saveKB = () => {
  showCreateModal.value = false
  isEdit.value = false
  Object.keys(form).forEach(key => form[key] = '')
  form.similarityThreshold = 70
  form.topK = 10
}
</script>

<style lang="scss">
.knowledge-base {
  padding: 20px;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;
    
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
  
  .kb-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
    gap: 20px;
  }
  
  .kb-card {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    
    .kb-header {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 16px;
      
      .kb-icon {
        flex-shrink: 0;
      }
      
      .kb-info {
        flex: 1;
        
        h3 {
          font-size: 16px;
          color: #1f2937;
          margin: 0 0 4px 0;
        }
        
        .kb-code {
          font-size: 12px;
          color: #9ca3af;
          margin: 0;
        }
      }
      
      .kb-actions {
        .el-button {
          margin-left: 8px;
        }
      }
    }
    
    .kb-stats {
      display: flex;
      gap: 24px;
      margin-bottom: 12px;
      
      .stat-item {
        display: flex;
        flex-direction: column;
        
        .stat-value {
          font-size: 20px;
          font-weight: bold;
          color: #1f2937;
        }
        
        .stat-label {
          font-size: 12px;
          color: #6b7280;
        }
      }
    }
    
    .kb-desc {
      font-size: 13px;
      color: #4b5563;
      margin: 0 0 12px 0;
      line-height: 1.5;
    }
    
    .kb-tags {
      margin-bottom: 12px;
    }
    
    .kb-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .kb-status {
        font-size: 12px;
        padding: 2px 8px;
        border-radius: 4px;
        
        &.active {
          background: #d1fae5;
          color: #065f46;
        }
        
        &.inactive {
          background: #f3f4f6;
          color: #6b7280;
        }
      }
      
      .kb-time {
        font-size: 12px;
        color: #9ca3af;
      }
    }
  }
}
</style>