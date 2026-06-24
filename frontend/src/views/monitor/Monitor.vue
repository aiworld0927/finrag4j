<template>
  <div class="monitor">
    <div class="page-header">
      <h2>运维监控</h2>
      <p>实时监控系统运行状态</p>
    </div>
    
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon blue">
          <el-icon :size="24"><component :is="icons.Cpu" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.cpuUsage }}%</div>
          <div class="stat-label">CPU使用率</div>
        </div>
        <div class="stat-bar">
          <div class="bar-fill" :style="{ width: stats.cpuUsage + '%' }"></div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon green">
          <el-icon :size="24"><component :is="icons.HardDrive" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.memoryUsage }}%</div>
          <div class="stat-label">内存使用率</div>
        </div>
        <div class="stat-bar">
          <div class="bar-fill" :style="{ width: stats.memoryUsage + '%' }"></div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon purple">
          <el-icon :size="24"><component :is="icons.User" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.onlineUsers }}</div>
          <div class="stat-label">在线用户</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon orange">
          <el-icon :size="24"><component :is="icons.Server" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.serviceCount }}</div>
          <div class="stat-label">服务数量</div>
        </div>
      </div>
    </div>
    
    <div class="monitor-content">
      <div class="left-panel">
        <div class="panel">
          <h3>服务状态</h3>
          <div class="service-list">
            <div v-for="service in services" :key="service.name" class="service-item">
              <div class="service-status" :class="service.status"></div>
              <div class="service-info">
                <span class="service-name">{{ service.name }}</span>
                <span class="service-host">{{ service.host }}</span>
              </div>
              <div class="service-metrics">
                <span>{{ service.responseTime }}ms</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="panel">
          <h3>数据库状态</h3>
          <div class="db-status">
            <div class="db-item">
              <span class="db-name">PostgreSQL</span>
              <el-tag :type="dbStatus.postgresql ? 'success' : 'danger'">
                {{ dbStatus.postgresql ? '正常' : '异常' }}
              </el-tag>
            </div>
            <div class="db-item">
              <span class="db-name">Redis</span>
              <el-tag :type="dbStatus.redis ? 'success' : 'danger'">
                {{ dbStatus.redis ? '正常' : '异常' }}
              </el-tag>
            </div>
            <div class="db-item">
              <span class="db-name">MinIO</span>
              <el-tag :type="dbStatus.minio ? 'success' : 'danger'">
                {{ dbStatus.minio ? '正常' : '异常' }}
              </el-tag>
            </div>
            <div class="db-item">
              <span class="db-name">Nacos</span>
              <el-tag :type="dbStatus.nacos ? 'success' : 'danger'">
                {{ dbStatus.nacos ? '正常' : '异常' }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
      
      <div class="right-panel">
        <div class="panel">
          <h3>系统负载趋势</h3>
          <div class="chart-container">
            <v-chart :option="chartOption" autoresize />
          </div>
        </div>
        
        <div class="panel">
          <h3>最近告警</h3>
          <div class="alert-list">
            <div v-for="alert in alerts" :key="alert.id" class="alert-item" :class="alert.level">
              <el-icon :size="16">
                <component :is="alert.level === 'error' ? icons.Warning : icons.Info" />
              </el-icon>
              <div class="alert-content">
                <span class="alert-title">{{ alert.title }}</span>
                <span class="alert-time">{{ alert.time }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { icons } from '@/utils/icons'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent } from 'echarts/components'

use([CanvasRenderer, LineChart, GridComponent])

const stats = reactive({
  cpuUsage: 45,
  memoryUsage: 62,
  onlineUsers: 128,
  serviceCount: 6
})

const services = ref([
  { name: 'Gateway', host: '11.0.1.190:8080', status: 'running', responseTime: 23 },
  { name: 'Auth', host: '11.0.1.190:8081', status: 'running', responseTime: 15 },
  { name: 'Document', host: '11.0.1.190:9082', status: 'running', responseTime: 45 },
  { name: 'Search', host: '11.0.1.190:8083', status: 'running', responseTime: 32 },
  { name: 'Agent', host: '11.0.1.190:8084', status: 'running', responseTime: 58 },
  { name: 'Python', host: '11.0.1.190:8090', status: 'running', responseTime: 18 }
])

const dbStatus = reactive({
  postgresql: true,
  redis: true,
  minio: true,
  nacos: true
})

const alerts = ref([
  { id: 1, level: 'warning', title: '内存使用率超过60%', time: '5分钟前' },
  { id: 2, level: 'info', title: 'Document服务重启', time: '1小时前' },
  { id: 3, level: 'warning', title: 'API请求超时次数增加', time: '2小时前' }
])

const timeLabels = []
const cpuData = []
const memoryData = []

for (let i = 23; i >= 0; i--) {
  const hour = new Date()
  hour.setHours(hour.getHours() - i)
  timeLabels.push(`${hour.getHours().toString().padStart(2, '0')}:00`)
  cpuData.push(Math.floor(Math.random() * 40) + 30)
  memoryData.push(Math.floor(Math.random() * 30) + 50)
}

const chartOption = {
  xAxis: {
    type: 'category',
    data: timeLabels,
    axisLabel: {
      fontSize: 10,
      color: '#6b7280'
    }
  },
  yAxis: {
    type: 'value',
    min: 0,
    max: 100,
    axisLabel: {
      formatter: '{value}%',
      fontSize: 10,
      color: '#6b7280'
    }
  },
  series: [
    {
      name: 'CPU',
      data: cpuData,
      type: 'line',
      smooth: true,
      lineStyle: {
        color: '#1890ff',
        width: 2
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
            { offset: 1, color: 'rgba(24, 144, 255, 0.05)' }
          ]
        }
      }
    },
    {
      name: '内存',
      data: memoryData,
      type: 'line',
      smooth: true,
      lineStyle: {
        color: '#52c41a',
        width: 2
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(82, 196, 26, 0.3)' },
            { offset: 1, color: 'rgba(82, 196, 26, 0.05)' }
          ]
        }
      }
    }
  ],
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    top: '10%',
    containLabel: true
  },
  legend: {
    data: ['CPU', '内存'],
    bottom: 0,
    textStyle: {
      fontSize: 12,
      color: '#6b7280'
    }
  }
}

let interval = null

onMounted(() => {
  interval = setInterval(() => {
    stats.cpuUsage = Math.floor(Math.random() * 40) + 30
    stats.memoryUsage = Math.floor(Math.random() * 30) + 50
    stats.onlineUsers = Math.floor(Math.random() * 50) + 100
  }, 3000)
})

onUnmounted(() => {
  if (interval) {
    clearInterval(interval)
  }
})
</script>

<style lang="scss">
.monitor {
  padding: 20px;
  
  .page-header {
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
    
    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 12px;
      
      &.blue { background: #e6f7ff; color: #1890ff; }
      &.green { background: #f6ffed; color: #52c41a; }
      &.purple { background: #f9f0ff; color: #722ed1; }
      &.orange { background: #fff7e6; color: #fa8c16; }
    }
    
    .stat-info {
      margin-bottom: 12px;
      
      .stat-value {
        font-size: 28px;
        font-weight: bold;
        color: #1f2937;
        display: block;
      }
      
      .stat-label {
        font-size: 14px;
        color: #6b7280;
      }
    }
    
    .stat-bar {
      height: 6px;
      background: #f3f4f6;
      border-radius: 3px;
      overflow: hidden;
      
      .bar-fill {
        height: 100%;
        background: linear-gradient(90deg, #1890ff, #91caff);
        border-radius: 3px;
        transition: width 0.3s ease;
      }
    }
  }
  
  .monitor-content {
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
  
  .service-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    
    .service-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px;
      background: #f9fafb;
      border-radius: 8px;
      
      .service-status {
        width: 10px;
        height: 10px;
        border-radius: 50%;
        
        &.running { background: #52c41a; }
        &.stopped { background: #ff4d4f; }
        &.warning { background: #faad14; }
      }
      
      .service-info {
        flex: 1;
        
        .service-name {
          display: block;
          font-size: 14px;
          color: #1f2937;
        }
        
        .service-host {
          font-size: 12px;
          color: #6b7280;
        }
      }
      
      .service-metrics {
        font-size: 13px;
        color: #6b7280;
      }
    }
  }
  
  .db-status {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    
    .db-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px;
      background: #f9fafb;
      border-radius: 8px;
      
      .db-name {
        font-size: 14px;
        color: #1f2937;
      }
    }
  }
  
  .chart-container {
    height: 200px;
  }
  
  .alert-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    
    .alert-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px;
      border-radius: 8px;
      
      &.error {
        background: #fff2f0;
        color: #ff4d4f;
      }
      
      &.warning {
        background: #fffbe6;
        color: #faad14;
      }
      
      &.info {
        background: #f6ffed;
        color: #52c41a;
      }
      
      .alert-content {
        flex: 1;
        
        .alert-title {
          display: block;
          font-size: 14px;
        }
        
        .alert-time {
          font-size: 12px;
          opacity: 0.7;
        }
      }
    }
  }
}
</style>