# Python 模块依赖分析

## 文档版本
- 版本：v1.0
- 创建日期：2026-06-19

---

## 1. 项目概述

finrag4j-python 是 FinRag4j 系统的 Python 预处理微服务，负责：
- 文档解析（PDF、Word、Excel、PPT、TXT）
- OCR 文字识别
- 文本清洗
- 文本分块

---

## 2. 直接依赖清单

### 2.1 基础框架依赖

| 包名 | 版本 | 说明 | 必需 |
|------|------|------|------|
| fastapi | >=0.110.0 | Web 框架 | ✅ |
| uvicorn | >=0.29.0 | ASGI 服务器 | ✅ |
| python-multipart | >=0.0.9 | 文件上传支持 | ✅ |
| pydantic | >=2.6.4 | 数据验证 | ✅ |
| requests | >=2.31.0 | HTTP 客户端 | ✅ |
| python-dotenv | >=1.0.1 | 环境变量加载 | ✅ |
| loguru | >=0.7.2 | 日志处理 | ✅ |

### 2.2 配置中心依赖

| 包名 | 版本 | 说明 | 必需 |
|------|------|------|------|
| nacos-sdk-python | >=1.4.0 | Nacos 配置中心客户端 | ✅ |

### 2.3 文档解析依赖

| 包名 | 版本 | 说明 | 必需 |
|------|------|------|------|
| python-docx | >=1.1.0 | Word 文档解析 | ✅ |
| openpyxl | >=3.1.2 | Excel 文档解析 | ✅ |
| python-pptx | >=0.6.23 | PPT 文档解析 | ✅ |
| PyPDF2 | >=3.0.1 | PDF 文档解析 | ✅ |

### 2.4 OCR 与图像处理依赖

| 包名 | 版本 | 说明 | 必需 |
|------|------|------|------|
| paddlepaddle | >=2.6.1 | 深度学习框架 | ⚠️ |
| paddleocr | >=2.8.2 | OCR 识别引擎 | ⚠️ |
| opencv-python | >=4.9.0 | 图像处理 | ✅ |
| pillow | >=10.2.0 | 图像处理 | ✅ |

### 2.5 文本处理依赖

| 包名 | 版本 | 说明 | 必需 |
|------|------|------|------|
| jieba | >=0.42.1 | 中文分词 | ✅ |
| nltk | >=3.8.1 | 自然语言处理 | ✅ |
| regex | >=2023.12.25 | 正则表达式 | ✅ |

---

## 3. 依赖关系图

```
finrag4j-python
├── FastAPI (Web框架)
│   ├── starlette
│   ├── pydantic
│   └── python-multipart
├── Uvicorn (ASGI服务器)
│   ├── httptools
│   └── uvloop
├── 日志模块
│   └── loguru
├── HTTP客户端
│   └── requests
├── 配置中心
│   └── nacos-sdk-python
│       ├── alibabacloud-tea
│       └── grpc
├── 文档解析
│   ├── python-docx (→ lxml, python-pptx)
│   ├── openpyxl (→ lxml, et_xmlfile)
│   ├── python-pptx (→ lxml, Pillow)
│   └── PyPDF2 (→ pypdf)
├── OCR识别
│   ├── paddlepaddle
│   │   ├── numpy
│   │   └── protobuf
│   ├── paddleocr
│   │   ├── paddlepaddle
│   │   ├── shapely
│   │   └── pyclipper
│   └── opencv-python
│       └── numpy
├── 图像处理
│   └── pillow (→ numpy)
└── 文本处理
    ├── jieba
    ├── nltk
    └── regex
```

---

## 4. 传递依赖统计

使用 `uv tree` 命令分析（部分）：

```
finrag4j-python v1.0.0
├── fastapi v0.137.2
│   ├── pydantic v2.13.4
│   │   ├── annotated-types v0.7.0
│   │   └── typing-extensions v4.15.0
│   ├── starlette v1.3.1
│   │   ├── anyio v4.14.0
│   │   └── typing-extensions v4.15.0
│   └── python-multipart v0.0.32
├── uvicorn v0.49.0
│   ├── click v8.4.1
│   ├── h11 v0.16.0
│   └── httptools v0.7.0
├── paddleocr v3.7.0
│   ├── paddlepaddle v3.3.1
│   │   ├── numpy v2.3.5
│   │   └── protobuf v7.35.1
│   └── shapely v2.1.2
├── opencv-python v4.13.0.92
│   └── numpy v2.3.5
└── jieba v0.42.1
```

---

## 5. 依赖版本兼容性矩阵

| 依赖包 | 最低版本 | 推荐版本 | 最高兼容版本 | 注意事项 |
|--------|---------|---------|-------------|---------|
| Python | 3.10 | 3.12 | 3.12 | 需要 3.12+ 特性支持 |
| fastapi | 0.110.0 | 0.137.x | 0.140.x | 建议使用最新稳定版 |
| paddlepaddle | 2.6.1 | 3.0.x | 3.3.x | oneDNN 版本有兼容性问题 |
| paddleocr | 2.8.2 | 3.7.x | 3.7.x | 需与 paddlepaddle 版本匹配 |
| opencv | 4.9.0 | 4.10.x | 4.13.x | Windows 下可能需要编译 |

---

## 6. 环境要求

### 6.1 系统要求

| 项目 | 最低要求 | 推荐配置 |
|------|---------|---------|
| CPU | 2 核 | 4 核+ |
| 内存 | 4 GB | 8 GB+ |
| 硬盘 | 5 GB 可用 | 10 GB+ |
| 操作系统 | Windows 10+ / Linux | Windows 11 / Ubuntu 22.04 |

### 6.2 软件要求

| 软件 | 版本 | 说明 |
|------|------|------|
| Python | 3.12+ | 必须 |
| uv | 0.10+ | 推荐使用 uv 管理依赖 |
| Git | 2.0+ | 代码管理 |

### 6.3 OCR GPU 支持（可选）

如需 GPU 加速：
- CUDA: 11.2+ 或 12.x
- cuDNN: 8.x+
- GPU 显存: 4 GB+

---

## 7. 依赖安装指南

### 7.1 使用 uv 安装（推荐）

```bash
# 进入项目目录
cd backend/finrag4j-python

# 安装所有依赖
uv sync

# 添加新依赖
uv add <package-name>

# 添加开发依赖
uv add --dev <package-name>
```

### 7.2 镜像源配置

如遇安装缓慢，可配置国内镜像源：

```bash
# 临时使用镜像
uv pip install <package> --index https://pypi.tuna.tsinghua.edu.cn/simple

# 或设置全局镜像
uv pip config set global.index-url https://pypi.tuna.tsinghua.edu.cn/simple
```

### 7.3 常见问题

**问题1：安装超时**
```
Solution: 配置镜像源或增加超时时间
uv sync --no-editable --refresh
```

**问题2：编译失败**
```
Solution: 安装 Visual C++ Build Tools (Windows)
或使用预编译的 wheel 包
```

**问题3：版本冲突**
```
Solution: 使用 uv lock 锁定版本
uv lock
uv sync --locked
```

---

## 8. 依赖大小估算

| 分类 | 依赖数量 | 预计大小 |
|------|---------|---------|
| 基础框架 | ~20 | ~50 MB |
| 文档解析 | ~30 | ~100 MB |
| OCR 处理 | ~50 | ~500 MB |
| 文本处理 | ~10 | ~50 MB |
| **总计** | **~110** | **~700 MB** |

> 注：OCR 相关依赖（主要是 PaddlePaddle）占用空间较大。

---

## 9. 可选替代方案

### 9.1 OCR 替代方案

| 方案 | 优点 | 缺点 |
|------|------|------|
| EasyOCR | 轻量、跨平台 | 识别速度较慢 |
| Tesseract | 成熟稳定 | 中文支持一般 |
| PaddleOCR (当前) | 中文识别好 | 依赖较大 |

### 9.2 文档解析替代方案

| 方案 | 优点 | 缺点 |
|------|------|------|
| unstructured (已移除) | 功能全面 | 依赖过多 |
| PyPDF2 + python-docx (当前) | 轻量 | 功能有限 |
| pymupdf | PDF 处理强 | 商业许可 |

---

## 10. 依赖更新策略

1. **定期更新**：每月检查一次依赖更新
2. **安全优先**：优先更新安全相关依赖
3. **测试验证**：更新后运行测试确保兼容性
4. **版本锁定**：生产环境使用 `uv.lock` 锁定版本

```bash
# 检查可更新的包
uv pip list --outdated

# 更新所有包
uv pip compile --upgrade -o requirements.txt

# 更新特定包
uv add --upgrade <package>
```
