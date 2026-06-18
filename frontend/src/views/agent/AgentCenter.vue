<template>
  <div class="agent-center">
    <div class="page-header">
      <h2>Agent中心</h2>
      <p>金融垂直领域智能Agent业务</p>
    </div>
    
    <div class="agent-grid">
      <!-- 信贷材料抽取Agent -->
      <div class="agent-card">
        <div class="agent-icon blue">
          <el-icon :size="32"><component :is="icons.Document" /></el-icon>
        </div>
        <h3>信贷材料抽取</h3>
        <p>自动从信贷合同中抽取关键字段，支持自定义模板和批量处理</p>
        <div class="agent-stats">
          <span>已处理: 128 份</span>
          <span>准确率: 95.2%</span>
        </div>
        <el-button type="primary" @click="openExtractAgent">开始使用</el-button>
      </div>
      
      <!-- 监管合规自查Agent -->
      <div class="agent-card">
        <div class="agent-icon green">
          <el-icon :size="32"><component :is="icons.ShieldCheck" /></el-icon>
        </div>
        <h3>监管合规自查</h3>
        <p>匹配监管知识库，自动识别合规风险并生成整改报告</p>
        <div class="agent-stats">
          <span>已检查: 56 次</span>
          <span>发现问题: 23 个</span>
        </div>
        <el-button type="primary" @click="openComplianceAgent">开始使用</el-button>
      </div>
      
      <!-- 制度咨询Agent -->
      <div class="agent-card">
        <div class="agent-icon purple">
          <el-icon :size="32"><component :is="icons.ChatLineSquare" /></el-icon>
        </div>
        <h3>制度咨询</h3>
        <p>智能检索制度条款，提供精准的制度解读和合规建议</p>
        <div class="agent-stats">
          <span>咨询次数: 342 次</span>
          <span>满意度: 98.5%</span>
        </div>
        <el-button type="primary" @click="openRegulationAgent">开始使用</el-button>
      </div>
      
      <!-- 客户经理助手 -->
      <div class="agent-card">
        <div class="agent-icon orange">
          <el-icon :size="32"><component :is="icons.UserFilled" /></el-icon>
        </div>
        <h3>客户经理助手</h3>
        <p>生成营销方案，分析客户画像，推荐合适产品</p>
        <div class="agent-stats">
          <span>方案生成: 89 个</span>
          <span>采纳率: 87.6%</span>
        </div>
        <el-button type="primary" @click="openMarketingAgent">开始使用</el-button>
      </div>
    </div>
    
    <!-- 信贷抽取模态框 -->
    <el-dialog title="信贷材料抽取" v-model="showExtractModal" width="800px">
      <div class="extract-form">
        <el-form :model="extractForm" label-width="120px">
          <el-form-item label="选择模板">
            <el-select v-model="extractForm.templateId" placeholder="请选择抽取模板">
              <el-option label="信贷合同模板" value="1" />
              <el-option label="抵押合同模板" value="2" />
              <el-option label="担保合同模板" value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="上传文档">
            <el-upload drag multiple :auto-upload="false">
              <el-icon class="el-icon--upload"><component :is="icons.UploadFilled" /></el-icon>
              <div class="el-upload__text">拖拽文件到此处或点击上传</div>
            </el-upload>
          </el-form-item>
        </el-form>
        
        <div class="extract-result" v-if="extractResult">
          <h4>抽取结果</h4>
          <el-table :data="extractResult" border>
            <el-table-column prop="field" label="字段名" />
            <el-table-column prop="value" label="抽取值" />
            <el-table-column prop="confidence" label="置信度">
              <template #default="scope">
                {{ (scope.row.confidence * 100).toFixed(1) }}%
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showExtractModal = false">取消</el-button>
        <el-button type="primary" @click="executeExtract">执行抽取</el-button>
      </template>
    </el-dialog>
    
    <!-- 合规自查模态框 -->
    <el-dialog title="监管合规自查" v-model="showComplianceModal" width="900px">
      <div class="compliance-form">
        <el-form :model="complianceForm" label-width="120px">
          <el-form-item label="报告名称">
            <el-input v-model="complianceForm.reportName" placeholder="请输入报告名称" />
          </el-form-item>
          <el-form-item label="选择文档">
            <el-select v-model="complianceForm.documentIds" multiple placeholder="请选择要检查的文档">
              <el-option label="信贷合同A.pdf" value="1" />
              <el-option label="业务流程文档.docx" value="2" />
              <el-option label="风险管理制度.pdf" value="3" />
            </el-select>
          </el-form-item>
        </el-form>
        
        <div class="compliance-result" v-if="complianceResult">
          <h4>检查结果</h4>
          <div class="risk-summary">
            <el-tag type="danger">高风险: {{ complianceResult.highRisk }}</el-tag>
            <el-tag type="warning">中风险: {{ complianceResult.mediumRisk }}</el-tag>
            <el-tag type="info">低风险: {{ complianceResult.lowRisk }}</el-tag>
          </div>
          <el-table :data="complianceResult.findings" border>
            <el-table-column prop="findingNo" label="问题编号" width="120" />
            <el-table-column prop="description" label="问题描述" />
            <el-table-column prop="riskLevel" label="风险等级" width="100">
              <template #default="scope">
                <el-tag :type="getRiskType(scope.row.riskLevel)">{{ scope.row.riskLevel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="suggestion" label="整改建议" />
          </el-table>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showComplianceModal = false">取消</el-button>
        <el-button type="primary" @click="executeCompliance">执行检查</el-button>
        <el-button type="success" @click="exportReport">导出报告</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { icons } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const showExtractModal = ref(false)
const showComplianceModal = ref(false)

const extractForm = reactive({
  templateId: '',
  documentIds: []
})

const complianceForm = reactive({
  reportName: '',
  documentIds: []
})

const extractResult = ref(null)
const complianceResult = ref(null)

const openExtractAgent = () => {
  showExtractModal.value = true
  extractResult.value = null
}

const openComplianceAgent = () => {
  showComplianceModal.value = true
  complianceResult.value = null
}

const openRegulationAgent = () => {
  ElMessage.info('跳转到制度咨询页面')
}

const openMarketingAgent = () => {
  ElMessage.info('跳转到客户经理助手页面')
}

const executeExtract = () => {
  extractResult.value = [
    { field: '借款人名称', value: '张三', confidence: 0.98 },
    { field: '借款金额', value: '100万元', confidence: 0.95 },
    { field: '借款期限', value: '36个月', confidence: 0.92 },
    { field: '利率', value: '年化4.5%', confidence: 0.89 },
    { field: '还款方式', value: '等额本息', confidence: 0.96 }
  ]
  ElMessage.success('抽取完成')
}

const executeCompliance = () => {
  complianceResult.value = {
    highRisk: 2,
    mediumRisk: 5,
    lowRisk: 8,
    findings: [
      { findingNo: 'F-001', description: '合同条款未明确约定违约责任', riskLevel: 'high', suggestion: '补充违约责任条款' },
      { findingNo: 'F-002', description: '利率约定超出监管上限', riskLevel: 'high', suggestion: '调整利率至合规范围' },
      { findingNo: 'F-003', description: '缺少必要的风险提示', riskLevel: 'medium', suggestion: '添加风险提示条款' }
    ]
  }
  ElMessage.success('检查完成')
}

const exportReport = () => {
  ElMessage.success('报告已导出')
}

const getRiskType = (level) => {
  const types = { high: 'danger', medium: 'warning', low: 'info' }
  return types[level] || 'info'
}
</script>

<style lang="scss">
.agent-center {
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
  
  .agent-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
  }
  
  .agent-card {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    text-align: center;
    
    .agent-icon {
      width: 64px;
      height: 64px;
      border-radius: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 16px;
      
      &.blue { background: #e6f7ff; color: #1890ff; }
      &.green { background: #f6ffed; color: #52c41a; }
      &.purple { background: #f9f0ff; color: #722ed1; }
      &.orange { background: #fff7e6; color: #fa8c16; }
    }
    
    h3 {
      font-size: 18px;
      color: #1f2937;
      margin: 0 0 8px 0;
    }
    
    p {
      font-size: 14px;
      color: #6b7280;
      margin: 0 0 16px 0;
      line-height: 1.5;
    }
    
    .agent-stats {
      display: flex;
      justify-content: center;
      gap: 24px;
      margin-bottom: 16px;
      
      span {
        font-size: 13px;
        color: #9ca3af;
      }
    }
  }
  
  .extract-result, .compliance-result {
    margin-top: 20px;
    
    h4 {
      margin: 0 0 12px 0;
      font-size: 14px;
      color: #374151;
    }
  }
  
  .risk-summary {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;
  }
}
</style>