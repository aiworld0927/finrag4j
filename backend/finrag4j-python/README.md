# FinRag4j Python 预处理微服务

## 项目简介

FinRag4j Python 预处理微服务是 FinRag4j 项目的重要组成部分，专注于提供文档解析、OCR识别和文本分块等预处理能力。

## 功能特性

- **文档解析**：支持 PDF、Word、Excel、TXT、PPT 等多种格式文档解析
- **离线OCR**：基于 PaddleOCR 的离线中文文字识别
- **图像预处理**：图像矫正、去噪、增强等预处理能力
- **文本分块**：支持监管文件、信贷合同、内部通知三种金融专属分块策略

## 技术栈

- **框架**: FastAPI + Uvicorn
- **OCR**: PaddleOCR
- **文档解析**: python-docx, openpyxl, PyPDF2, unstructured
- **图像处理**: OpenCV-Python, Pillow
- **文本处理**: jieba, nltk
- **依赖管理**: uv

## 快速开始

### 环境要求

- Python 3.10+
- uv 0.1+

### 安装依赖

```bash
# 使用 uv 安装依赖
uv sync

# 激活虚拟环境
source .venv/bin/activate  # Linux/Mac
.venv\Scripts\activate     # Windows
```

### 启动服务

```bash
# 开发模式
uv run python main.py

# 生产模式
uvicorn main:app --host 0.0.0.0 --port 8001
```

### 使用 Docker

```bash
docker build -t finrag4j-python .
docker run -p 8001:8001 finrag4j-python
```

## API 接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/health` | GET | 健康检查 |
| `/api/parse` | POST | 文档解析 |
| `/api/ocr` | POST | OCR识别 |
| `/api/chunk` | POST | 文本分块 |
| `/api/chunk/strategies` | GET | 获取分块策略 |

## 项目结构

```
finrag4j-python/
├── main.py              # 服务入口
├── config.py            # 配置文件
├── pyproject.toml       # uv 项目配置
├── uv.lock              # uv 依赖锁文件
├── requirements.txt     # 兼容 pip 的依赖列表
├── utils/               # 工具包
│   ├── text_cleaner.py  # 文本清洗工具
│   └── image_processor.py # 图像预处理工具
└── services/            # 服务模块
    ├── document_parser.py  # 文档解析服务
    ├── ocr_service.py      # OCR识别服务
    └── text_chunker.py     # 文本分块服务
```

## 配置说明

配置文件 `config.py` 支持以下环境变量：

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| SERVER_PORT | 服务端口 | 8001 |
| DEBUG_MODE | 调试模式 | false |
| OCR_USE_GPU | 是否使用GPU | false |
| LOG_LEVEL | 日志级别 | INFO |

## 许可证

Apache 2.0 License