<template>
  <div class="user-management">
    <div class="page-header">
      <div class="header-left">
        <h2>用户管理</h2>
        <p>管理系统用户账号</p>
      </div>
      <div class="header-right">
        <el-button type="primary" icon="Plus" @click="showCreateModal = true">新增用户</el-button>
        <el-button icon="Delete" type="danger" @click="batchDelete">批量删除</el-button>
      </div>
    </div>
    
    <div class="filter-bar">
      <el-input v-model="filter.keyword" placeholder="搜索用户名或邮箱" clearable style="width: 300px;">
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
    
    <el-table :data="filteredUsers" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="nickname" label="昵称" min-width="120" />
      <el-table-column prop="email" label="邮箱" min-width="200" />
      <el-table-column prop="phone" label="手机号" width="120" />
      <el-table-column prop="roleName" label="角色" width="120" />
      <el-table-column prop="tenantName" label="所属租户" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-switch :value="scope.row.status === 'active'" @change="toggleStatus(scope.row)" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button size="small" icon="Edit" @click="editUser(scope.row)">编辑</el-button>
          <el-button size="small" icon="Lock" @click="resetPassword(scope.row)">重置密码</el-button>
          <el-button size="small" icon="Delete" type="danger" @click="deleteUser(scope.row)">删除</el-button>
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
    <el-dialog :title="isEdit ? '编辑用户' : '新增用户'" v-model="showCreateModal" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱" required>
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" required>
          <el-select v-model="form.roleId" placeholder="请选择角色">
            <el-option label="管理员" :value="1" />
            <el-option label="普通用户" :value="2" />
            <el-option label="审计员" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属租户" required>
          <el-select v-model="form.tenantId" placeholder="请选择租户">
            <el-option label="默认租户" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" required>
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateModal = false">取消</el-button>
        <el-button type="primary" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { icons } from '@/utils/icons'
import { ElMessage } from 'element-plus'

const showCreateModal = ref(false)
const isEdit = ref(false)
const selectedIds = ref([])

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
  username: '',
  nickname: '',
  email: '',
  phone: '',
  roleId: '',
  tenantId: '',
  password: ''
})

const users = ref([
  { id: 1, username: 'admin', nickname: '系统管理员', email: 'admin@finrag4j.com', phone: '13800138000', roleName: '管理员', tenantName: '默认租户', status: 'active', createTime: '2024-01-10 10:00:00' },
  { id: 2, username: 'user01', nickname: '张三', email: 'user01@finrag4j.com', phone: '13800138001', roleName: '普通用户', tenantName: '默认租户', status: 'active', createTime: '2024-01-11 14:30:00' },
  { id: 3, username: 'audit01', nickname: '李四', email: 'audit01@finrag4j.com', phone: '13800138002', roleName: '审计员', tenantName: '默认租户', status: 'active', createTime: '2024-01-12 09:15:00' },
  { id: 4, username: 'user02', nickname: '王五', email: 'user02@finrag4j.com', phone: '13800138003', roleName: '普通用户', tenantName: '默认租户', status: 'disabled', createTime: '2024-01-13 16:45:00' },
  { id: 5, username: 'user03', nickname: '赵六', email: 'user03@finrag4j.com', phone: '13800138004', roleName: '普通用户', tenantName: '默认租户', status: 'active', createTime: '2024-01-14 11:20:00' }
])

const filteredUsers = computed(() => {
  let result = users.value
  if (filter.keyword) {
    result = result.filter(u => 
      u.username.includes(filter.keyword) || u.email.includes(filter.keyword) || u.nickname.includes(filter.keyword)
    )
  }
  if (filter.status) {
    result = result.filter(u => u.status === filter.status)
  }
  pagination.total = result.length
  return result.slice((pagination.page - 1) * pagination.pageSize, pagination.page * pagination.pageSize)
})

const handleSelectionChange = (val) => {
  selectedIds.value = val.map(item => item.id)
}

const editUser = (user) => {
  isEdit.value = true
  Object.assign(form, user)
  showCreateModal.value = true
}

const deleteUser = (user) => {
  users.value = users.value.filter(u => u.id !== user.id)
  ElMessage.success('删除成功')
}

const batchDelete = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的用户')
    return
  }
  users.value = users.value.filter(u => !selectedIds.value.includes(u.id))
  selectedIds.value = []
  ElMessage.success(`成功删除 ${selectedIds.value.length} 个用户`)
}

const toggleStatus = (user) => {
  user.status = user.status === 'active' ? 'disabled' : 'active'
  ElMessage.success(`用户${user.status === 'active' ? '已启用' : '已禁用'}`)
}

const resetPassword = (user) => {
  ElMessage.success(`已重置用户 ${user.username} 的密码`)
}

const saveUser = () => {
  showCreateModal.value = false
  isEdit.value = false
  Object.keys(form).forEach(key => form[key] = '')
  ElMessage.success(isEdit.value ? '修改成功' : '创建成功')
}
</script>

<style lang="scss">
.user-management {
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