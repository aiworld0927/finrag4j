# FinRag4j Python 预处理微服务

## 项目简介

FinRag4j Python 预处理微服务是 FinRag4j 项目的重要组成部分，专注于提供文档解析、OCR识别和文本分块等预处理能力。

## 功能特性

- **文档解析**：支持 PDF、Word、Excel、TXT、PPT 等多种格式文档解析
- **离线OCR**：基于 PaddleOCR 的离线中文文字识别
- **图像预处理**：图像矫正、去噪、增强等预处理能力
- **文本分块**：支持监管文件、信贷合同、内部通知三种金融专属分块策略
- **Nacos适配**：支持从 Nacos 配置中心获取配置

---

## 运行依赖

### 基础环境

| 依赖 | 版本要求 | 说明 |
|------|---------|------|
| Python | 3.12+ | 运行环境 |
| uv | 0.1+ | 依赖管理工具 |

### Python 依赖包

#### 基础框架依赖
| 包名 | 版本 | 说明 |
|------|------|------|
| fastapi | >=0.110.0 | Web 框架 |
| uvicorn | >=0.29.0 | ASGI 服务器 |
| python-multipart | >=0.0.9 | 文件上传支持 |
| pydantic | >=2.6.4 | 数据验证 |
| requests | >=2.31.0 | HTTP 客户端 |
| python-dotenv | >=1.0.1 | 环境变量加载 |
| loguru | >=0.7.2 | 日志处理 |

#### 配置中心依赖
| 包名 | 版本 | 说明 |
|------|------|------|
| nacos-sdk-python | >=1.4.0 | Nacos 配置中心客户端 |

#### 文档解析依赖
| 包名 | 版本 | 说明 |
|------|------|------|
| python-docx | >=1.1.0 | Word 文档解析 |
| openpyxl | >=3.1.2 | Excel 文档解析 |
| python-pptx | >=0.6.23 | PPT 文档解析 |
| PyPDF2 | >=3.0.1 | PDF 文档解析 |
| unstructured | >=0.14.3 | 通用文档解析 |

#### OCR 与图像处理依赖
| 包名 | 版本 | 说明 |
|------|------|------|
| paddlepaddle | >=2.6.1 | 深度学习框架 |
| paddleocr | >=2.8.2 | OCR 识别引擎 |
| opencv-python | >=4.9.0 | 图像处理 |
| pillow | >=10.2.0 | 图像处理 |

#### 文本处理依赖
| 包名 | 版本 | 说明 |
|------|------|------|
| jieba | >=0.42.1 | 中文分词 |
| nltk | >=3.8.1 | 自然语言处理 |
| regex | >=2023.12.25 | 正则表达式增强 |

#### 开发依赖
| 包名 | 版本 | 说明 |
|------|------|------|
| pytest | >=7.4.0 | 测试框架 |
| pytest-asyncio | >=0.21.0 | 异步测试支持 |
| httpx | >=0.27.0 | HTTP 测试客户端 |
| black | >=23.10.0 | 代码格式化 |
| isort | >=5.12.0 | 导入排序 |
| flake8 | >=6.1.0 | 代码检查 |

---

## 中间件依赖

### 必需中间件

| 中间件 | 版本 | 说明 | 默认端口 |
|--------|------|------|---------|
| Nacos | 2.2.0+ | 服务注册与配置中心 | 8848 |

### 可选中间件

| 中间件 | 版本 | 说明 | 默认端口 |
|--------|------|------|---------|
| PostgreSQL | 16+ | 数据库（如需持久化） | 5432 |
| Redis | 7.x | 缓存（如需缓存） | 6379 |

### 架构关系

```
┌─────────────────────────────────────────────────────────────┐
│                    finrag4j-python (8001)                    │
│                     Python 预处理服务                         │
└─────────────────────────────────────────────────────────────┘
                              │
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      Nacos (8848)                            │
│                    配置中心（可选）                            │
└─────────────────────────────────────────────────────────────┘
                              │
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                   Java 微服务集群                             │
│     Gateway / Auth / Document / Search / Agent               │
└─────────────────────────────────────────────────────────────┘
```

---

## 技术栈

- **框架**: FastAPI + Uvicorn
- **OCR**: PaddleOCR
- **文档解析**: python-docx, openpyxl, PyPDF2, unstructured
- **图像处理**: OpenCV-Python, Pillow
- **文本处理**: jieba, nltk
- **配置中心**: nacos-sdk-python
- **依赖管理**: uv

---

## 快速开始

### 环境要求

- Python 3.12+
- uv 0.1+

### 安装依赖

```bash
# 进入 Python 服务目录
cd backend/finrag4j-python

# 使用 uv 安装依赖到 .venv 目录
uv sync

# 激活虚拟环境
source .venv/bin/activate  # Linux/Mac
.venv\Scripts\activate     # Windows
```

### 启动服务

```bash
# 开发模式（使用 .venv 中的依赖）
uv run python main.py

# 或激活虚拟环境后运行
python main.py

# 生产模式
uvicorn main:app --host 0.0.0.0 --port 8001
```

### 使用 Docker

```bash
docker build -t finrag4j-python .
docker run -p 8001:8001 finrag4j-python
```

---

## API 接口文档

**接口文档地址**: `http://localhost:8001/docs` (Swagger UI)

**备用文档地址**: `http://localhost:8001/redoc` (ReDoc)

---

### 1. 健康检查

**路径**: `GET /health`

**描述**: 服务健康心跳检测，适配 Nacos 健康巡检

**响应示例**:
```json
{
    "status": "healthy",
    "service": "finrag4j-python",
    "timestamp": "2024-01-15T10:30:00",
    "version": "1.0.0",
    "message": "服务运行正常",
    "nacos_enabled": true,
    "nacos_host": "nacos",
    "nacos_port": 8848
}
```

---

### 2. 文档解析

**路径**: `POST /api/parse/file`

**描述**: 通用文档解析接口，支持 PDF/Word/Excel/TXT

**Content-Type**: `multipart/form-data`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 上传的文件 |
| file_type | string | 否 | 文件类型（pdf/doc/docx/xls/xlsx/txt），自动检测 |

**响应示例**:
```json
{
    "success": true,
    "file_type": "pdf",
    "text": "文档的纯文本内容...",
    "page_count": 10,
    "message": "解析成功"
}
```

---

### 3. OCR识别

**路径**: `POST /api/parse/ocr`

**描述**: 离线 PaddleOCR 识别，支持图片/扫描PDF

**Content-Type**: `multipart/form-data`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 上传的图片或PDF文件 |
| preprocess | boolean | 否 | 是否进行图像预处理（默认true） |

**响应示例**:
```json
{
    "success": true,
    "text": "识别的完整文本内容...",
    "confidence": 0.95,
    "page_count": 5,
    "message": "OCR识别成功"
}
```

---

### 4. 文本清洗

**路径**: `POST /api/text/clean`

**描述**: 金融专属文本清洗接口

**Content-Type**: `application/json`

**请求参数**:
```json
{
    "text": "待清洗的文本内容",
    "remove_header_footer": true,
    "remove_watermark": true,
    "remove_empty_lines": true,
    "remove_junk_text": true
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| text | string | 是 | 待清洗的文本内容 |
| remove_header_footer | boolean | 否 | 是否去除页眉页脚（默认true） |
| remove_watermark | boolean | 否 | 是否去除水印（默认true） |
| remove_empty_lines | boolean | 否 | 是否去除冗余空行（默认true） |
| remove_junk_text | boolean | 否 | 是否去除制式垃圾文本（默认true） |

**响应示例**:
```json
{
    "success": true,
    "text": "原始文本前200字符...",
    "cleaned_text": "清洗后的文本内容...",
    "cleaned_length": 1500,
    "original_length": 2000,
    "message": "清洗成功"
}
```

---

### 5. 文本分块

**路径**: `POST /api/text/split`

**描述**: 金融三策略智能分块接口

**Content-Type**: `application/json`

**请求参数**:
```json
{
    "text": "待分块的文本内容",
    "strategy": "regulatory",
    "chunk_size": null,
    "chunk_overlap": null
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| text | string | 是 | 待分块的文本内容 |
| strategy | string | 否 | 分块策略（默认regulatory） |
| chunk_size | integer | 否 | 自定义分块大小 |
| chunk_overlap | integer | 否 | 自定义重叠度 |

**分块策略说明**:
| 策略 | 说明 | 默认块大小 | 默认重叠 |
|------|------|-----------|----------|
| regulatory | 监管文件策略（法律法规、监管通知） | 600字符 | 150字符 |
| credit | 信贷合同策略（贷款合同、担保协议） | 800字符 | 200字符 |
| official | 公文策略（内部公告、通知） | 400字符 | 80字符 |

**响应示例**:
```json
{
    "success": true,
    "chunks": [
        {
            "content": "第一块内容...",
            "chunk_index": 0,
            "start_pos": 0,
            "end_pos": 600
        },
        {
            "content": "第二块内容...",
            "chunk_index": 1,
            "start_pos": 450,
            "end_pos": 1050
        }
    ],
    "strategy": "regulatory",
    "total_chunks": 5,
    "message": "分块成功"
}
```

---

## 配置说明

### 环境变量

配置文件 `config.py` 支持以下环境变量：

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| SERVER_PORT | 服务端口 | 8001 |
| SERVER_HOST | 服务地址 | 0.0.0.0 |
| DEBUG_MODE | 调试模式 | false |
| MAX_FILE_SIZE | 最大文件大小（字节） | 52428800 (50MB) |
| OCR_USE_GPU | 是否使用GPU | false |
| OCR_LANG | OCR语言 | ch |
| OCR_TIMEOUT | OCR超时时间（秒） | 120 |
| LOG_LEVEL | 日志级别 | INFO |
| NACOS_ENABLED | 是否启用Nacos | true |
| NACOS_HOST | Nacos服务地址 | localhost |
| NACOS_PORT | Nacos服务端口 | 8848 |
| NACOS_NAMESPACE | Nacos命名空间 | public |

### Nacos 配置中心

Python 服务支持从 Nacos 动态获取配置，配置 DataId 为 `finrag4j-python.yml`

## 项目结构

```
finrag4j-python/
├── main.py                  # 服务入口
├── config.py                # 配置文件（Nacos适配）
├── pyproject.toml           # uv 项目配置
├── uv.lock                  # uv 依赖锁文件
├── README.md                # 接口文档
├── .venv/                   # 本地虚拟环境（uv sync 自动创建）
├── services/                # 服务模块
│   ├── document_parser.py   # 文档解析服务
│   ├── ocr_service.py      # OCR识别服务
│   ├── text_chunker.py     # 文本分块服务
│   └── text_cleaner.py     # 文本清洗服务
└── utils/                   # 工具包
    ├── image_processor.py   # 图像预处理工具
    └── text_cleaner.py      # 文本清洗工具
```

## 许可证

Apache 2.0 License
