<template>
  <div class="document-management">
    <div class="page-header">
      <div class="header-left">
        <h2>文档管理</h2>
        <p>上传、预览和管理知识库文档</p>
      </div>
      <div class="header-right">
        <el-button type="primary" icon="Upload" @click="showUploadModal = true">
          上传文档
        </el-button>
      </div>
    </div>
    
    <div class="filter-bar">
      <el-select v-model="filter.status" placeholder="状态筛选" clearable>
        <el-option label="全部" value="" />
        <el-option label="已索引" value="indexed" />
        <el-option label="处理中" value="processing" />
        <el-option label="失败" value="failed" />
      </el-select>
      <el-input v-model="filter.keyword" placeholder="搜索文档" clearable style="width: 300px; margin-left: 16px;">
        <template #prefix>
          <el-icon><component :is="icons.Search" /></el-icon>
        </template>
      </el-input>
    </div>
    
    <el-table :data="documents" border>
      <el-table-column prop="fileName" label="文件名" min-width="200" />
      <el-table-column prop="fileType" label="类型" width="100">
        <template #default="scope">
          <el-tag size="small">{{ scope.row.fileType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="fileSize" label="大小" width="100">
        <template #default="scope">
          {{ formatSize(scope.row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)" size="small">
            {{ scope.row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="chunkCount" label="分块数" width="100" />
      <el-table-column prop="uploadTime" label="上传时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button size="small" icon="View" @click="previewDoc(scope.row)">预览</el-button>
          <el-button size="small" icon="Download" @click="downloadDoc(scope.row)">下载</el-button>
          <el-button size="small" icon="Delete" type="danger" @click="deleteDoc(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
      />
    </div>
    
    <!-- 上传模态框 -->
    <el-dialog title="上传文档" v-model="showUploadModal" width="500px">
      <el-upload
        drag
        multiple
        :auto-upload="false"
        :on-change="handleFileChange"
        :file-list="fileList"
      >
        <el-icon class="el-icon--upload"><component :is="icons.UploadFilled" /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持 PDF、Word、Excel、TXT 格式，单文件最大 50MB
          </div>
        </template>
      </el-upload>
      
      <el-form :model="uploadForm" label-width="100px" style="margin-top: 20px;">
        <el-form-item label="知识库">
          <el-select v-model="uploadForm.kbId" placeholder="请选择知识库">
            <el-option label="金融法规知识库" :value="1" />
            <el-option label="信贷业务知识库" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showUploadModal = false">取消</el-button>
        <el-button type="primary" @click="uploadFiles">开始上传</el-button>
      </template>
    </el-dialog>
    
    <!-- 预览模态框 -->
    <el-dialog title="文档预览" v-model="showPreviewModal" width="80%" top="5vh">
      <div class="preview-content">
        <div v-if="previewContent" class="text-content">
          {{ previewContent }}
        </div>
        <el-empty v-else description="暂无预览内容" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { icons } from '@element-plus/icons-vue'

const showUploadModal = ref(false)
const showPreviewModal = ref(false)
const fileList = ref([])
const previewContent = ref('')

const filter = reactive({
  status: '',
  keyword: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 100
})

const uploadForm = reactive({
  kbId: null
})

const documents = ref([
  { id: 1, fileName: '信贷合同模板.pdf', fileType: 'PDF', fileSize: 2048000, status: 'indexed', chunkCount: 45, uploadTime: '2024-01-15 10:30:00' },
  { id: 2, fileName: '监管政策解读.docx', fileType: 'DOCX', fileSize: 1536000, status: 'indexed', chunkCount: 32, uploadTime: '2024-01-14 14:20:00' },
  { id: 3, fileName: '合规自查报告.xlsx', fileType: 'XLSX', fileSize: 512000, status: 'processing', chunkCount: 0, uploadTime: '2024-01-13 09:15:00' },
  { id: 4, fileName: '产品说明书.txt', fileType: 'TXT', fileSize: 102400, status: 'indexed', chunkCount: 12, uploadTime: '2024-01-12 16:45:00' }
])

const formatSize = (bytes) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

const getStatusType = (status) => {
  const types = { indexed: 'success', processing: 'warning', failed: 'danger' }
  return types[status] || 'info'
}

const handleFileChange = (file) => {
  fileList.value.push(file)
}

const uploadFiles = () => {
  showUploadModal.value = false
  fileList.value = []
}

const previewDoc = (doc) => {
  previewContent.value = '这是文档的预览内容...\n\n文档名称: ' + doc.fileName + '\n\n[这里是解析后的文本内容]'
  showPreviewModal.value = true
}

const downloadDoc = (doc) => {
  console.log('下载文档:', doc.fileName)
}

const deleteDoc = (doc) => {
  documents.value = documents.value.filter(d => d.id !== doc.id)
}
</script>

<style lang="scss">
.document-management {
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
  
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  .preview-content {
    max-height: 60vh;
    overflow-y: auto;
    
    .text-content {
      white-space: pre-wrap;
      font-family: monospace;
      font-size: 14px;
      line-height: 1.6;
      color: #374151;
    }
  }
}
</style>