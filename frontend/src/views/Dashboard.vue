<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <h2>工作台</h2>
      <p>欢迎回来，{{ userInfo.nickname }}</p>
    </div>
    
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon blue">
          <el-icon :size="24"><component :is="icons.Database" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.knowledgeBases }}</div>
          <div class="stat-label">知识库数量</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon green">
          <el-icon :size="24"><component :is="icons.FileText" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.documents }}</div>
          <div class="stat-label">文档数量</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon purple">
          <el-icon :size="24"><component :is="icons.MessageSquare" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.questions }}</div>
          <div class="stat-label">今日问答</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon orange">
          <el-icon :size="24"><component :is="icons.Cpu" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.modelUsage }}%</div>
          <div class="stat-label">模型使用率</div>
        </div>
      </div>
    </div>
    
    <div class="dashboard-content">
      <div class="left-panel">
        <div class="panel">
          <h3>快速操作</h3>
          <div class="quick-actions">
            <el-button type="primary" icon="Upload" @click="goToUpload">上传文档</el-button>
            <el-button icon="MessageCircle" @click="goToChat">开始问答</el-button>
            <el-button icon="FileSearch" @click="goToCompliance">合规自查</el-button>
          </div>
        </div>
        
        <div class="panel">
          <h3>最近文档</h3>
          <el-table :data="recentDocs" border>
            <el-table-column prop="fileName" label="文件名" />
            <el-table-column prop="uploadTime" label="上传时间" />
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      
      <div class="right-panel">
        <div class="panel">
          <h3>问答趋势</h3>
          <div class="chart-container">
            <v-chart :option="chartOption" autoresize />
          </div>
        </div>
        
        <div class="panel">
          <h3>系统通知</h3>
          <div class="notifications">
            <div v-for="notification in notifications" :key="notification.id" class="notification">
              <el-icon :size="16" :color="notification.type === 'success' ? '#67c23a' : '#f56c6c'">
                <component :is="notification.type === 'success' ? icons.CheckCircle : icons.Warning" />
              </el-icon>
              <span>{{ notification.message }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { icons } from '@element-plus/icons-vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart } from 'echarts/charts'
import { GridComponent } from 'echarts/components'

use([CanvasRenderer, BarChart, GridComponent])

const router = useRouter()

const userInfo = reactive({
  nickname: '管理员'
})

const stats = reactive({
  knowledgeBases: 5,
  documents: 128,
  questions: 45,
  modelUsage: 68
})

const recentDocs = ref([
  { id: 1, fileName: '信贷合同模板.pdf', uploadTime: '2024-01-15', status: '已索引' },
  { id: 2, fileName: '监管政策解读.docx', uploadTime: '2024-01-14', status: '已索引' },
  { id: 3, fileName: '合规自查报告.xlsx', uploadTime: '2024-01-13', status: '处理中' },
  { id: 4, fileName: '产品说明书.pdf', uploadTime: '2024-01-12', status: '已索引' }
])

const notifications = ref([
  { id: 1, type: 'success', message: '知识库"金融法规"更新成功' },
  { id: 2, type: 'warning', message: '模型服务负载较高，请关注' },
  { id: 3, type: 'success', message: '文档批量上传完成' }
])

const chartOption = {
  xAxis: {
    type: 'category',
    data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  },
  yAxis: {
    type: 'value'
  },
  series: [{
    data: [30, 45, 35, 50, 42, 28, 35],
    type: 'bar',
    itemStyle: {
      color: '#1890ff'
    }
  }]
}

const getStatusType = (status) => {
  const types = { '已索引': 'success', '处理中': 'warning', '失败': 'danger' }
  return types[status] || 'info'
}

const goToUpload = () => router.push('/documents')
const goToChat = () => router.push('/chat')
const goToCompliance = () => router.push('/agent')
</script>

<style lang="scss">
.dashboard {
  padding: 20px;
  
  .dashboard-header {
    margin-bottom: 24px;
    
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
  
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 24px;
  }
  
  .stat-card {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    display: flex;
    align-items: center;
    gap: 16px;
    
    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      
      &.blue { background: #e6f7ff; color: #1890ff; }
      &.green { background: #f6ffed; color: #52c41a; }
      &.purple { background: #f9f0ff; color: #722ed1; }
      &.orange { background: #fff7e6; color: #fa8c16; }
    }
    
    .stat-info {
      .stat-value {
        font-size: 28px;
        font-weight: bold;
        color: #1f2937;
      }
      
      .stat-label {
        font-size: 14px;
        color: #6b7280;
      }
    }
  }
  
  .dashboard-content {
    display: flex;
    gap: 20px;
  }
  
  .left-panel, .right-panel {
    flex: 1;
  }
  
  .panel {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 20px;
    
    h3 {
      font-size: 16px;
      color: #374151;
      margin: 0 0 16px 0;
    }
  }
  
  .quick-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    
    .el-button {
      margin-bottom: 8px;
    }
  }
  
  .chart-container {
    height: 200px;
  }
  
  .notifications {
    .notification {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px;
      background: #f9fafb;
      border-radius: 8px;
      margin-bottom: 8px;
      
      span {
        font-size: 13px;
        color: #4b5563;
      }
    }
  }
}
</style>