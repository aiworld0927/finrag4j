<template>
  <div class="role-management">
    <div class="page-header">
      <div class="header-left">
        <h2>角色管理</h2>
        <p>管理系统角色和权限</p>
      </div>
      <div class="header-right">
        <el-button type="primary" icon="Plus" @click="showCreateModal = true">新增角色</el-button>
      </div>
    </div>
    
    <div class="filter-bar">
      <el-input v-model="filter.keyword" placeholder="搜索角色名称或编码" clearable style="width: 300px;">
        <template #prefix>
          <el-icon><component :is="icons.Search" /></el-icon>
        </template>
      </el-input>
    </div>
    
    <el-table :data="filteredRoles" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="角色名称" min-width="150" />
      <el-table-column prop="code" label="角色编码" min-width="150" />
      <el-table-column prop="description" label="描述" min-width="200" />
      <el-table-column prop="userCount" label="用户数" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button size="small" icon="Lock" @click="editPermissions(scope.row)">权限配置</el-button>
          <el-button size="small" icon="Edit" @click="editRole(scope.row)">编辑</el-button>
          <el-button size="small" icon="Delete" type="danger" @click="deleteRole(scope.row)">删除</el-button>
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
    <el-dialog :title="isEdit ? '编辑角色' : '新增角色'" v-model="showCreateModal" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="角色名称" required>
          <el-input v-model="form.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" required>
          <el-input v-model="form.code" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="请输入角色描述" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateModal = false">取消</el-button>
        <el-button type="primary" @click="saveRole">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 权限配置模态框 -->
    <el-dialog title="权限配置" v-model="showPermissionModal" width="600px">
      <div class="permission-tree">
        <el-tree
          :data="permissionTree"
          show-checkbox
          default-expand-all
          node-key="id"
          :checked-keys="checkedPermissions"
          @check-change="handleCheckChange"
        />
      </div>
      
      <template #footer>
        <el-button @click="showPermissionModal = false">取消</el-button>
        <el-button type="primary" @click="savePermissions">保存权限</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { icons } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const showCreateModal = ref(false)
const showPermissionModal = ref(false)
const isEdit = ref(false)

const filter = reactive({
  keyword: ''
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
  description: ''
})

const checkedPermissions = ref([])

const roles = ref([
  { id: 1, name: '管理员', code: 'admin', description: '系统管理员，拥有全部权限', userCount: 1, createTime: '2024-01-10 10:00:00' },
  { id: 2, name: '普通用户', code: 'user', description: '普通业务用户', userCount: 3, createTime: '2024-01-11 14:30:00' },
  { id: 3, name: '审计员', code: 'auditor', description: '审计人员，只读权限', userCount: 1, createTime: '2024-01-12 09:15:00' }
])

const permissionTree = ref([
  {
    id: 1,
    label: '系统管理',
    children: [
      { id: 11, label: '用户管理' },
      { id: 12, label: '角色管理' },
      { id: 13, label: '租户管理' },
      { id: 14, label: '系统配置' }
    ]
  },
  {
    id: 2,
    label: '知识库管理',
    children: [
      { id: 21, label: '知识库管理' },
      { id: 22, label: '文档管理' }
    ]
  },
  {
    id: 3,
    label: '问答中心',
    children: [
      { id: 31, label: 'RAG问答' },
      { id: 32, label: '对话历史' }
    ]
  },
  {
    id: 4,
    label: 'Agent中心',
    children: [
      { id: 41, label: '信贷材料抽取' },
      { id: 42, label: '合规自查' },
      { id: 43, label: '制度咨询' }
    ]
  },
  {
    id: 5,
    label: '运维监控',
    children: [
      { id: 51, label: '系统监控' },
      { id: 52, label: '审计日志' }
    ]
  }
])

const filteredRoles = computed(() => {
  let result = roles.value
  if (filter.keyword) {
    result = result.filter(r => 
      r.name.includes(filter.keyword) || r.code.includes(filter.keyword)
    )
  }
  pagination.total = result.length
  return result.slice((pagination.page - 1) * pagination.pageSize, pagination.page * pagination.pageSize)
})

const editRole = (role) => {
  isEdit.value = true
  Object.assign(form, role)
  showCreateModal.value = true
}

const deleteRole = (role) => {
  roles.value = roles.value.filter(r => r.id !== role.id)
  ElMessage.success('删除成功')
}

const saveRole = () => {
  showCreateModal.value = false
  isEdit.value = false
  Object.keys(form).forEach(key => form[key] = '')
  ElMessage.success(isEdit.value ? '修改成功' : '创建成功')
}

const editPermissions = (role) => {
  checkedPermissions.value = [1, 2, 3]
  showPermissionModal.value = true
}

const handleCheckChange = () => {
  // 权限变更处理
}

const savePermissions = () => {
  showPermissionModal.value = false
  ElMessage.success('权限配置成功')
}
</script>

<style lang="scss">
.role-management {
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
    margin-bottom: 20px;
  }
  
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  .permission-tree {
    max-height: 400px;
    overflow-y: auto;
  }
}
</style>