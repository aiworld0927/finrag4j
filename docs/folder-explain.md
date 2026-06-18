# FinRag4j 文件夹用途说明

本文档详细说明 FinRag4j 项目中各个文件夹的用途和职责划分，帮助开发者快速理解项目结构。

---

## 项目根目录结构

```
FinRag4j/
├── docs/                    # 全套文档
├── backend/                 # 后端总目录
│   ├── finrag4j-java/       # Java主服务
│   └── finrag4j-python/     # Python预处理微服务
├── frontend/                # Vue3 管理后台前端
├── deploy/                  # 部署脚本
├── demo-data/               # 演示测试数据
├── script/                  # 辅助脚本
├── LICENSE                  # Apache2.0开源协议
├── README.md                # 项目总说明
└── .gitignore               # Git忽略配置
```

---

## 详细文件夹说明

### 1. docs/ - 文档目录

**用途**：存放项目全套文档，包括产品需求、技术设计、部署指南、接口文档等。

**子目录建议结构**：
- `PRD/` - 产品需求文档（产品功能说明、业务流程、原型设计）
- `deploy/` - 部署手册（环境要求、安装步骤、配置说明）
- `api/` - 接口文档（RESTful API规范、Swagger文档、接口测试用例）
- `xinchuang/` - 信创适配文档（国产环境适配说明、兼容性测试报告）
- `ppt/` - 演示PPT（产品介绍、技术架构、POC演示材料）
- `tech/` - 技术设计文档（架构设计、数据库设计、核心流程说明）

**文档管理规范**：
- 所有文档采用 Markdown 格式编写
- 文件名使用英文小写，单词之间用短横线分隔
- 重要文档需定期更新，保持与代码同步

---

### 2. backend/ - 后端总目录

**用途**：包含项目所有后端服务代码。

#### 2.1 backend/finrag4j-java/ - Java主服务

**职责**：
- 承载全部业务逻辑
- RAG检索与问答
- Agent流程编排
- 租户隔离与权限管理
- 审计日志与系统集成

**技术栈**：
- Spring Boot 3.2.x
- LangChain4j 0.30+
- Spring Security + RBAC
- MyBatis-Plus + PostgreSQL
- Redis Stack + RocketMQ + MinIO

**目录结构建议**：
```
finrag4j-java/
├── src/main/java/          # Java源代码
├── src/main/resources/     # 配置文件
├── src/test/java/          # 单元测试
├── pom.xml                 # Maven依赖管理
└── README.md               # 服务说明
```

#### 2.2 backend/finrag4j-python/ - Python预处理微服务

**职责**：
- 文档解析（PDF/Word/Excel等）
- OCR识别（PaddleOCR）
- 文本清洗与分块
- 金融文档特殊处理

**定位**：纯工具微服务，无数据库、无业务逻辑、不处理权限租户。

**技术栈**：
- FastAPI + Uvicorn
- unstructured, python-docx, openpyxl, Camelot-py
- PaddleOCR（离线OCR）
- OpenCV-Python（图像处理）
- jieba（中文分词）

**目录结构建议**：
```
finrag4j-python/
├── app/                    # 应用代码
├── requirements.txt        # Python依赖
├── main.py                 # 启动入口
└── README.md               # 服务说明
```

---

### 3. frontend/ - Vue3 管理后台前端

**职责**：
- 提供用户交互界面
- 管理后台功能
- 数据可视化展示

**技术栈**：
- Vue3 + Vite
- Element Plus
- Axios
- ECharts

**约束**：
- 仅对接 Java 后端接口
- 不直连 Python 服务

**目录结构建议**：
```
frontend/
├── src/                    # 源代码
├── public/                 # 静态资源
├── index.html              # HTML入口
├── package.json            # Node依赖
├── vite.config.js          # Vite配置
└── README.md               # 前端说明
```

---

### 4. deploy/ - 部署脚本

**用途**：存放项目部署相关的配置文件和脚本。

**子目录结构**：
- `docker-compose/` - Docker Compose 单机部署配置
- `k8s/` - Kubernetes 集群部署 YAML 文件
- `offline/` - 离线打包脚本（用于无网络环境部署）
- `xinchuang/` - 信创环境镜像构建脚本和配置

**文件规范**：
- Dockerfile 需支持多架构构建（amd64/arm64）
- 配置文件需区分开发、测试、生产环境
- 提供一键启动和停止脚本

---

### 5. demo-data/ - 演示测试数据

**用途**：存放演示和测试用的示例数据。

**内容类型**：
- 模拟监管文件（PDF格式）
- 信贷合同样本
- 金融产品说明文档
- 测试用配置文件

**注意事项**：
- 不存放敏感真实数据
- 数据仅用于功能演示和测试
- 可根据需要定期更新和扩展

---

### 6. script/ - 辅助脚本

**用途**：存放项目运维和开发辅助脚本。

**脚本类型**：
- 环境自检脚本（检查依赖服务是否就绪）
- 向量批量导入脚本（初始化向量数据库）
- 日志导出脚本（方便问题排查）
- 版本升级补丁脚本（数据库迁移、配置更新）

**脚本规范**：
- 使用 Shell 或 Python 编写
- 提供清晰的使用说明
- 脚本命名清晰，便于理解用途

---

## 开发约束总结

| 服务 | 职责范围 | 边界约束 |
|------|---------|---------|
| Java主服务 | 业务逻辑、RAG检索、Agent流程、权限、审计、集成 | 承载全部业务和租户隔离 |
| Python服务 | 文档解析、OCR识别、文本分块 | 纯工具服务，无业务逻辑 |
| 前端 | 用户界面、数据展示 | 仅对接Java后端 |

---

## 目录使用原则

1. **单一职责**：每个目录有明确的职责范围，不存放无关文件
2. **分层清晰**：按技术栈和功能模块划分，便于定位和维护
3. **文档先行**：重要功能和接口需先编写文档再开发
4. **环境隔离**：开发、测试、生产环境配置分离
5. **安全性**：敏感配置和密钥不提交到版本控制

---

**文档版本**：v1.0  
**最后更新**：2024年