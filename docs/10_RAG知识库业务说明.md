# FinRag4j RAG知识库业务说明

## 文档概述

本文档详细说明FinRag4j Java主服务的RAG知识库问答业务链路，包括知识库管理、文档生命周期、RAG检索、多轮对话、合规约束等功能。

**文档版本**: v1.0.0  
**编制日期**: 2024年  
**编制单位**: FinRag4j Team

---

## 1. 业务架构

### 1.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                      Controller层                                  │
│  - KnowledgeBaseController: 知识库CRUD                             │
│  - DocumentController: 文档管理                                    │
│  - ChatController: RAG问答对话                                     │
│  - ComplianceController: 合规管理                                   │
└─────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────┐
│                        Service层                                   │
│  - KnowledgeBaseService: 知识库管理                               │
│  - DocumentLifecycleService: 文档全生命周期                        │
│  - RagRetrievalService: RAG检索核心                                │
│  - RagChatService: RAG问答核心                                     │
│  - ChatSessionService: 多轮对话管理                                │
│  - ComplianceService: 合规规则引擎                                 │
│  - ModelRouterService: 模型智能路由                                │
└─────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────┐
│                        基础设施层                                   │
│  - PostgreSQL + PGVector: 向量数据库                               │
│  - Redis: 会话缓存、限流计数                                       │
│  - RocketMQ: 异步任务队列                                         │
│  - MinIO: 文件存储                                                │
│  - Python服务: 文档解析、OCR、分块                                 │
│  - LLM: Ollama/vLLM 大模型                                        │
└─────────────────────────────────────────────────────────────────────┘
```

### 1.2 核心业务流程

#### 1.2.1 文档上传处理流程

```
用户上传文件
    ↓
计算文件MD5（去重）
    ↓
检查是否已存在相同文件
    ↓
是 → 创建新版本
    ↓
否 → 上传到MinIO
    ↓
保存文档元数据
    ↓
创建初始版本记录
    ↓
发送解析任务到RocketMQ
    ↓
DocumentParseConsumer消费任务
    ↓
调用Python服务解析/OCR
    ↓
调用Python服务分块
    ↓
发送向量入库任务
    ↓
VectorIndexConsumer消费任务
    ↓
调用LLM向量化
    ↓
存入PGVector
    ↓
更新文档状态为已索引
```

#### 1.2.2 RAG问答流程

```
用户提问
    ↓
会话管理（Redis存储历史）
    ↓
模型路由（选择合适模型）
    ↓
向量召回（PGVector检索）
    ↓
结果重排（按相似度排序）
    ↓
合规检查（敏感词、相似度、幻觉）
    ↓
构建提示词（上下文+历史）
    ↓
调用大模型生成回答
    ↓
添加来源引用
    ↓
更新会话记忆
    ↓
保存对话历史到数据库
    ↓
返回结果
```

---

## 2. 核心功能模块

### 2.1 知识库管理

#### 2.1.1 功能说明

- 创建/编辑/删除知识库
- 标签分类管理
- 文档绑定/解绑
- 知识库配置（相似度阈值、召回数量、默认模型）

#### 2.1.2 关键接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/kb` | POST | 创建知识库 |
| `/api/kb/{id}` | PUT | 更新知识库 |
| `/api/kb/{id}` | DELETE | 删除知识库 |
| `/api/kb` | GET | 获取知识库列表 |
| `/api/kb/{kbId}/documents/{documentId}` | POST | 绑定文档 |
| `/api/kb/tags` | POST | 创建标签 |

#### 2.1.3 数据库表设计

**knowledge_base 表**:
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| kb_name | VARCHAR(100) | 知识库名称 |
| kb_code | VARCHAR(50) | 知识库编码（唯一） |
| description | VARCHAR(500) | 描述 |
| status | VARCHAR(20) | 状态 |
| default_model | VARCHAR(50) | 默认模型 |
| similarity_threshold | DOUBLE | 相似度阈值 |
| top_k | INTEGER | 召回数量 |

---

### 2.2 文档全生命周期

#### 2.2.1 功能说明

- 文件上传（支持PDF、Word、Excel、TXT）
- 异步解析（调用Python服务）
- 文本分块（调用Python服务）
- 版本管理
- 回收站（30天自动清理）
- 过期归档

#### 2.2.2 关键接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/documents/upload` | POST | 上传文档 |
| `/api/documents/{id}` | GET | 获取文档详情 |
| `/api/documents/{id}` | DELETE | 移到回收站 |
| `/api/documents/{id}/preview` | GET | 预览文档 |
| `/api/documents/{id}/versions` | GET | 获取版本列表 |
| `/api/documents/recycle-bin` | GET | 获取回收站列表 |

#### 2.2.3 文档状态流转

```
uploaded → parsing → parsed → indexing → indexed
              ↓            ↓           ↓
              └────────────┴───────────→ failed
```

---

### 2.3 RAG检索核心

#### 2.3.1 功能说明

- 向量召回（基于PGVector）
- 结果重排
- 原文溯源
- 来源引用生成

#### 2.3.2 检索算法

使用PGVector的IVFFlat索引进行向量相似度检索：

```sql
SELECT id, document_id, content, 
       1 - (vector <=> #{queryVector}) as similarity
FROM vector_chunk
WHERE tenant_id = #{tenantId} 
  AND 1 - (vector <=> #{queryVector}) >= #{threshold}
ORDER BY vector <=> #{queryVector}
LIMIT #{topK}
```

#### 2.3.3 检索结果结构

```java
public record RagResult(
    List<RetrievedChunk> chunks,    // 检索到的文本块
    Double avgSimilarity,            // 平均相似度
    String context,                  // 拼接后的上下文
    List<SourceReference> references // 来源引用
) {}
```

---

### 2.4 多轮对话管理

#### 2.4.1 功能说明

- Redis会话缓存（24小时过期）
- 对话历史持久化
- 会话管理（创建/删除）
- 问答收藏

#### 2.4.2 会话管理流程

```
创建会话 → 生成sessionId → 存储到Redis
    ↓
用户提问 → 更新会话记忆 → 保存历史到数据库
    ↓
继续对话 → 读取历史 → 构建上下文 → 调用LLM
    ↓
结束会话 → 删除Redis缓存 → 软删除数据库记录
```

#### 2.4.3 关键接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/chat/new` | POST | 开始新对话 |
| `/api/chat/continue` | POST | 继续对话 |
| `/api/chat/history/{sessionId}` | GET | 获取会话历史 |
| `/api/chat/favorite` | POST | 添加收藏 |

---

### 2.5 合规规则引擎

#### 2.5.1 功能说明

- 敏感词检测（金融敏感词过滤）
- 低相似度拦截
- 幻觉检测
- 无知识库内容拦截

#### 2.5.2 合规检查流程

```
用户提问 → 敏感词检测 → 拦截/通过
    ↓
RAG检索 → 相似度检查 → 拦截/通过
    ↓
LLM回答 → 敏感词检测 → 拦截/通过
    ↓
        → 幻觉检测 → 拦截/通过
    ↓
返回结果
```

#### 2.5.3 敏感词级别

| 级别 | 说明 | 处理方式 |
|------|------|----------|
| 1 | 警告级别 | 记录日志，允许通过 |
| 2 | 拦截级别 | 直接拦截，拒绝处理 |

#### 2.5.4 幻觉检测规则

1. **相似度检测**: 相似度低于阈值（默认0.5）视为幻觉
2. **回答长度检测**: 过短回答（<10字符）但有上下文视为幻觉
3. **编造迹象检测**: 检测"根据最新政策"等编造关键词

---

### 2.6 模型智能路由

#### 2.6.1 功能说明

- 问题复杂度分析
- 模型选择（7B/14B/72B）
- 并发限流
- 租户算力配额管理

#### 2.6.2 路由决策逻辑

| 问题特征 | 选择模型 |
|----------|----------|
| 短句（<20字符） | 7B小模型 |
| 中等长度（20-50字符） | 7B小模型 |
| 长文本（≥50字符） | 14B大模型 |
| 包含推理关键词 | 14B/72B大模型 |

#### 2.6.3 限流策略

- **日请求限额**: 按租户配置，默认1000次/天
- **并发限制**: 按租户配置，默认5并发
- **Redis计数**: 使用Redis原子操作计数

---

## 3. 数据库表结构

### 3.1 新增表清单

| 表名 | 说明 |
|------|------|
| knowledge_base | 知识库表 |
| tag | 标签表 |
| kb_document | 知识库文档关联表 |
| kb_tag | 知识库标签关联表 |
| document_version | 文档版本表 |
| recycle_bin | 回收站表 |
| chat_history | 对话历史表 |
| chat_favorite | 问答收藏表 |
| sensitive_word | 敏感词表 |
| tenant_quota | 租户配额表 |

### 3.2 表关系图

```
tenant ──┬── knowledge_base ──┬── kb_document ── document
         │                    └── kb_tag ── tag
         │
         ├── chat_history ─── chat_favorite
         ├── sensitive_word
         └── tenant_quota
```

---

## 4. API接口总览

### 4.1 知识库管理

| 接口 | 方法 | 路径 |
|------|------|------|
| 创建知识库 | POST | `/api/kb` |
| 更新知识库 | PUT | `/api/kb/{id}` |
| 删除知识库 | DELETE | `/api/kb/{id}` |
| 获取知识库列表 | GET | `/api/kb` |
| 获取知识库详情 | GET | `/api/kb/{id}` |
| 绑定文档 | POST | `/api/kb/{kbId}/documents/{documentId}` |

### 4.2 文档管理

| 接口 | 方法 | 路径 |
|------|------|------|
| 上传文档 | POST | `/api/documents/upload` |
| 获取文档列表 | GET | `/api/documents` |
| 获取文档详情 | GET | `/api/documents/{id}` |
| 删除文档 | DELETE | `/api/documents/{id}` |
| 预览文档 | GET | `/api/documents/{id}/preview` |
| 获取回收站 | GET | `/api/documents/recycle-bin` |

### 4.3 RAG问答

| 接口 | 方法 | 路径 |
|------|------|------|
| 开始新对话 | POST | `/api/chat/new` |
| 继续对话 | POST | `/api/chat/continue` |
| 获取会话历史 | GET | `/api/chat/history/{sessionId}` |
| 结束会话 | DELETE | `/api/chat/session/{sessionId}` |
| 添加收藏 | POST | `/api/chat/favorite` |

### 4.4 合规管理

| 接口 | 方法 | 路径 |
|------|------|------|
| 添加敏感词 | POST | `/api/compliance/sensitive-words` |
| 获取敏感词列表 | GET | `/api/compliance/sensitive-words` |
| 获取租户配额 | GET | `/api/compliance/quota/{tenantId}` |
| 更新租户配额 | PUT | `/api/compliance/quota/{tenantId}` |

---

## 5. 部署与运行

### 5.1 环境要求

- JDK 21+
- PostgreSQL 14+（需安装PGVector扩展）
- Redis 7+
- RocketMQ 5.0+
- MinIO
- Python预处理服务（fastapi服务）

### 5.2 启动命令

```bash
# 1. 创建数据库
psql -d postgres -f sql/init.sql
psql -d finrag4j -f sql/schema.sql

# 2. 配置环境变量
cp .env.example .env
# 编辑 .env 文件配置数据库连接等信息

# 3. 启动服务
cd backend/finrag4j-java
mvn spring-boot:run

# 4. 访问接口文档
http://localhost:8080/doc.html
```

### 5.3 接口测试

```bash
# 创建知识库
curl -X POST http://localhost:8080/api/kb \
  -H "Content-Type: application/json" \
  -d '{"kbName":"测试知识库","kbCode":"test_kb"}' \
  -G --data-urlencode "tenantId=1"

# 上传文档
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@test.pdf" \
  -F "tenantId=1"

# RAG问答
curl -X POST http://localhost:8080/api/chat/new \
  -G --data-urlencode "question=什么是RAG?" \
  --data-urlencode "kbId=1" \
  --data-urlencode "tenantId=1"
```

---

## 6. 总结

FinRag4j RAG知识库问答业务已完成以下功能：

1. ✅ **知识库管理**: 创建/编辑/删除、标签分类、文档绑定
2. ✅ **文档全生命周期**: 上传、解析、分块、向量化、版本管理、回收站
3. ✅ **RAG检索核心**: 向量召回、结果重排、原文溯源
4. ✅ **多轮对话**: Redis会话记忆、历史持久化、收藏功能
5. ✅ **合规约束**: 敏感词过滤、低相似度拦截、幻觉检测
6. ✅ **模型路由**: 智能模型选择、并发限流、租户配额
7. ✅ **完整API接口**: 配套Knife4j文档

---

**文档版本**: v1.0.0  
**编制日期**: 2024年  
**编制单位**: FinRag4j Team