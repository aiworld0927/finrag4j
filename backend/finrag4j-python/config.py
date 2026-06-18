"""
FinRag4j Python 预处理微服务配置文件

配置说明：
- 所有配置项均可通过环境变量覆盖
- OCR模型首次运行会自动下载
- 分块参数可根据实际业务需求调整
"""

import os
from dotenv import load_dotenv

# 加载环境变量
load_dotenv()

# ---------------------------
# 服务基础配置
# ---------------------------
SERVER_PORT = int(os.getenv("SERVER_PORT", 8001))
SERVER_HOST = os.getenv("SERVER_HOST", "0.0.0.0")
DEBUG_MODE = os.getenv("DEBUG_MODE", "false").lower() == "true"

# ---------------------------
# OCR配置
# ---------------------------
# PaddleOCR模型存储路径
OCR_MODEL_DIR = os.getenv("OCR_MODEL_DIR", "./models/ocr")

# OCR语言配置（支持: ch, en, fr, german, japan, korean）
OCR_LANG = os.getenv("OCR_LANG", "ch")

# 是否使用GPU加速（需要安装paddlepaddle-gpu）
OCR_USE_GPU = os.getenv("OCR_USE_GPU", "false").lower() == "true"

# OCR检测超时时间（秒）
OCR_TIMEOUT = int(os.getenv("OCR_TIMEOUT", 120))

# ---------------------------
# 文本分块配置
# ---------------------------
# 默认分块大小（字符数）
DEFAULT_CHUNK_SIZE = int(os.getenv("DEFAULT_CHUNK_SIZE", 500))

# 默认重叠大小（字符数）
DEFAULT_CHUNK_OVERLAP = int(os.getenv("DEFAULT_CHUNK_OVERLAP", 100))

# 最小分块大小（字符数）
MIN_CHUNK_SIZE = int(os.getenv("MIN_CHUNK_SIZE", 100))

# 最大分块大小（字符数）
MAX_CHUNK_SIZE = int(os.getenv("MAX_CHUNK_SIZE", 2000))

# ---------------------------
# 文件处理配置
# ---------------------------
# 临时文件存储目录
TEMP_DIR = os.getenv("TEMP_DIR", "./temp")

# 支持的文件类型
SUPPORTED_FILE_TYPES = {
    "pdf": ["application/pdf", ".pdf"],
    "doc": ["application/msword", ".doc"],
    "docx": ["application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx"],
    "xls": ["application/vnd.ms-excel", ".xls"],
    "xlsx": ["application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx"],
    "txt": ["text/plain", ".txt"],
    "jpg": ["image/jpeg", ".jpg", ".jpeg"],
    "png": ["image/png", ".png"],
    "bmp": ["image/bmp", ".bmp"],
    "tiff": ["image/tiff", ".tiff"],
}

# 最大文件大小限制（字节）- 50MB
MAX_FILE_SIZE = int(os.getenv("MAX_FILE_SIZE", 50 * 1024 * 1024))

# ---------------------------
# 日志配置
# ---------------------------
LOG_LEVEL = os.getenv("LOG_LEVEL", "INFO")
LOG_FILE = os.getenv("LOG_FILE", "./logs/app.log")
LOG_ROTATION = os.getenv("LOG_ROTATION", "1 week")
LOG_RETENTION = os.getenv("LOG_RETENTION", "1 month")

# ---------------------------
# 分块策略配置
# ---------------------------
# 监管文件分块策略
REGULATORY_CHUNK_CONFIG = {
    "chunk_size": 600,
    "chunk_overlap": 150,
    "priority_sections": ["第一章", "第二章", "第三章", "第一节", "第二节", "第三条", "第四条"],
}

# 信贷合同分块策略
CREDIT_CHUNK_CONFIG = {
    "chunk_size": 800,
    "chunk_overlap": 200,
    "priority_sections": ["第一条", "第二条", "第三条", "一、", "二、", "三、", "甲方", "乙方", "违约责任"],
}

# 公文分块策略
OFFICIAL_CHUNK_CONFIG = {
    "chunk_size": 400,
    "chunk_overlap": 80,
    "priority_sections": ["通知", "要求", "决定", "事项", "时间", "地点", "联系人"],
}

# ---------------------------
# 文本清洗配置
# ---------------------------
# 是否去除多余空白
CLEAN_REMOVE_WHITESPACE = True

# 是否去除特殊字符
CLEAN_REMOVE_SPECIAL_CHARS = True

# 是否去除停用词
CLEAN_REMOVE_STOPWORDS = True

# 停用词文件路径
STOPWORDS_FILE = os.getenv("STOPWORDS_FILE", "./data/stopwords.txt")

# ---------------------------
# 图像预处理配置
# ---------------------------
# 是否启用图像矫正
IMAGE_ENABLE_CORRECTION = True

# 是否启用图像去噪
IMAGE_ENABLE_DENOISING = True

# 是否启用图像增强
IMAGE_ENABLE_ENHANCEMENT = True