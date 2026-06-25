# FinRag4j API接口文档

## 文档概述

本文档汇总FinRag4j全部REST API接口，供前端对接和第三方集成使用。

**接口基础地址**: `http://localhost:8080`  
**网关路径前缀**: `/api`  
**认证方式**: Bearer Token  
**文档版本**: v1.0.0

---

## 1. 认证说明

### 1.1 获取Token

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password123"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "..."
  }
}
```

### 1.2 使用Token

```http
GET /api/users
Authorization: Bearer <token>
```

---

## 2. 认证管理 API (finrag4j-auth)

### 2.1 用户认证

| API路径 | HTTP方法 | 功能描述 |
|---------|----------|----------|
| `/api/auth/login` | POST | 用户登录，返回JWT Token |
| `/api/auth/register` | POST | 注册新用户 |
| `/api/auth/logout` | POST | 用户登出，注销会话 |
| `/api/auth/refresh` | POST | 使用Refresh Token刷新Access Token |
| `/api/auth/me` | GET | 获取当前用户信息 |

**登录请求示例**:
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "zhangsan",
  "password": "password123"
}
```

**注册请求示例**:
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "zhangsan",
  "password": "password123",
  "email": "zhangsan@example.com",
  "phone": "13800138000"
}
```

### 2.2 用户管理

| API路径 | HTTP方法 | 功能描述 |
|---------|----------|----------|
| `/api/users` | GET | 分页查询用户列表 |
| `/api/users/{id}` | GET | 获取用户详情 |
| `/api/users` | POST | 创建用户 |
| `/api/users/{id}` | PUT | 更新用户信息 |
| `/api/users/{id}` | DELETE | 删除用户 |
| `/api/users/{id}/status` | PUT | 更新用户状态（启用/禁用） |
| `/api/users/{id}/roles` | POST | 为用户分配角色 |

**分页查询示例**:
```http
GET /api/users?pageNum=1&pageSize=10&username=张三&status=active
Authorization: Bearer <token>
```

### 2.3 角色管理

| API路径 | HTTP方法 | 功能描述 |
|---------|----------|----------|
| `/api/roles` | GET | 查询所有角色 |
| `/api/roles/{id}` | GET | 获取角色详情 |
| `/api/roles` | POST | 创建角色 |
| `/api/roles/{id}` | PUT | 更新角色 |
| `/api/roles/{id}` | DELETE | 删除角色 |
| `/api/roles/{id}/permissions` | POST | 为角色分配权限 |

**创建角色示例**:
```http
POST /api/roles
Content-Type: application/json
Authorization: Bearer <token>

{
  "roleName": "业务管理员",
  "roleCode": "business_admin",
  "description": "业务管理权限"
}
```

### 2.4 权限管理

| API路径 | HTTP方法 | 功能描述 |
|---------|----------|----------|
| `/api/permissions/tree` | GET | 获取权限菜单树 |
| `/api/permissions` | GET | 查询所有权限 |
| `/api/permissions/{id}` | GET | 获取权限详情 |
| `/api/permissions` | POST | 创建权限 |
| `/api/permissions/{id}` | PUT | 更新权限 |
| `/api/permissions/{id}` | DELETE | 删除权限 |

---

## 3. 文档管理 API (finrag4j-document)

### 3.1 文档管理

| API路径 | HTTP方法 | 功能描述 |
|---------|----------|----------|
| `/api/document/upload` | POST | 上传文档到MinIO |
| `/api/document/{id}` | GET | 获取文档详情 |
| `/api/document` | GET | 分页查询文档 |
| `/api/document/{id}` | DELETE | 将文档放入回收站 |
| `/api/document/{id}/recover` | POST | 从回收站恢复文档 |
| `/api/document/{id}/permanent` | DELETE | 永久删除文档 |
| `/api/document/{id}/versions` | GET | 获取文档版本历史 |
| `/api/document/{id}/versions/{versionId}/restore` | POST | 恢复文档到指定版本 |
| `/api/document/status/{taskId}` | GET | 查询文档处理状态 |

**上传文档示例**:
```http
POST /api/document/upload
Content-Type: multipart/form-data
Authorization: Bearer <token>

file: <文件>
kbId: 1
tags: ["信贷", "合同"]
```

**分页查询示例**:
```http
GET /api/document?pageNum=1&pageSize=10&kbId=1&status=processed
Authorization: Bearer <token>
```

### 3.2 知识库管理

| API路径 | HTTP方法 | 功能描述 |
|---------|----------|----------|
| `/api/knowledge-base` | GET | 查询所有知识库 |
| `/api/knowledge-base/{id}` | GET | 获取知识库详情 |
| `/api/knowledge-base` | POST | 创建知识库 |
| `/api/knowledge-base/{id}` | PUT | 更新知识库 |
| `/api/knowledge-base/{id}` | DELETE | 删除知识库 |
| `/api/knowledge-base/{id}/documents/{docId}` | POST | 绑定文档到知识库 |
| `/api/knowledge-base/{id}/documents/{docId}` | DELETE | 从知识库解绑文档 |

**创建知识库示例**:
```http
POST /api/knowledge-base
Content-Type: application/json
Authorization: Bearer <token>

{
  "kbName": "金融法规知识库",
  "kbCode": "finance_regulation",
  "description": "收录各类金融监管法规"
}
```

---

## 4. 搜索检索 API (finrag4j-search)

### 4.1 向量管理

| API路径 | HTTP方法 | 功能描述 |
|---------|----------|----------|
| `/api/vector/chunk` | POST | 添加向量片段 |
| `/api/vector/chunk/batch` | POST | 批量添加向量片段 |
| `/api/vector/chunk/{id}` | DELETE | 删除向量片段 |
| `/api/vector/document/{docId}` | DELETE | 删除文档的所有向量 |
| `/api/vector/rebuild-index` | POST | 重建向量索引 |

### 4.2 RAG检索

| API路径 | HTTP方法 | 功能描述 |
|---------|----------|----------|
| `/api/rag/retrieve` | POST | 混合检索（向量+关键词） |
| `/api/rag/search` | POST | 纯语义向量搜索 |
| `/api/rag/keyword-search` | POST | 基于BM25的关键词搜索 |

**混合检索示例**:
```http
POST /api/rag/retrieve
Content-Type: application/json
Authorization: Bearer <token>

{
  "query": "信贷业务合规要求",
  "kbId": 1,
  "topK": 10,
  "similarityThreshold": 0.7,
  "enableRerank": true
}
```

---

## 5. 智能代理 API (finrag4j-agent)

### 5.1 RAG聊天

| API路径 | HTTP方法 | 功能描述 |
|---------|----------|----------|
| `/api/chat/send` | POST | 发送消息获取AI回复 |
| `/api/chat/history/{sessionId}` | GET | 获取聊天历史 |
| `/api/chat/session/create` | POST | 创建会话 |
| `/api/chat/session/{sessionId}` | DELETE | 删除会话 |
| `/api/chat/session/{sessionId}/favorite/{messageId}` | POST | 收藏消息 |

**发送消息示例**:
```http
POST /api/chat/send
Content-Type: application/json
Authorization: Bearer <token>

{
  "sessionId": "abc123",
  "message": "什么是信贷业务？",
  "kbId": 1,
  "agentType": "rag",
  "useRerank": true
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "sessionId": "abc123",
    "answer": "信贷业务是指...",
    "references": [
      {
        "fileName": "信贷管理办法.pdf",
        "pageNum": 12,
        "similarity": 0.85
      }
    ]
  }
}
```

### 5.2 合规检查

| API路径 | HTTP方法 | 功能描述 |
|---------|----------|----------|
| `/api/agent/compliance/check` | POST | 对文档进行合规检查 |
| `/api/agent/compliance/report/{reportId}` | POST | 生成合规报告 |
| `/api/agent/compliance/report/{reportId}` | GET | 获取合规报告 |

**合规检查示例**:
```http
POST /api/agent/compliance/check
Content-Type: application/json
Authorization: Bearer <token>

{
  "documentId": 1,
  "checkType": "full"
}
```

### 5.3 信息抽取

| API路径 | HTTP方法 | 功能描述 |
|---------|----------|----------|
| `/api/agent/extraction/extract` | POST | 信贷材料信息抽取 |
| `/api/agent/extraction/template` | POST | 创建抽取模板 |
| `/api/agent/extraction/template/{id}` | GET | 获取抽取模板 |

**信息抽取示例**:
```http
POST /api/agent/extraction/extract
Content-Type: application/json
Authorization: Bearer <token>

{
  "documentId": 1,
  "templateId": 1,
  "batchMode": false
}
```

---

## 6. Python预处理 API (finrag4j-python)

Python预处理服务独立运行，不通过网关访问。

### 6.1 健康检查

```http
GET /health
```

**响应示例**:
```json
{
  "status": "healthy",
  "service": "finrag4j-python",
  "timestamp": "2024-01-01T12:00:00",
  "version": "1.0.0",
  "nacos_enabled": true,
  "nacos_registered": true
}
```

### 6.2 文档解析

| API路径 | HTTP方法 | 功能描述 |
|---------|----------|----------|
| `/api/parse/file` | POST | 通用文档解析（PDF/Word/Excel/TXT） |
| `/api/parse/ocr` | POST | OCR识别 |
| `/api/parse/text/clean` | POST | 文本清洗 |
| `/api/parse/text/chunk` | POST | 文本分块 |

---

## 7. 网关路由说明

| 路径前缀 | 目标服务 | 说明 |
|----------|----------|------|
| `/api/auth/**` | finrag4j-auth | 认证接口 |
| `/api/users/**` | finrag4j-auth | 用户管理接口 |
| `/api/roles/**` | finrag4j-auth | 角色管理接口 |
| `/api/permissions/**` | finrag4j-auth | 权限管理接口 |
| `/api/document/**` | finrag4j-document | 文档管理接口 |
| `/api/knowledge-base/**` | finrag4j-document | 知识库管理接口 |
| `/api/vector/**` | finrag4j-search | 向量管理接口 |
| `/api/rag/**` | finrag4j-search | RAG检索接口 |
| `/api/chat/**` | finrag4j-agent | 聊天接口 |
| `/api/agent/**` | finrag4j-agent | 业务Agent接口 |

---

## 8. 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（Token无效或过期） |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 9. 模块接口统计

| 模块 | Controller数量 | 接口数量 |
|------|---------------|----------|
| finrag4j-auth | 4 | 18 |
| finrag4j-document | 2 | 14 |
| finrag4j-search | 2 | 7 |
| finrag4j-agent | 2 | 9 |
| finrag4j-python | 1 | 5 |
| **总计** | **11** | **53** |

---

**文档版本**: v1.0.0  
**更新日期**: 2026年6月  
**前端API文件**: `frontend/src/api/`  
**Swagger文档**: `http://localhost:8080/docs`