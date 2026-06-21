"""
FinRag4j Python 预处理微服务配置文件

配置说明：
- 所有配置项均可通过环境变量覆盖
- 优先从 Nacos 配置中心获取配置
- OCR模型首次运行会自动下载
- 分块参数可根据实际业务需求调整
"""

import os
import json
from dotenv import load_dotenv
from loguru import logger
from pathlib import Path

# 加载环境变量（优先使用当前目录的 .env 文件，不覆盖已存在的环境变量）
env_file = Path(__file__).resolve().parent / ".env"
if env_file.exists():
    load_dotenv(env_file, override=False)

# ---------------------------
# Nacos 配置中心配置
# ---------------------------
NACOS_ENABLED = os.getenv("NACOS_ENABLED", "true").lower() == "true"
NACOS_HOST = os.getenv("NACOS_HOST", "localhost")
NACOS_PORT = int(os.getenv("NACOS_PORT", 8848))
NACOS_NAMESPACE = os.getenv("NACOS_NAMESPACE", "public")
NACOS_GROUP = os.getenv("NACOS_GROUP", "DEFAULT_GROUP")
NACOS_DATA_ID = os.getenv("NACOS_DATA_ID", "finrag4j-python.yml")
NACOS_TIMEOUT = int(os.getenv("NACOS_TIMEOUT", 30))
NACOS_USERNAME = os.getenv("NACOS_USERNAME", "nacos")
NACOS_PASSWORD = os.getenv("NACOS_PASSWORD", "nacos")

# ---------------------------
# Nacos 服务注册配置
# ---------------------------
NACOS_SERVICE_NAME = os.getenv("NACOS_SERVICE_NAME", "finrag4j-python")
NACOS_SERVICE_GROUP = os.getenv("NACOS_SERVICE_GROUP", "DEFAULT_GROUP")
NACOS_SERVICE_WEIGHT = float(os.getenv("NACOS_SERVICE_WEIGHT", "1.0"))
NACOS_SERVICE_CLUSTER = os.getenv("NACOS_SERVICE_CLUSTER", "DEFAULT")
NACOS_SERVICE_EPHEMERAL = os.getenv("NACOS_SERVICE_EPHEMERAL", "true").lower() == "true"

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


def load_config_from_nacos():
    """
    从 Nacos 配置中心加载配置（使用官方 Python SDK v3.x）
    :return: 配置字典，如果加载失败返回空字典
    """
    if not NACOS_ENABLED:
        logger.info("Nacos 配置中心已禁用，使用本地配置")
        return {}

    return {}  # 配置加载移到 lifespan 中处理


async def load_config_from_nacos_async():
    """
    异步从 Nacos 配置中心加载配置
    :return: 配置字典，如果加载失败返回空字典
    """
    if not NACOS_ENABLED:
        logger.info("Nacos 配置中心已禁用，使用本地配置")
        return {}

    try:
        from v2.nacos import NacosConfigService, ClientConfigBuilder, GRPCConfig, ConfigParam
    except ImportError:
        logger.warning("未安装 nacos-sdk-python，跳过 Nacos 配置加载。请运行: pip install nacos-sdk-python")
        return {}

    try:
        logger.info(f"正在连接 Nacos 配置中心: {NACOS_HOST}:{NACOS_PORT} (用户: {NACOS_USERNAME})")
        
        # Nacos 3.x 客户端使用主端口 8848，SDK 自动计算 gRPC 端口
        client_config = (ClientConfigBuilder()
            .server_address(f"{NACOS_HOST}:{NACOS_PORT}")
            .username(NACOS_USERNAME)
            .password(NACOS_PASSWORD)
            .namespace_id(NACOS_NAMESPACE)
            .log_level('WARNING')
            .grpc_config(GRPCConfig(grpc_timeout=NACOS_TIMEOUT * 1000))
            .build())

        config_client = await NacosConfigService.create_config_service(client_config)

        content = await config_client.get_config(ConfigParam(
            data_id=NACOS_DATA_ID,
            group=NACOS_GROUP
        ))

        if content:
            logger.info(f"成功从 Nacos 加载配置: {NACOS_DATA_ID}")
            try:
                return json.loads(content)
            except json.JSONDecodeError:
                # 如果不是JSON格式，尝试按行解析
                config_dict = {}
                for line in content.split('\n'):
                    line = line.strip()
                    if line and '=' in line and not line.startswith('#'):
                        key, value = line.split('=', 1)
                        config_dict[key.strip()] = value.strip()
                return config_dict
        else:
            logger.warning(f"从 Nacos 获取配置为空: {NACOS_DATA_ID}")
            return {}

    except Exception as e:
        logger.warning(f"从 Nacos 加载配置失败: {e}")
        return {}


def merge_nacos_config(nacos_config: dict = None):
    """
    合并 Nacos 配置到全局变量
    :param nacos_config: 可选的配置字典，如果不提供则从 Nacos 加载
    """
    global SERVER_PORT, SERVER_HOST, DEBUG_MODE
    global OCR_MODEL_DIR, OCR_LANG, OCR_USE_GPU, OCR_TIMEOUT
    global DEFAULT_CHUNK_SIZE, DEFAULT_CHUNK_OVERLAP, MIN_CHUNK_SIZE, MAX_CHUNK_SIZE
    global MAX_FILE_SIZE, LOG_LEVEL, LOG_FILE
    
    if not NACOS_ENABLED:
        return
    
    # 如果没有提供配置，则从 Nacos 加载
    if nacos_config is None:
        nacos_config = load_config_from_nacos()
    
    if not nacos_config:
        return
    
    # 合并配置（Nacos配置优先）
    if 'server.port' in nacos_config:
        SERVER_PORT = int(nacos_config['server.port'])
    if 'server.host' in nacos_config:
        SERVER_HOST = nacos_config['server.host']
    if 'debug.mode' in nacos_config:
        DEBUG_MODE = nacos_config['debug.mode'].lower() == "true"
    
    if 'ocr.model.dir' in nacos_config:
        OCR_MODEL_DIR = nacos_config['ocr.model.dir']
    if 'ocr.lang' in nacos_config:
        OCR_LANG = nacos_config['ocr.lang']
    if 'ocr.use.gpu' in nacos_config:
        OCR_USE_GPU = nacos_config['ocr.use.gpu'].lower() == "true"
    if 'ocr.timeout' in nacos_config:
        OCR_TIMEOUT = int(nacos_config['ocr.timeout'])
    
    if 'chunk.default.size' in nacos_config:
        DEFAULT_CHUNK_SIZE = int(nacos_config['chunk.default.size'])
    if 'chunk.default.overlap' in nacos_config:
        DEFAULT_CHUNK_OVERLAP = int(nacos_config['chunk.default.overlap'])
    if 'chunk.min.size' in nacos_config:
        MIN_CHUNK_SIZE = int(nacos_config['chunk.min.size'])
    if 'chunk.max.size' in nacos_config:
        MAX_CHUNK_SIZE = int(nacos_config['chunk.max.size'])
    
    if 'file.max.size' in nacos_config:
        MAX_FILE_SIZE = int(nacos_config['file.max.size'])
    
    if 'log.level' in nacos_config:
        LOG_LEVEL = nacos_config['log.level']
    if 'log.file' in nacos_config:
        LOG_FILE = nacos_config['log.file']
    
    logger.info("Nacos 配置已合并到全局变量")