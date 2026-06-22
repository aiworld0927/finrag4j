<template>
  <div class="tenant-management">
    <div class="page-header">
      <div class="header-left">
        <h2>租户管理</h2>
        <p>管理系统租户</p>
      </div>
      <div class="header-right">
        <el-button type="primary" icon="Plus" @click="showCreateModal = true">新增租户</el-button>
      </div>
    </div>
    
    <div class="filter-bar">
      <el-input v-model="filter.keyword" placeholder="搜索租户名称或编码" clearable style="width: 300px;">
        <template #prefix>
          <el-icon><component :is="icons.Search" /></el-icon>
        </template>
      </el-input>
      <el-select v-model="filter.status" placeholder="状态筛选" clearable style="margin-left: 16px;">
        <el-option label="全部" value="" />
        <el-option label="启用" value="active" />
        <el-option label="禁用" value="disabled" />
      </el-select>
    </div>
    
    <el-table :data="filteredTenants" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="租户名称" min-width="150" />
      <el-table-column prop="code" label="租户编码" min-width="150" />
      <el-table-column prop="description" label="描述" min-width="200" />
      <el-table-column prop="userCount" label="用户数" width="100" />
      <el-table-column prop="kbCount" label="知识库数" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">
            {{ scope.row.status === 'active' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="expireTime" label="有效期" width="180" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button size="small" icon="Edit" @click="editTenant(scope.row)">编辑</el-button>
          <el-button size="small" icon="Settings" @click="configureTenant(scope.row)">配置</el-button>
          <el-button size="small" icon="Delete" type="danger" @click="deleteTenant(scope.row)">删除</el-button>
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
    
    <!-- 创建/编辑模态框 -->
    <el-dialog :title="isEdit ? '编辑租户' : '新增租户'" v-model="showCreateModal" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="租户名称" required>
          <el-input v-model="form.name" placeholder="请输入租户名称" />
        </el-form-item>
        <el-form-item label="租户编码" required>
          <el-input v-model="form.code" placeholder="请输入租户编码" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="请输入租户描述" />
        </el-form-item>
        <el-form-item label="有效期">
          <el-date-picker v-model="form.expireTime" type="date" placeholder="选择有效期" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateModal = false">取消</el-button>
        <el-button type="primary" @click="saveTenant">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 租户配置模态框 -->
    <el-dialog title="租户配置" v-model="showConfigModal" width="500px">
      <el-form :model="configForm" label-width="100px">
        <el-form-item label="最大用户数">
          <el-input-number v-model="configForm.maxUsers" :min="1" :max="1000" />
        </el-form-item>
        <el-form-item label="最大知识库数">
          <el-input-number v-model="configForm.maxKBs" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="存储容量(GB)">
          <el-input-number v-model="configForm.storageLimit" :min="1" :max="1000" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showConfigModal = false">取消</el-button>
        <el-button type="primary" @click="saveConfig">保存配置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { icons } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const showCreateModal = ref(false)
const showConfigModal = ref(false)
const isEdit = ref(false)

const filter = reactive({
  keyword: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  id: '',
  name: '',
  code: '',
  description: '',
  expireTime: ''
})

const configForm = reactive({
  maxUsers: 10,
  maxKBs: 5,
  storageLimit: 100
})

const tenants = ref([
  { id: 1, name: '默认租户', code: 'default', description: '系统默认租户', userCount: 5, kbCount: 2, status: 'active', expireTime: '2025-12-31', createTime: '2024-01-10 10:00:00' },
  { id: 2, name: '测试租户', code: 'test', description: '测试用租户', userCount: 2, kbCount: 1, status: 'active', expireTime: '2024-12-31', createTime: '2024-01-11 14:30:00' }
])

const filteredTenants = computed(() => {
  let result = tenants.value
  if (filter.keyword) {
    result = result.filter(t => 
      t.name.includes(filter.keyword) || t.code.includes(filter.keyword)
    )
  }
  if (filter.status) {
    result = result.filter(t => t.status === filter.status)
  }
  pagination.total = result.length
  return result.slice((pagination.page - 1) * pagination.pageSize, pagination.page * pagination.pageSize)
})

const editTenant = (tenant) => {
  isEdit.value = true
  Object.assign(form, tenant)
  showCreateModal.value = true
}

const deleteTenant = (tenant) => {
  tenants.value = tenants.value.filter(t => t.id !== tenant.id)
  ElMessage.success('删除成功')
}

const configureTenant = (tenant) => {
  showConfigModal.value = true
}

const saveTenant = () => {
  showCreateModal.value = false
  isEdit.value = false
  Object.keys(form).forEach(key => form[key] = '')
  ElMessage.success(isEdit.value ? '修改成功' : '创建成功')
}

const saveConfig = () => {
  showConfigModal.value = false
  ElMessage.success('配置保存成功')
}
</script>

<style lang="scss">
.tenant-management {
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