<template>
  <div class="audit-log">
    <div class="page-header">
      <div class="header-left">
        <h2>审计日志</h2>
        <p>查看系统操作日志</p>
      </div>
      <div class="header-right">
        <el-button icon="Download" @click="exportLog">导出日志</el-button>
      </div>
    </div>
    
    <div class="filter-bar">
      <el-input v-model="filter.keyword" placeholder="搜索用户名或操作" clearable style="width: 300px;">
        <template #prefix>
          <el-icon><component :is="icons.Search" /></el-icon>
        </template>
      </el-input>
      <el-select v-model="filter.actionType" placeholder="操作类型" clearable style="margin-left: 16px;">
        <el-option label="全部" value="" />
        <el-option label="登录" value="login" />
        <el-option label="创建" value="create" />
        <el-option label="更新" value="update" />
        <el-option label="删除" value="delete" />
      </el-select>
      <el-date-picker v-model="filter.dateRange" type="daterange" placeholder="选择日期范围" style="margin-left: 16px;" />
    </div>
    
    <el-table :data="filteredLogs" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="action" label="操作" width="150">
        <template #default="scope">
          <el-tag :type="getActionType(scope.row.actionType)">{{ scope.row.action }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="module" label="模块" width="120" />
      <el-table-column prop="target" label="操作对象" min-width="200" />
      <el-table-column prop="ip" label="IP地址" width="150" />
      <el-table-column prop="userAgent" label="客户端" width="200" />
      <el-table-column prop="result" label="结果" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.result === 'success' ? 'success' : 'danger'">
            {{ scope.row.result === 'success' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="180" />
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
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { icons } from '@/utils/icons'
import { ElMessage } from 'element-plus'

const filter = reactive({
  keyword: '',
  actionType: '',
  dateRange: []
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const logs = ref([
  { id: 1, username: 'admin', action: '用户登录', actionType: 'login', module: '系统管理', target: '用户登录', ip: '192.168.1.100', userAgent: 'Chrome/120.0.0.0', result: 'success', createTime: '2024-01-15 10:30:00' },
  { id: 2, username: 'admin', action: '创建用户', actionType: 'create', module: '用户管理', target: '用户: user04', ip: '192.168.1.100', userAgent: 'Chrome/120.0.0.0', result: 'success', createTime: '2024-01-15 10:35:00' },
  { id: 3, username: 'user01', action: '上传文档', actionType: 'create', module: '文档管理', target: '文档: 合同模板.pdf', ip: '192.168.1.101', userAgent: 'Firefox/121.0', result: 'success', createTime: '2024-01-15 11:20:00' },
  { id: 4, username: 'user02', action: '开始问答', actionType: 'create', module: 'RAG问答', target: '对话: 信贷合规问题', ip: '192.168.1.102', userAgent: 'Chrome/120.0.0.0', result: 'success', createTime: '2024-01-15 14:15:00' },
  { id: 5, username: 'admin', action: '删除文档', actionType: 'delete', module: '文档管理', target: '文档: 旧版手册.pdf', ip: '192.168.1.100', userAgent: 'Chrome/120.0.0.0', result: 'success', createTime: '2024-01-15 15:30:00' },
  { id: 6, username: 'audit01', action: '查询日志', actionType: 'read', module: '审计日志', target: '日志查询', ip: '192.168.1.103', userAgent: 'Edge/120.0.0.0', result: 'success', createTime: '2024-01-15 16:45:00' }
])

const filteredLogs = computed(() => {
  let result = logs.value
  if (filter.keyword) {
    result = result.filter(l => 
      l.username.includes(filter.keyword) || l.action.includes(filter.keyword) || l.target.includes(filter.keyword)
    )
  }
  if (filter.actionType) {
    result = result.filter(l => l.actionType === filter.actionType)
  }
  pagination.total = result.length
  return result.slice((pagination.page - 1) * pagination.pageSize, pagination.page * pagination.pageSize)
})

const getActionType = (type) => {
  const types = {
    login: 'info',
    create: 'success',
    update: 'warning',
    delete: 'danger',
    read: 'info'
  }
  return types[type] || 'info'
}

const exportLog = () => {
  ElMessage.success('日志导出成功')
}
</script>

<style lang="scss">
.audit-log {
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
}
</style>