# FinRag4j Java主服务基座架构说明

## 文档概述

本文档详细说明FinRag4j Java主服务的基础架构设计、模块划分、技术选型以及Python客户端调用流程。

**文档版本**: v1.0.0  
**编制日期**: 2024年  
**编制单位**: FinRag4j Team

---

## 1. 架构概述

### 1.1 设计原则

- **微服务架构**: 模块化设计，职责清晰
- **高可用性**: 支持水平扩展，故障自动转移
- **可扩展性**: 插件化设计，易于扩展新功能
- **租户隔离**: 多租户数据隔离，安全可控
- **信创适配**: 支持国产化软硬件环境

### 1.2 技术栈

| 分类 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 语言 | Java | 21 | 支持国产信创JDK |
| 框架 | Spring Boot | 3.2.0 | 核心框架 |
| RAG框架 | LangChain4j | 0.30.0 | 大模型集成 |
| ORM | MyBatis-Plus | 3.5.5 | 数据库操作 |
| 数据库 | PostgreSQL + PGVector | 14+ | 向量数据库 |
| 缓存 | Redis | 7+ | 分布式缓存 |
| 消息队列 | RocketMQ | 5.0+ | 异步任务 |
| 对象存储 | MinIO | Latest | 文件存储 |
| 接口文档 | Knife4j | 4.4.0 | API文档 |
| 工具类 | Hutool | 5.8.24 | 常用工具 |

---

## 2. 分层架构

### 2.1 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        Controller层                          │
│  - 接收HTTP请求                                              │
│  - 参数校验                                                  │
│  - 调用Service层                                             │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                        Service层                             │
│  - 业务逻辑处理                                              │
│  - 事务控制                                                  │
│  - 调用Mapper层和外部服务                                    │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                        Mapper层                              │
│  - 数据库操作                                                │
│  - SQL映射                                                  │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                        Entity层                              │
│  - 数据实体定义                                              │
│  - 数据库表映射                                              │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 模块划分

#### 2.2.1 common模块（公共模块）

**职责**: 提供全局通用功能

**核心类**:
- `Result<T>`: 统一返回结果封装
- `BusinessException`: 业务异常类
- `GlobalExceptionHandler`: 全局异常处理器
- `CorsConfig`: 跨域配置
- `LogAspect`: 日志切面
- `Knife4jConfig`: 接口文档配置

**功能特性**:
- 统一异常处理和返回格式
- 自动记录请求和响应日志
- 支持跨域访问
- 自动生成API文档

#### 2.2.2 config模块（基础设施配置）

**职责**: 配置基础设施连接

**核心类**:
- `RedisConfig`: Redis缓存配置
- `MinioConfig`: MinIO对象存储配置
- `PostgresConfig`: PostgreSQL + PGVector配置
- `RocketMQConfig`: RocketMQ消息队列配置
- `LLMConfig`: 大模型配置

**功能特性**:
- 自动初始化PGVector扩展
- 自动创建MinIO bucket
- 支持多模型管理
- 支持模型路由

#### 2.2.3 entity模块（实体层）

**职责**: 定义数据实体

**核心类**:
- `Document`: 文档实体
- `VectorChunk`: 向量文本块实体

**功能特性**:
- 支持MyBatis-Plus注解
- 自动填充创建时间和更新时间
- 逻辑删除支持

#### 2.2.4 mapper模块（数据访问层）

**职责**: 数据库CRUD操作

**核心类**:
- `DocumentMapper`: 文档Mapper
- `VectorChunkMapper`: 向量文本块Mapper

**功能特性**:
- 继承BaseMapper，提供基础CRUD
- 自定义SQL查询
- 向量相似度检索

#### 2.2.5 service模块（业务逻辑层）

**职责**: 核心业务逻辑

**核心类**:
- `MinioService`: MinIO文件存储服务
- `DocumentService`: 文档服务
- `VectorService`: 向量库服务
- `LLMService`: 大模型服务

**功能特性**:
- 文件上传、下载、删除
- 文档元数据管理
- 文件去重（MD5）
- 向量增删改查
- 向量相似度检索
- 大模型调用
- RAG对话

#### 2.2.6 client模块（客户端层）

**职责**: 调用外部服务

**核心类**:
- `FinDocParseClient`: Python服务HTTP客户端
- `PythonServiceConfig`: Python服务配置

**功能特性**:
- 文档解析接口调用
- OCR识别接口调用
- 文本分块接口调用
- 自动重试机制
- 超时处理

#### 2.2.7 task模块（异步任务层）

**职责**: 异步任务处理

**核心类**:
- `DocumentParseMessage`: 文档解析消息
- `VectorIndexMessage`: 向量入库消息
- `TaskProducer`: 任务消息生产者
- `DocumentParseConsumer`: 文档解析任务消费者
- `VectorIndexConsumer`: 向量入库任务消费者

**功能特性**:
- 文档解析任务
- 向量入库任务
- 失败重试机制
- 延迟消息支持

---

## 3. 核心功能模块

### 3.1 文件上传底座

#### 3.1.1 功能说明

- 支持PDF、Word、Excel、TXT等多种格式
- MinIO对象存储
- 文件去重（MD5）
- 租户隔离
- 文档元数据管理

#### 3.1.2 核心流程

```
用户上传文件
    ↓
计算文件MD5
    ↓
检查文件是否已存在（去重）
    ↓
上传到MinIO
    ↓
保存文档元数据到数据库
    ↓
发送文档解析任务到RocketMQ
    ↓
返回文档信息
```

#### 3.1.3 关键代码

```java
// DocumentService.java
public Document uploadDocument(MultipartFile file, Long tenantId, Long createdBy) {
    // 1. 计算文件MD5
    String fileMd5 = calculateMd5(file);
    
    // 2. 检查文件是否已存在（去重）
    Document existDoc = baseMapper.selectByMd5(fileMd5, tenantId);
    if (existDoc != null) {
        return existDoc;
    }
    
    // 3. 上传到MinIO
    String storagePath = minioService.uploadFile(file, generateStoragePath(fileType, fileMd5));
    
    // 4. 保存文档元数据
    Document document = Document.builder()
            .fileName(file.getOriginalFilename())
            .fileMd5(fileMd5)
            .storagePath(storagePath)
            .status("uploaded")
            .build();
    
    save(document);
    
    return document;
}
```

### 3.2 向量库底层封装

#### 3.2.1 功能说明

- 向量增删改查
- 向量批量插入
- 向量相似度检索
- 租户隔离

#### 3.2.2 核心流程

```
文本向量化
    ↓
生成PGVector对象
    ↓
批量插入向量库
    ↓
创建向量索引（IVFFlat）
    ↓
支持向量相似度检索
```

#### 3.2.3 关键代码

```java
// VectorService.java
public List<VectorChunk> searchByVector(
        PGvector queryVector,
        Long tenantId,
        Integer topK,
        Double threshold
) {
    return baseMapper.searchByVector(queryVector, tenantId, topK, threshold);
}

// VectorChunkMapper.java
@Select("SELECT id, document_id, content, chunk_index, chunk_size, " +
        "1 - (vector <=> #{queryVector}) as similarity " +
        "FROM vector_chunk " +
        "WHERE tenant_id = #{tenantId} AND deleted = 0 " +
        "AND 1 - (vector <=> #{queryVector}) >= #{threshold} " +
        "ORDER BY vector <=> #{queryVector} " +
        "LIMIT #{topK}")
List<VectorChunk> searchByVector(
        @Param("queryVector") PGvector queryVector,
        @Param("tenantId") Long tenantId,
        @Param("topK") Integer topK,
        @Param("threshold") Double threshold
);
```

### 3.3 离线大模型调度

#### 3.3.1 功能说明

- Ollama/vLLM支持
- 模型路由
- 单轮对话
- 多轮对话
- RAG对话
- 文本向量化

#### 3.3.2 核心流程

```
用户提问
    ↓
检索相关文档（向量检索）
    ↓
构建系统提示词
    ↓
调用大模型生成回答
    ↓
返回回答结果
```

#### 3.3.3 关键代码

```java
// LLMService.java
public String ragChat(String question, String context) {
    String systemPrompt = "你是一个专业的金融助手。请基于以下参考文档回答用户问题，不要编造信息。如果参考文档中没有相关信息，请如实告知。\n\n参考文档：\n" + context;
    
    return chatWithSystem(systemPrompt, question);
}

public String chatWithSystem(String systemPrompt, String userMessage) {
    List<ChatMessage> messages = new ArrayList<>();
    messages.add(SystemMessage.from(systemPrompt));
    messages.add(UserMessage.from(userMessage));
    
    return chat(messages);
}
```

### 3.4 异步任务调度

#### 3.4.1 功能说明

- 文档解析任务
- 向量入库任务
- 失败重试机制
- 延迟消息支持

#### 3.4.2 核心流程

```
文档上传完成
    ↓
发送文档解析任务到RocketMQ
    ↓
DocumentParseConsumer消费任务
    ↓
调用Python服务解析文档
    ↓
调用Python服务分块文本
    ↓
发送向量入库任务到RocketMQ
    ↓
VectorIndexConsumer消费任务
    ↓
向量化文本块
    ↓
存入PGVector
```

#### 3.4.3 关键代码

```java
// DocumentParseConsumer.java
@Override
public void onMessage(DocumentParseMessage message) {
    try {
        // 1. 查询文档
        Document document = documentService.getDocumentById(message.getDocumentId());
        
        // 2. 调用Python服务解析文档
        ParseResponse parseResponse = finDocParseClient.parseDocument(parseRequest);
        
        // 3. 更新解析结果
        documentService.updateParseResult(documentId, parseResponse.getText(), parseResponse.getPageCount());
        
        // 4. 调用Python服务进行文本分块
        ChunkResponse chunkResponse = finDocParseClient.chunkText(chunkRequest);
        
        // 5. 发送向量入库任务
        taskProducer.sendVectorIndexTask(vectorIndexMessage);
        
    } catch (Exception e) {
        // 失败重试
        if (retryCount < MAX_RETRY_COUNT) {
            taskProducer.sendDelayMessage("document-parse-topic", retryMessage, 5);
        }
    }
}
```

---

## 4. Python客户端调用流程

### 4.1 调用架构

```
Java服务
    ↓
FinDocParseClient（HTTP客户端）
    ↓
WebClient（响应式HTTP客户端）
    ↓
Python服务（FastAPI）
    ↓
文档解析/OCR/分块
    ↓
返回结果
```

### 4.2 调用流程

#### 4.2.1 文档解析调用

```java
// 1. 构建请求
ParseRequest request = ParseRequest.builder()
        .fileType("pdf")
        .fileContent(base64Content)
        .needOcr(true)
        .needClean(true)
        .build();

// 2. 调用Python服务
ParseResponse response = finDocParseClient.parseDocument(request);

// 3. 处理响应
if ("success".equals(response.getStatus())) {
    String text = response.getText();
    Integer pageCount = response.getPageCount();
}
```

#### 4.2.2 OCR识别调用

```java
// 1. 构建请求
OcrRequest request = OcrRequest.builder()
        .imageContent(base64Image)
        .language("chinese")
        .build();

// 2. 调用Python服务
OcrResponse response = finDocParseClient.recognizeOcr(request);

// 3. 处理响应
if ("success".equals(response.getStatus())) {
    String text = response.getText();
    Double confidence = response.getConfidence();
}
```

#### 4.2.3 文本分块调用

```java
// 1. 构建请求
ChunkRequest request = ChunkRequest.builder()
        .text(parsedText)
        .strategy("regulatory")
        .chunkSize(600)
        .chunkOverlap(100)
        .build();

// 2. 调用Python服务
ChunkResponse response = finDocParseClient.chunkText(request);

// 3. 处理响应
if ("success".equals(response.getStatus())) {
    List<Chunk> chunks = response.getChunks();
}
```

### 4.3 异常处理和重试

#### 4.3.1 异常处理

```java
try {
    ParseResponse response = finDocParseClient.parseDocument(request);
    
    if (response == null || !"success".equals(response.getStatus())) {
        throw new BusinessException("文档解析失败: " + response.getError());
    }
    
} catch (WebClientResponseException e) {
    log.error("文档解析HTTP异常: {}", e.getMessage());
    throw new BusinessException("文档解析失败: " + e.getMessage());
    
} catch (Exception e) {
    log.error("文档解析异常: {}", e.getMessage(), e);
    throw new BusinessException("文档解析失败: " + e.getMessage());
}
```

#### 4.3.2 自动重试

```java
ParseResponse response = webClient()
        .post()
        .uri("/api/parse")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(ParseResponse.class)
        .timeout(Duration.ofMillis(config.getReadTimeout()))
        .retryWhen(Retry.backoff(
                config.getRetryTimes(),
                Duration.ofMillis(config.getRetryInterval())
        ))
        .block();
```

### 4.4 配置说明

```yaml
python:
  service:
    base-url: http://localhost:8001  # Python服务地址
    connect-timeout: 5000            # 连接超时（毫秒）
    read-timeout: 30000              # 读取超时（毫秒）
    retry-times: 3                   # 重试次数
    retry-interval: 1000             # 重试间隔（毫秒）
```

---

## 5. 数据库设计

### 5.1 核心表结构

#### 5.1.1 文档表（document）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 文档ID |
| file_name | VARCHAR(255) | 文档名称 |
| file_type | VARCHAR(20) | 文件类型 |
| file_size | BIGINT | 文件大小 |
| file_md5 | VARCHAR(32) | 文件MD5 |
| storage_path | VARCHAR(500) | MinIO存储路径 |
| status | VARCHAR(20) | 文档状态 |
| parsed_text | TEXT | 解析后的文本 |
| page_count | INTEGER | 页数 |
| tenant_id | BIGINT | 租户ID |
| created_by | BIGINT | 创建人ID |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted | INTEGER | 删除标记 |

#### 5.1.2 向量文本块表（vector_chunk）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 文本块ID |
| document_id | BIGINT | 文档ID |
| content | TEXT | 文本块内容 |
| vector | vector(768) | 向量 |
| chunk_index | INTEGER | 块序号 |
| chunk_size | INTEGER | 块大小 |
| chunk_strategy | VARCHAR(20) | 分块策略 |
| tenant_id | BIGINT | 租户ID |
| created_at | TIMESTAMP | 创建时间 |
| deleted | INTEGER | 删除标记 |

### 5.2 索引设计

```sql
-- 向量索引（IVFFlat算法）
CREATE INDEX idx_vector_chunk_vector ON vector_chunk 
USING ivfflat (vector vector_cosine_ops) 
WITH (lists = 100);

-- 文档MD5索引（用于去重）
CREATE INDEX idx_document_md5 ON document(file_md5);

-- 租户隔离索引
CREATE INDEX idx_document_tenant ON document(tenant_id);
CREATE INDEX idx_vector_chunk_tenant ON vector_chunk(tenant_id);
```

---

## 6. 部署说明

### 6.1 环境要求

- JDK 21+
- PostgreSQL 14+ (需安装PGVector扩展)
- Redis 7+
- RocketMQ 5.0+
- MinIO

### 6.2 快速启动

```bash
# 1. 克隆项目
git clone https://github.com/your-org/finrag4j.git
cd finrag4j/backend/finrag4j-java

# 2. 配置数据库
psql -d finrag4j -f sql/init.sql

# 3. 修改配置
vim src/main/resources/application.yml

# 4. 启动服务
mvn spring-boot:run

# 5. 访问接口文档
http://localhost:8080/doc.html
```

### 6.3 Docker部署

```bash
# 1. 构建镜像
docker build -t finrag4j-java:1.0.0 .

# 2. 运行容器
docker run -d \
  --name finrag4j-java \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  finrag4j-java:1.0.0
```

---

## 7. 国产化适配

### 7.1 支持的国产化环境

| 组件 | 国产替代方案 |
|------|-----------|
| JDK | 麒麟JDK、鲲鹏JDK |
| 数据库 | 人大金仓、达梦 |
| 操作系统 | 麒麟OS、统信OS |
| CPU架构 | ARM架构（鲲鹏、飞腾） |

### 7.2 适配说明

- 使用Java 21，兼容国产JDK
- 使用标准JDBC接口，兼容国产数据库
- 使用Docker多阶段构建，支持多架构
- 使用环境变量配置，易于适配不同环境

---

## 8. 总结

FinRag4j Java主服务基座提供了完整的基础能力，包括：

1. **项目分层架构**: controller/service/mapper/entity/config/task/client
2. **全局统一配置**: 异常处理、返回体、跨域、日志、Knife4j
3. **基础设施配置**: PostgreSQL、Redis、RocketMQ、MinIO
4. **Python服务集成**: 文档解析、OCR识别、文本分块
5. **文件上传底座**: MinIO存储、元数据管理、去重逻辑
6. **向量库底层封装**: PGVector增删改查、批量插入、检索工具
7. **离线大模型调度**: Ollama/vLLM客户端、模型路由
8. **异步任务调度**: 文档解析、向量入库、失败重试
9. **数据库初始化**: 完整的SQL脚本
10. **Docker部署**: Dockerfile和配置文件

该基座可直接启动使用，为上层Agent业务提供坚实的基础支撑。

---

**文档版本**: v1.0.0  
**编制日期**: 2024年  
**编制单位**: FinRag4j Team