# FinRag4j Java主服务

## 项目简介

FinRag4j Java主服务是面向金融行业的企业级大模型RAG应用框架的核心服务，承担全部业务逻辑、RAG检索、Agent流程、权限管理、审计日志等职责。

## 技术栈

- **Java**: 21
- **Spring Boot**: 3.2.0
- **LangChain4j**: 0.30.0
- **MyBatis-Plus**: 3.5.5
- **PostgreSQL + PGVector**: 向量数据库
- **Redis**: 缓存
- **RocketMQ**: 消息队列
- **MinIO**: 对象存储
- **Knife4j**: 接口文档

## 项目结构

```
finrag4j-java/
├── src/main/java/com/finrag4j/
│   ├── FinRag4jApplication.java          # 启动类
│   ├── common/                           # 公共模块
│   │   ├── config/                       # 全局配置
│   │   ├── exception/                    # 异常处理
│   │   ├── response/                     # 统一返回体
│   │   └── aspect/                       # 切面
│   ├── config/                           # 基础设施配置
│   │   ├── RedisConfig.java
│   │   ├── MinioConfig.java
│   │   ├── PostgresConfig.java
│   │   ├── RocketMQConfig.java
│   │   └── LLMConfig.java
│   ├── entity/                           # 实体类
│   │   ├── Document.java
│   │   └── VectorChunk.java
│   ├── mapper/                           # Mapper接口
│   │   ├── DocumentMapper.java
│   │   └── VectorChunkMapper.java
│   ├── service/                          # 服务层
│   │   ├── MinioService.java
│   │   ├── DocumentService.java
│   │   ├── VectorService.java
│   │   └── LLMService.java
│   ├── client/                           # 客户端
│   │   └── python/                       # Python服务客户端
│   │       ├── FinDocParseClient.java
│   │       ├── PythonServiceConfig.java
│   │       └── dto/                      # 数据传输对象
│   └── task/                             # 异步任务
│       ├── message/                      # 消息定义
│       ├── producer/                     # 消息生产者
│       └── consumer/                     # 消息消费者
├── src/main/resources/
│   └── application.yml                   # 配置文件
├── sql/
│   └── init.sql                          # 数据库初始化脚本
├── Dockerfile                            # Docker镜像构建文件
└── pom.xml                               # Maven配置文件
```

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.9+
- PostgreSQL 14+ (需安装PGVector扩展)
- Redis 7+
- RocketMQ 5.0+
- MinIO

### 本地运行

1. **克隆项目**
```bash
git clone https://github.com/your-org/finrag4j.git
cd finrag4j/backend/finrag4j-java
```

2. **配置数据库**
```bash
# 创建数据库
createdb finrag4j

# 执行初始化脚本
psql -d finrag4j -f sql/init.sql
```

3. **修改配置**
```yaml
# 编辑 src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/finrag4j
    username: your_username
    password: your_password
```

4. **启动服务**
```bash
mvn spring-boot:run
```

5. **访问接口文档**
```
http://localhost:8080/doc.html
```

### Docker运行

1. **构建镜像**
```bash
docker build -t finrag4j-java:1.0.0 .
```

2. **运行容器**
```bash
docker run -d \
  --name finrag4j-java \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e POSTGRES_HOST=postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e REDIS_HOST=redis \
  -e ROCKETMQ_NAME_SERVER=rocketmq:9876 \
  -e MINIO_ENDPOINT=http://minio:9000 \
  finrag4j-java:1.0.0
```

## 核心功能

### 1. 知识库管理
- 创建/编辑/删除知识库
- 标签分类管理
- 文档绑定/解绑
- 知识库配置（相似度阈值、召回数量、默认模型）

### 2. 文档全生命周期
- 文件上传（PDF、Word、Excel、TXT）
- 异步解析（调用Python服务）
- 文本分块（调用Python服务）
- 版本管理
- 回收站（30天自动清理）
- MinIO对象存储
- 文件去重（MD5）

### 3. RAG检索核心
- PGVector向量存储
- 向量相似度检索
- 结果重排
- 原文溯源
- 来源引用生成

### 4. 多轮对话管理
- Redis会话缓存（24小时过期）
- 对话历史持久化
- 会话管理（创建/删除）
- 问答收藏

### 5. 合规规则引擎
- 敏感词检测（金融敏感词过滤）
- 低相似度拦截
- 幻觉检测
- 无知识库内容拦截

### 6. 模型智能路由
- 问题复杂度分析
- 模型选择（7B/14B/72B）
- 并发限流
- 租户算力配额管理

### 7. 异步任务调度
- 文档解析任务
- 向量入库任务
- 失败重试机制
- RocketMQ消息队列

## RAG问答流程

```
用户提问 → 会话管理 → 模型路由 → 向量召回 → 结果重排 → 合规检查 → 调用LLM → 返回结果
              ↓                    ↓
         Redis缓存            PGVector检索
```

### RAG问答流程图

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ 用户提问     │───→│ 会话管理     │───→│ 模型智能路由 │
└──────────────┘    └──────────────┘    └──────────────┘
                                              ↓
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ 结果重排     │←───│ 向量召回     │←───│ 选择模型     │
└──────────────┘    └──────────────┘    └──────────────┘
       ↓
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ 合规检查     │───→│ 调用大模型   │───→│ 返回结果     │
│ (敏感词/幻觉) │    │ (生成回答)   │    │ (带来源引用) │
└──────────────┘    └──────────────┘    └──────────────┘
```

## 接口文档

启动服务后，访问以下地址查看接口文档：

- **Knife4j文档**: http://localhost:8080/doc.html
- **Actuator健康检查**: http://localhost:8080/actuator/health

## 配置说明

### 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| SPRING_PROFILES_ACTIVE | 运行环境 | dev |
| POSTGRES_HOST | PostgreSQL地址 | localhost |
| POSTGRES_PORT | PostgreSQL端口 | 5432 |
| POSTGRES_DB | 数据库名称 | finrag4j |
| POSTGRES_USER | 数据库用户名 | postgres |
| POSTGRES_PASSWORD | 数据库密码 | postgres |
| REDIS_HOST | Redis地址 | localhost |
| REDIS_PORT | Redis端口 | 6379 |
| ROCKETMQ_NAME_SERVER | RocketMQ地址 | localhost:9876 |
| MINIO_ENDPOINT | MinIO地址 | http://localhost:9000 |
| MINIO_ACCESS_KEY | MinIO访问密钥 | minioadmin |
| MINIO_SECRET_KEY | MinIO秘密密钥 | minioadmin |
| PYTHON_SERVICE_BASE_URL | Python服务地址 | http://localhost:8001 |
| LLM_TYPE | 大模型类型 | ollama |
| LLM_BASE_URL | 大模型地址 | http://localhost:11434 |
| LLM_DEFAULT_MODEL | 默认模型 | qwen2:7b |

## 开发规范

### 代码规范
- 遵循阿里巴巴Java开发手册
- 使用Lombok简化代码
- 统一异常处理
- 统一返回格式

### 提交规范
```
feat: 新功能
fix: 修复bug
docs: 文档更新
refactor: 重构
test: 测试
```

## 国产化适配

本项目支持国产化信创环境：

- **JDK**: 支持麒麟JDK、鲲鹏JDK
- **数据库**: 支持人大金仓、达梦
- **操作系统**: 支持麒麟OS、统信OS
- **CPU架构**: 支持ARM架构（鲲鹏、飞腾）

## 许可证

Apache License 2.0

## 联系方式

- 项目地址: https://github.com/your-org/finrag4j
- 邮箱: support@finrag4j.com