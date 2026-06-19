# FinRag4j - 金融领域大模型RAG应用框架

<div align="center">

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-21%2B-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4-4fc08d.svg)](https://vuejs.org/)

**⭐ 如果这个项目对您有帮助，请给我们一个 Star ⭐**

[在线演示](#-在线演示) | [快速开始](#-快速开始) | [功能特性](#-核心功能) | [部署文档](#-部署指南) | [商业咨询](#-商业咨询)

</div>

---

## 🎯 项目定位

FinRag4j 是一款**面向金融行业的企业级大模型RAG应用框架**，专注于金融文档处理、智能问答、合规分析等场景。

### 核心优势

- 🔒 **私有化部署**：数据不出域，满足金融合规要求
- 🏦 **金融垂直**：预置金融知识库、合规检查、信贷抽取等业务能力
- 🚀 **开箱即用**：完整前后端、一键部署、快速落地
- 🔧 **灵活扩展**：工作流编排、Agent定制、模型智能路由
- 🇨🇳 **国产信创**：支持麒麟操作系统、鲲鹏处理器、国产数据库

---

## ✨ 核心功能

### 基础功能（开源版）

| 功能模块 | 描述 | 状态 |
|---------|------|:----:|
| 📚 知识库管理 | 创建/编辑/删除知识库，文档分类管理 | ✅ |
| 📄 文档处理 | 支持PDF/Word/Excel/TXT，自动解析、分块、向量化 | ✅ |
| 🤖 RAG智能问答 | 向量检索 + 大模型生成，多轮对话，来源溯源 | ✅ |
| 👥 用户权限 | 用户管理、角色管理、基础权限控制 | ✅ |

### 企业功能（商业版）

| 功能模块 | 描述 | 状态 |
|---------|------|:----:|
| 🏢 多租户隔离 | 物理隔离、独立向量空间、算力配额 | ✅ |
| 🔐 RBAC权限 | 细粒度权限控制、部门管理、三级权限 | ✅ |
| 📋 全链路审计 | AOP自动捕获、日志留存5年、监管报表导出 | ✅ |
| 💼 信贷材料抽取 | 自定义模板、批量抽取、人工复核 | ✅ |
| 🛡️ 监管合规自查 | 风险分级、整改报告、强制复核节点 | ✅ |
| 🔄 工作流引擎 | 可视化编排、串行/并行/分支、定时触发 | ✅ |
| 🔗 SSO单点登录 | CAS/OAuth2集成 | ✅ |

---

## 🛠️ 技术栈

### 后端技术栈

| 分类 | 技术 | 版本 |
|------|------|------|
| 基础框架 | Spring Boot | 3.2.x |
| 微服务框架 | Spring Cloud | 2023.0.x |
| 服务注册/配置 | Nacos | 2.2.x |
| API网关 | Spring Cloud Gateway | - |
| RAG框架 | LangChain4j | 0.30+ |
| 权限安全 | Spring Security | 6.x |
| 持久层 | MyBatis-Plus | 3.5.x |
| 数据库 | PostgreSQL + PGVector | 16+ |
| 缓存 | Redis | 7.x |
| 消息队列 | RocketMQ | 5.0+ |
| 对象存储 | MinIO | 2024+ |
| API文档 | Knife4j | 4.x |
| 推理适配 | Ollama / vLLM | - |

### Python预处理服务

| 分类 | 技术 | 版本 | 描述 |
|------|------|------|------|
| 运行环境 | Python | 3.12+ | 运行时环境 |
| 框架 | FastAPI + Uvicorn | 0.110+ | 高性能异步API |
| 配置中心 | nacos-sdk-python | 1.4+ | Nacos配置获取 |
| 文档解析 | unstructured, python-docx, openpyxl | - | 多格式解析 |
| OCR识别 | PaddleOCR | 2.8+ | 离线中文OCR |
| 图像处理 | OpenCV-Python, Pillow | - | 图像预处理 |
| 文本处理 | jieba, nltk | - | 中文分词/NLP |
| 依赖管理 | uv | 0.1+ | 快速依赖管理 |

### 前端技术栈

| 分类 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue3 | 3.4+ |
| 构建工具 | Vite | 5.x |
| UI组件 | Element Plus | 2.6+ |
| HTTP客户端 | Axios | 1.6+ |
| 图表库 | ECharts | 5.5+ |

---

## 📁 项目结构（微服务架构）

```
FinRag4j/
├── docs/                       # 📚 完整文档
│   ├── 1_产品可行性分析.md
│   ├── 2_产品规划方案.md
│   ├── ...
│   └── 25_90天开源落地迭代计划.md
├── backend/
│   ├── pom.xml                 # Maven父模块
│   ├── finrag4j-gateway/       # 🚪 API网关服务 (Port: 8080)
│   │   └── src/main/java/com/finrag4j/gateway/
│   │       └── config/         # 路由、限流、鉴权配置
│   ├── finrag4j-auth/          # 🔐 认证授权服务 (Port: 8081)
│   │   └── src/main/java/com/finrag4j/auth/
│   │       ├── controller/     # 用户、角色、权限控制器
│   │       ├── service/        # 认证、RBAC、JWT服务
│   │       └── mapper/          # 数据访问层
│   ├── finrag4j-document/      # 📄 文档服务 (Port: 8082)
│   │   └── src/main/java/com/finrag4j/document/
│   │       ├── controller/     # 文档、知识库控制器
│   │       ├── service/        # 文档上传、MinIO存储服务
│   │       ├── client/         # Python服务Feign客户端
│   │       └── mapper/         # 数据访问层
│   ├── finrag4j-search/        # 🔍 检索服务 (Port: 8083)
│   │   └── src/main/java/com/finrag4j/search/
│   │       ├── controller/     # RAG检索控制器
│   │       ├── service/        # 向量服务、Embedding服务
│   │       └── mapper/         # 数据访问层
│   ├── finrag4j-agent/         # 🤖 Agent服务 (Port: 8084)
│   │   └── src/main/java/com/finrag4j/agent/
│   │       ├── controller/     # 聊天、合规、抽取控制器
│   │       ├── service/        # RAG聊天、LLM、合规检查服务
│   │       └── mapper/         # 数据访问层
│   ├── finrag4j-common/       # 🔧 公共模块
│   │   └── src/main/java/com/finrag4j/common/
│   │       ├── Result.java     # 统一响应封装
│   │       ├── BusinessException.java  # 业务异常
│   │       ├── GlobalExceptionHandler.java  # 全局异常处理
│   │       ├── PageRequest.java   # 分页请求
│   │       └── PageResult.java    # 分页响应
│   └── finrag4j-python/        # 🐍 FastAPI Python预处理服务
│       ├── services/           # 文档解析/OCR/文本处理
│       └── main.py
├── frontend/                   # 🖥️ Vue3 管理后台
├── deploy/                     # 🚀 部署脚本
└── README.md
```

### 微服务架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                         前端 Vue3                                 │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    finrag4j-gateway (8080)                       │
│                    API网关 - 路由/限流/鉴权                        │
└─────────────────────────────────────────────────────────────────┘
         ↓               ↓                ↓                ↓
┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│   Auth      │ │  Document   │ │   Search    │ │   Agent     │
│  (8081)     │ │  (8082)     │ │  (8083)     │ │  (8084)     │
│             │ │             │ │             │ │             │
│ 用户/角色/   │ │ 文档上传/   │ │ 向量检索/   │ │ 智能问答/   │
│ 权限/RBAC   │ │ 存储/解析   │ │ 混合检索    │ │ 合规/抽取   │
└─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘
       ↓               ↓                ↓                ↓
┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│ PostgreSQL  │ │   MinIO     │ │  PGVector   │ │ PostgreSQL  │
│   + Redis   │ │             │ │   + Redis   │ │   + Redis   │
└─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘
                              ↓
                    ┌─────────────────┐
                    │  Python预处理   │
                    │  (OCR/解析)     │
                    └─────────────────┘
```

---

## 🚀 快速开始

### 环境要求

| 软件 | 版本要求 |
|------|---------|
| JDK | 21+ |
| Python | 3.12+ |
| Docker | 24.0+ |
| Docker Compose | 2.20+ |

### 一键部署（推荐）

```bash
# 1. 克隆项目
git clone https://github.com/finrag4j/finrag4j.git
cd finrag4j

# 2. 启动所有服务
cd deploy
docker-compose up -d

# 3. 查看服务状态
docker-compose ps
```

### 访问地址

| 服务 | 地址 |
|------|------|
| 前端界面 | http://localhost |
| API文档 | http://localhost:8080/doc.html |
| MinIO控制台 | http://localhost:9001 |

### 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |

---

## 📖 功能演示

### 1. 知识库管理

创建知识库，上传文档，系统自动解析、分块、向量化：

```
创建知识库 → 上传文档 → 自动解析 → 向量化入库 → 就绪
```

### 2. RAG智能问答

基于知识库的智能问答，回答携带来源引用：

```
用户提问 → 向量检索 → 结果重排 → 大模型生成 → 返回答案+来源
```

### 3. 监管合规自查（商业版）

自动匹配监管条款，识别风险点，生成整改报告：

```
上传业务文档 → 匹配监管条款 → 风险分级 → 生成报告 → 人工复核
```

### 4. 工作流编排（商业版）

可视化拖拽编排业务流程：

```
拖拽节点 → 连接流程 → 配置参数 → 触发执行 → 监控日志
```

---

## 📚 部署指南

### 单机部署

详见 [单机快速部署手册](docs/20_单机快速部署手册.md)

### K8s集群部署

详见 [K8s部署配置](deploy/k8s/finrag4j-deployment.yaml)

### 信创国产化部署

详见 [信创国产化部署手册](docs/21_信创国产化部署手册.md)

### 离线部署

```bash
cd deploy/offline-package
./build-offline-package.sh
```

---

## 📖 文档目录

| 文档 | 说明 |
|------|------|
| [产品可行性分析](docs/1_产品可行性分析.md) | 市场分析、需求痛点 |
| [产品规划方案](docs/2_产品规划方案.md) | 产品定位、功能规划 |
| [需求分析文档](docs/3_需求分析文档.md) | 详细需求说明 |
| [概要设计文档](docs/4_概要设计文档.md) | 系统架构设计 |
| [详细设计文档](docs/5_详细设计文档.md) | 模块详细设计 |
| [Java基座架构说明](docs/15_Java基座架构说明.md) | Java服务架构 |
| [RAG知识库业务说明](docs/16_RAG知识库业务说明.md) | RAG业务流程 |
| [企业版核心功能说明](docs/17_企业版核心功能说明.md) | 商业版功能 |
| [产品需求文档PRD](docs/18_产品需求文档PRD.md) | 完整需求清单 |
| [API接口汇总文档](docs/19_API接口汇总文档.md) | 全部接口文档 |
| [开源版vs商业版功能对比](docs/23_开源版vs商业版功能对比.md) | 版本对比 |

---

## 💰 商业咨询

### 开源版 vs 商业版

| 功能 | 开源版 | 商业版 |
|------|:------:|:------:|
| 知识库管理 | ✅ | ✅ |
| RAG智能问答 | ✅ | ✅ |
| 多租户隔离 | ❌ | ✅ |
| 全链路审计 | ❌ | ✅ |
| 金融Agent | ❌ | ✅ |
| 工作流引擎 | ❌ | ✅ |
| SSO单点登录 | ❌ | ✅ |
| 企业级支持 | ❌ | ✅ |

详见 [功能对比文档](docs/23_开源版vs商业版功能对比.md)

### 联系我们

- 📧 邮箱：wangjn1130@163.com
- 💬 微信：FinRag4j
- 🌐 官网：https://finrag4j.com

---

## 🤝 贡献指南

欢迎参与项目贡献！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

---

## 📄 许可证

本项目采用 **Apache 2.0** 开源协议，详见 [LICENSE](LICENSE) 文件。

---

## ⭐ Star History

如果这个项目对您有帮助，请给我们一个 Star ⭐

[![Star History Chart](https://api.star-history.com/svg?repos=finrag4j/finrag4j&type=Date)](https://star-history.com/#finrag4j/finrag4j&Date)

---

<div align="center">

**FinRag4j - 让金融大模型应用更简单**

Made with ❤️ by FinRag4j Team

</div>