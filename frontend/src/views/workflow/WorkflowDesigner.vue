<template>
  <div class="workflow-designer">
    <div class="designer-header">
      <div class="header-left">
        <h2>工作流编排</h2>
        <el-input v-model="workflowName" placeholder="工作流名称" style="width: 200px; margin-left: 16px;" />
      </div>
      <div class="header-right">
        <el-button icon="VideoPlay" @click="testWorkflow">测试运行</el-button>
        <el-button type="primary" icon="Check" @click="saveWorkflow">保存</el-button>
      </div>
    </div>
    
    <div class="designer-main">
      <div class="node-palette">
        <h4>节点组件</h4>
        <div class="node-list">
          <div class="node-item" draggable="true" @dragstart="dragStart($event, 'start')">
            <el-icon color="#52c41a"><component :is="icons.CircleCheck" /></el-icon>
            <span>开始节点</span>
          </div>
          <div class="node-item" draggable="true" @dragstart="dragStart($event, 'end')">
            <el-icon color="#ff4d4f"><component :is="icons.CircleClose" /></el-icon>
            <span>结束节点</span>
          </div>
          <div class="node-item" draggable="true" @dragstart="dragStart($event, 'task')">
            <el-icon color="#1890ff"><component :is="icons.Setting" /></el-icon>
            <span>自动任务</span>
          </div>
          <div class="node-item" draggable="true" @dragstart="dragStart($event, 'approval')">
            <el-icon color="#722ed1"><component :is="icons.User" /></el-icon>
            <span>人工审批</span>
          </div>
          <div class="node-item" draggable="true" @dragstart="dragStart($event, 'review')">
            <el-icon color="#fa8c16"><component :is="icons.View" /></el-icon>
            <span>复核节点</span>
          </div>
          <div class="node-item" draggable="true" @dragstart="dragStart($event, 'branch')">
            <el-icon color="#13c2c2"><component :is="icons.Share" /></el-icon>
            <span>分支节点</span>
          </div>
        </div>
      </div>
      
      <div class="canvas-area" 
        @drop="drop" 
        @dragover.prevent
        @click="selectNode(null)"
      >
        <div class="canvas-grid"></div>
        <svg class="connections-layer">
          <defs>
            <marker id="arrowhead" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto">
              <polygon points="0 0, 10 3.5, 0 7" fill="#1890ff" />
            </marker>
          </defs>
          <line v-for="conn in connections" :key="conn.id"
            :x1="conn.x1" :y1="conn.y1"
            :x2="conn.x2" :y2="conn.y2"
            stroke="#1890ff" stroke-width="2" marker-end="url(#arrowhead)"
          />
        </svg>
        
        <div
          v-for="node in nodes"
          :key="node.id"
          class="flow-node"
          :class="{ selected: selectedNode === node.id }"
          :style="{ left: node.x + 'px', top: node.y + 'px' }"
          @mousedown="startDrag($event, node)"
          @click.stop="selectNode(node.id)"
        >
          <div class="node-header" :class="node.type">
            <el-icon><component :is="getNodeIcon(node.type)" /></el-icon>
            <span>{{ node.name }}</span>
          </div>
          <div class="node-body">
            <div class="port input" @click.stop="startConnection(node.id, 'input')"></div>
            <div class="port output" @click.stop="startConnection(node.id, 'output')"></div>
          </div>
        </div>
      </div>
      
      <div class="properties-panel" v-if="selectedNode">
        <h4>节点属性</h4>
        <el-form label-width="80px" size="small">
          <el-form-item label="节点名称">
            <el-input v-model="getNodeById(selectedNode).name" />
          </el-form-item>
          <el-form-item label="节点类型">
            <el-tag>{{ getNodeById(selectedNode).type }}</el-tag>
          </el-form-item>
          <el-form-item v-if="getNodeById(selectedNode).type === 'approval'" label="审批人">
            <el-select v-model="getNodeById(selectedNode).assignee" placeholder="选择审批人">
              <el-option label="张三" value="1" />
              <el-option label="李四" value="2" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="getNodeById(selectedNode).type === 'task'" label="任务类型">
            <el-select v-model="getNodeById(selectedNode).taskType" placeholder="选择任务类型">
              <el-option label="文档解析" value="parse" />
              <el-option label="向量化" value="vectorize" />
              <el-option label="合规检查" value="compliance" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { icons } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const workflowName = ref('新建工作流')
const selectedNode = ref(null)
const nodes = ref([
  { id: 1, type: 'start', name: '开始', x: 100, y: 200 },
  { id: 2, type: 'task', name: '文档解析', x: 300, y: 200, taskType: 'parse' },
  { id: 3, type: 'approval', name: '人工审批', x: 500, y: 200, assignee: '' },
  { id: 4, type: 'end', name: '结束', x: 700, y: 200 }
])

const connections = ref([
  { id: 1, x1: 140, y1: 220, x2: 300, y2: 220 },
  { id: 2, x1: 340, y1: 220, x2: 500, y2: 220 },
  { id: 3, x1: 540, y1: 220, x2: 700, y2: 220 }
])

const dragStart = (e, type) => {
  e.dataTransfer.setData('nodeType', type)
}

const drop = (e) => {
  const type = e.dataTransfer.getData('nodeType')
  if (type) {
    const rect = e.currentTarget.getBoundingClientRect()
    const node = {
      id: Date.now(),
      type,
      name: getNodeName(type),
      x: e.clientX - rect.left - 60,
      y: e.clientY - rect.top - 30
    }
    nodes.value.push(node)
  }
}

const getNodeName = (type) => {
  const names = {
    start: '开始',
    end: '结束',
    task: '自动任务',
    approval: '人工审批',
    review: '复核节点',
    branch: '分支节点'
  }
  return names[type] || '节点'
}

const getNodeIcon = (type) => {
  const iconMap = {
    start: icons.CircleCheck,
    end: icons.CircleClose,
    task: icons.Setting,
    approval: icons.User,
    review: icons.View,
    branch: icons.Share
  }
  return iconMap[type] || icons.Document
}

const selectNode = (id) => {
  selectedNode.value = id
}

const getNodeById = (id) => {
  return nodes.value.find(n => n.id === id) || {}
}

const startDrag = (e, node) => {
  // 节点拖拽逻辑
}

const startConnection = (nodeId, portType) => {
  // 连接线逻辑
}

const testWorkflow = () => {
  ElMessage.success('工作流测试运行中...')
}

const saveWorkflow = () => {
  ElMessage.success('工作流保存成功')
}
</script>

<style lang="scss">
.workflow-designer {
  height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
  
  .designer-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    background: #fff;
    border-bottom: 1px solid #e5e7eb;
    
    .header-left {
      display: flex;
      align-items: center;
      
      h2 {
        margin: 0;
        font-size: 18px;
        color: #1f2937;
      }
    }
  }
  
  .designer-main {
    flex: 1;
    display: flex;
    overflow: hidden;
  }
  
  .node-palette {
    width: 200px;
    background: #fff;
    border-right: 1px solid #e5e7eb;
    padding: 16px;
    
    h4 {
      margin: 0 0 12px 0;
      font-size: 14px;
      color: #374151;
    }
    
    .node-list {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    
    .node-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px;
      background: #f9fafb;
      border-radius: 8px;
      cursor: grab;
      
      &:hover {
        background: #f3f4f6;
      }
      
      span {
        font-size: 13px;
        color: #4b5563;
      }
    }
  }
  
  .canvas-area {
    flex: 1;
    position: relative;
    background: #f9fafb;
    overflow: hidden;
    
    .canvas-grid {
      position: absolute;
      inset: 0;
      background-image: 
        linear-gradient(#e5e7eb 1px, transparent 1px),
        linear-gradient(90deg, #e5e7eb 1px, transparent 1px);
      background-size: 20px 20px;
    }
    
    .connections-layer {
      position: absolute;
      inset: 0;
      width: 100%;
      height: 100%;
    }
    
    .flow-node {
      position: absolute;
      width: 120px;
      background: #fff;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      cursor: move;
      
      &.selected {
        box-shadow: 0 0 0 2px #1890ff;
      }
      
      .node-header {
        padding: 8px 12px;
        border-radius: 8px 8px 0 0;
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
        color: #fff;
        
        &.start { background: #52c41a; }
        &.end { background: #ff4d4f; }
        &.task { background: #1890ff; }
        &.approval { background: #722ed1; }
        &.review { background: #fa8c16; }
        &.branch { background: #13c2c2; }
      }
      
      .node-body {
        position: relative;
        height: 20px;
        
        .port {
          position: absolute;
          width: 12px;
          height: 12px;
          background: #1890ff;
          border-radius: 50%;
          top: 4px;
          cursor: crosshair;
          
          &.input { left: -6px; }
          &.output { right: -6px; }
        }
      }
    }
  }
  
  .properties-panel {
    width: 280px;
    background: #fff;
    border-left: 1px solid #e5e7eb;
    padding: 16px;
    
    h4 {
      margin: 0 0 16px 0;
      font-size: 14px;
      color: #374151;
    }
  }
}
</style>