# FinRag4j API接口文档

## 文档概述

本文档汇总FinRag4j全部REST API接口，供前端对接和第三方集成使用。

**接口基础地址**: `http://localhost:8080/api`  
**认证方式**: Bearer Token  
**文档版本**: v1.0.0

---

## 1. 认证说明

### 1.1 获取Token

```http
POST /api/users/login
Content-Type: application/x-www-form-urlencoded

username=admin&password=admin123&tenantId=1
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "nickname": "管理员"
    }
  }
}
```

### 1.2 使用Token

```http
GET /api/kb?tenantId=1
Authorization: Bearer <token>
```

---

## 2. 用户管理 API

### 2.1 获取用户列表

```http
GET /api/users?tenantId=1
Authorization: Bearer <token>
```

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "username": "admin",
      "nickname": "管理员",
      "email": "wangjn1130@163.com",
      "status": "active"
    }
  ]
}
```

### 2.2 创建用户

```http
POST /api/users?tenantId=1
Content-Type: application/json
Authorization: Bearer <token>

{
  "username": "zhangsan",
  "password": "password123",
  "nickname": "张三",
  "email": "wangjn1130@163.com",
  "phone": "13800138000"
}
```

### 2.3 更新用户

```http
PUT /api/users/{id}
Content-Type: application/json
Authorization: Bearer <token>

{
  "nickname": "新昵称",
  "email": "wangjn1130@163.com"
}
```

### 2.4 删除用户

```http
DELETE /api/users/{id}
Authorization: Bearer <token>
```

### 2.5 获取用户角色

```http
GET /api/users/{userId}/roles?tenantId=1
Authorization: Bearer <token>
```

### 2.6 分配角色

```http
POST /api/users/{userId}/roles?tenantId=1
Content-Type: application/json
Authorization: Bearer <token>

[1, 2, 3]
```

---

## 3. 角色权限 API

### 3.1 获取角色列表

```http
GET /api/roles?tenantId=1
Authorization: Bearer <token>
```

### 3.2 创建角色

```http
POST /api/roles?tenantId=1
Content-Type: application/json
Authorization: Bearer <token>

{
  "roleName": "业务管理员",
  "roleCode": "business_admin",
  "description": "业务管理权限"
}
```

### 3.3 分配权限

```http
POST /api/roles/{id}/permissions?tenantId=1
Content-Type: application/json
Authorization: Bearer <token>

[1, 2, 3, 4, 5]
```

---

## 4. 知识库管理 API

### 4.1 创建知识库

```http
POST /api/kb?tenantId=1
Content-Type: application/json
Authorization: Bearer <token>

{
  "kbName": "金融法规知识库",
  "kbCode": "finance_regulation",
  "description": "收录各类金融监管法规",
  "defaultModel": "qwen2:7b",
  "similarityThreshold": 0.7,
  "topK": 10
}
```

### 4.2 获取知识库列表

```http
GET /api/kb?tenantId=1
Authorization: Bearer <token>
```

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "kbName": "金融法规知识库",
      "kbCode": "finance_regulation",
      "description": "收录各类金融监管法规",
      "status": "active",
      "documentCount": 128
    }
  ]
}
```

### 4.3 更新知识库

```http
PUT /api/kb/{id}
Content-Type: application/json
Authorization: Bearer <token>

{
  "kbName": "更新后的名称",
  "similarityThreshold": 0.75
}
```

### 4.4 删除知识库

```http
DELETE /api/kb/{id}?tenantId=1
Authorization: Bearer <token>
```

---

## 5. 文档管理 API

### 5.1 上传文档

```http
POST /api/documents/upload?tenantId=1
Content-Type: multipart/form-data
Authorization: Bearer <token>

file: <文件>
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "fileName": "test.pdf",
    "fileType": "PDF",
    "status": "processing"
  }
}
```

### 5.2 获取文档列表

```http
GET /api/documents?tenantId=1&status=indexed
Authorization: Bearer <token>
```

### 5.3 预览文档

```http
GET /api/documents/{id}/preview?tenantId=1
Authorization: Bearer <token>
```

### 5.4 删除文档

```http
DELETE /api/documents/{id}?tenantId=1
Authorization: Bearer <token>
```

---

## 6. RAG问答 API

### 6.1 开始新对话

```http
POST /api/chat/new
Content-Type: application/x-www-form-urlencoded
Authorization: Bearer <token>

question=什么是信贷业务？&kbId=1&tenantId=1&userId=1
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "sessionId": "abc123",
    "answer": "信贷业务是指...",
    "references": [
      {
        "fileName": "信贷管理办法.pdf",
        "pageNum": 12,
        "similarity": 0.85
      }
    ],
    "similarity": 0.85,
    "responseTime": 1250,
    "modelName": "qwen2:7b"
  }
}
```

### 6.2 继续对话

```http
POST /api/chat/continue
Content-Type: application/x-www-form-urlencoded
Authorization: Bearer <token>

sessionId=abc123&question=有哪些合规要求？&tenantId=1&userId=1
```

### 6.3 获取对话历史

```http
GET /api/chat/history/{sessionId}
Authorization: Bearer <token>
```

### 6.4 获取最近对话

```http
GET /api/chat/recent?tenantId=1&userId=1&limit=10
Authorization: Bearer <token>
```

### 6.5 添加收藏

```http
POST /api/chat/favorite
Content-Type: application/x-www-form-urlencoded
Authorization: Bearer <token>

sessionId=abc123&chatId=1&userMessage=问题&aiMessage=回答&tenantId=1&userId=1
```

### 6.6 获取收藏列表

```http
GET /api/chat/favorites?tenantId=1&userId=1
Authorization: Bearer <token>
```

---

## 7. 金融Agent API

### 7.1 获取抽取模板

```http
GET /api/agent/extract/templates?tenantId=1
Authorization: Bearer <token>
```

### 7.2 创建抽取模板

```http
POST /api/agent/extract/template?tenantId=1
Content-Type: application/json
Authorization: Bearer <token>

{
  "templateName": "信贷合同模板",
  "fields": [
    {"name": "借款人名称", "type": "string"},
    {"name": "借款金额", "type": "string"},
    {"name": "借款期限", "type": "string"}
  ]
}
```

### 7.3 执行信贷抽取

```http
POST /api/agent/extract/execute?documentId=1&templateId=1&tenantId=1
Authorization: Bearer <token>
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "recordId": 1,
    "extractedFields": [
      {"field": "借款人名称", "value": "张三", "confidence": 0.98},
      {"field": "借款金额", "value": "100万元", "confidence": 0.95}
    ]
  }
}
```

### 7.4 人工复核抽取结果

```http
POST /api/agent/extract/{recordId}/review
Content-Type: application/x-www-form-urlencoded
Authorization: Bearer <token>

correctedResult={"借款人名称":"张三"}&comment=确认无误&reviewerId=1
```

### 7.5 创建合规自查报告

```http
POST /api/agent/compliance/report?tenantId=1&reportName=合规自查报告
Content-Type: application/json
Authorization: Bearer <token>

[1, 2, 3]
```

### 7.6 执行合规检查

```http
POST /api/agent/compliance/check?reportId=1&tenantId=1
Content-Type: application/json
Authorization: Bearer <token>

[1, 2, 3]
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "reportId": 1,
    "highRisk": 2,
    "mediumRisk": 5,
    "lowRisk": 8,
    "findings": [
      {
        "findingNo": "F-001",
        "description": "合同条款未明确约定违约责任",
        "riskLevel": "high",
        "suggestion": "补充违约责任条款"
      }
    ]
  }
}
```

### 7.7 复核合规报告

```http
POST /api/agent/compliance/report/{reportId}/review
Content-Type: application/x-www-form-urlencoded
Authorization: Bearer <token>

comment=已确认整改方案&reviewerId=1
```

### 7.8 制度咨询

```http
POST /api/agent/regulation/query?question=信贷业务有哪些合规要求？&tenantId=1
Authorization: Bearer <token>
```

---

## 8. 工作流 API

### 8.1 创建工作流

```http
POST /api/workflow/definitions?tenantId=1
Content-Type: application/json
Authorization: Bearer <token>

{
  "workflowName": "文档审批流程",
  "workflowCode": "doc_approval",
  "workflowJson": "{...}",
  "triggerType": "manual"
}
```

### 8.2 获取工作流列表

```http
GET /api/workflow/definitions?tenantId=1
Authorization: Bearer <token>
```

### 8.3 触发工作流

```http
POST /api/workflow/trigger?workflowCode=doc_approval&tenantId=1
Authorization: Bearer <token>
```

### 8.4 获取待办任务

```http
GET /api/workflow/tasks/pending?assigneeId=1
Authorization: Bearer <token>
```

### 8.5 完成任务

```http
POST /api/workflow/tasks/{taskId}/complete
Content-Type: application/x-www-form-urlencoded
Authorization: Bearer <token>

comment=同意&approved=true
```

### 8.6 获取执行日志

```http
GET /api/workflow/instances/{id}/logs
Authorization: Bearer <token>
```

---

## 9. 审计日志 API

### 9.1 按时间范围查询

```http
GET /api/audit/time-range?tenantId=1&startTime=2024-01-01 00:00:00&endTime=2024-01-31 23:59:59
Authorization: Bearer <token>
```

### 9.2 按用户查询

```http
GET /api/audit/user/{userId}
Authorization: Bearer <token>
```

### 9.3 导出Excel

```http
GET /api/audit/export/excel?tenantId=1&startTime=2024-01-01 00:00:00&endTime=2024-01-31 23:59:59
Authorization: Bearer <token>
```

---

## 10. 系统配置 API

### 10.1 获取系统配置

```http
GET /api/config?tenantId=1
Authorization: Bearer <token>
```

### 10.2 更新系统配置

```http
PUT /api/config?tenantId=1
Content-Type: application/json
Authorization: Bearer <token>

{
  "similarityThreshold": 0.7,
  "chunkSize": 500,
  "maxConcurrentRequests": 100
}
```

---

## 11. 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（Token无效或过期） |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

**文档版本**: v1.0.0  
**更新日期**: 2024年  
**前端API文件**: `frontend/src/api/`