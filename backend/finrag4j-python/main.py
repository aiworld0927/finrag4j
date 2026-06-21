"""
FinRag4j Python 预处理微服务入口文件

服务功能：
1. 健康检查（适配Nacos）
2. 文档解析（PDF/Word/Excel/TXT）
3. OCR识别（图片/扫描PDF）
4. 文本清洗（金融专属）
5. 文本分块（金融三策略）

技术栈：FastAPI + Uvicorn + PaddleOCR + jieba

配置来源：
- 环境变量
- Nacos配置中心（优先）
"""

import os
import io
from contextlib import asynccontextmanager
from fastapi import FastAPI, File, UploadFile, HTTPException, Body
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from loguru import logger
from typing import Optional, List, Dict, Any
import uvicorn
import datetime
import asyncio

# 导入配置和服务
from config import (
    SERVER_HOST,
    SERVER_PORT,
    DEBUG_MODE,
    MAX_FILE_SIZE,
    LOG_FILE,
    LOG_LEVEL,
    LOG_ROTATION,
    LOG_RETENTION,
    NACOS_ENABLED,
    NACOS_HOST,
    NACOS_PORT
)
from services import document_parser, ocr_service, text_chunker, text_cleaner
from services.nacos_service import nacos_registry


# ---------------------------
# 生命周期管理（服务注册/注销）
# ---------------------------
@asynccontextmanager
async def lifespan(app: FastAPI):
    """FastAPI 应用生命周期管理"""
    # 启动时：注册服务到 Nacos
    if NACOS_ENABLED:
        try:
            init_success = await nacos_registry.init_client()
            if init_success:
                await nacos_registry.register()
                logger.info(f"服务已注册到 Nacos: {nacos_registry.service_info}")
        except Exception as e:
            logger.error(f"Nacos 服务注册失败: {e}")

    yield  # 服务运行中

    # 关闭时：从 Nacos 注销服务
    if nacos_registry.is_registered:
        try:
            await nacos_registry.deregister()
            await nacos_registry.shutdown()
        except Exception as e:
            logger.error(f"Nacos 服务注销失败: {e}")

# 配置日志
logger.remove()
logger.add(
    LOG_FILE,
    level=LOG_LEVEL,
    rotation=LOG_ROTATION,
    retention=LOG_RETENTION,
    format="{time:YYYY-MM-DD HH:mm:ss} | {level} | {message}"
)
logger.add(
    lambda msg: print(msg),
    level=LOG_LEVEL
)

# 创建FastAPI应用
app = FastAPI(
    title="FinRag4j Python预处理微服务",
    description="金融文档解析、OCR识别、文本清洗、文本分块服务（适配Nacos配置中心）",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
    lifespan=lifespan  # 添加生命周期管理
)

# 配置跨域
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 创建必要的目录
os.makedirs(os.path.dirname(LOG_FILE), exist_ok=True)
os.makedirs("./temp", exist_ok=True)


# ---------------------------
# 请求/响应模型
# ---------------------------
class HealthResponse(BaseModel):
    """健康检查响应模型（适配Nacos）"""
    status: str
    service: str
    timestamp: str
    version: str
    message: str
    nacos_enabled: bool
    nacos_host: Optional[str]
    nacos_port: Optional[int]
    nacos_registered: Optional[bool] = False


class ParseFileResponse(BaseModel):
    """文档解析响应模型"""
    success: bool
    file_type: str
    text: str
    page_count: int
    message: str


class OCRResponse(BaseModel):
    """OCR识别响应模型"""
    success: bool
    text: str
    confidence: float
    page_count: int
    message: str


class TextCleanRequest(BaseModel):
    """文本清洗请求模型"""
    text: str
    remove_header_footer: bool = True
    remove_watermark: bool = True
    remove_empty_lines: bool = True
    remove_junk_text: bool = True


class TextCleanResponse(BaseModel):
    """文本清洗响应模型"""
    success: bool
    text: str
    cleaned_text: str
    cleaned_length: int
    original_length: int
    message: str


class TextSplitRequest(BaseModel):
    """文本分块请求模型"""
    text: str
    strategy: str = "regulatory"
    chunk_size: Optional[int] = None
    chunk_overlap: Optional[int] = None


class TextSplitResponse(BaseModel):
    """文本分块响应模型"""
    success: bool
    chunks: List[Dict[str, Any]]
    strategy: str
    total_chunks: int
    message: str


# ---------------------------
# API端点
# ---------------------------
@app.get("/health", response_model=HealthResponse, tags=["系统"], summary="健康检查（适配Nacos）")
async def health_check():
    """
    服务健康心跳检测，适配Nacos健康巡检
    
    **返回结构**:
    - status: healthy/unhealthy
    - service: 服务名称
    - timestamp: 当前时间戳
    - version: 服务版本
    - message: 状态消息
    - nacos_enabled: 是否启用Nacos配置中心
    - nacos_host: Nacos服务地址
    - nacos_port: Nacos服务端口
    """
    return {
        "status": "healthy",
        "service": "finrag4j-python",
        "timestamp": datetime.datetime.now().isoformat(),
        "version": "1.0.0",
        "message": "服务运行正常",
        "nacos_enabled": NACOS_ENABLED,
        "nacos_host": NACOS_HOST if NACOS_ENABLED else None,
        "nacos_port": NACOS_PORT if NACOS_ENABLED else None,
        "nacos_registered": nacos_registry.is_registered
    }


@app.post("/api/parse/file", response_model=ParseFileResponse, tags=["文档解析"], summary="通用文档解析")
async def parse_file(
    file: UploadFile = File(...),
    file_type: Optional[str] = Body(None)
):
    """
    通用文档解析接口（PDF/Word/Excel/TXT）
    
    **支持的文件类型**:
    - PDF (.pdf)
    - Word (.doc, .docx)
    - Excel (.xls, .xlsx)
    - TXT (.txt)
    
    **请求参数**:
    - file: 上传的文件（必填）
    - file_type: 文件类型（可选，自动检测）
    
    **返回结构**:
    - success: 是否成功
    - file_type: 文件类型
    - text: 纯文本内容
    - page_count: 页数/工作表数
    - message: 处理消息
    """
    try:
        # 检查文件大小限制
        if file.size > MAX_FILE_SIZE:
            raise HTTPException(status_code=400, detail=f"文件大小超过限制（最大{MAX_FILE_SIZE/1024/1024:.1f}MB）")
        
        # 获取文件类型
        if not file_type:
            file_type = file.filename.split(".")[-1].lower()
        
        # 读取文件内容
        file_bytes = await file.read()
        
        # 解析文件
        result = document_parser.parse_bytes(file_bytes, file_type)
        
        logger.info(f"解析文件: {file.filename}, 类型: {file_type}, 大小: {len(file_bytes)} bytes")
        
        # 构建返回结构
        content = result.get("content", {})
        return {
            "success": result.get("success", False),
            "file_type": result.get("file_type", file_type),
            "text": content.get("text", ""),
            "page_count": content.get("page_count", 0) or content.get("sheets_count", 0) or 1,
            "message": result.get("message", "")
        }
    
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"文档解析失败: {e}")
        raise HTTPException(status_code=500, detail=f"文档解析失败: {str(e)}")


@app.post("/api/parse/ocr", response_model=OCRResponse, tags=["OCR识别"], summary="离线PaddleOCR识别")
async def ocr_recognize(
    file: UploadFile = File(...),
    preprocess: bool = Body(True)
):
    """
    离线PaddleOCR识别接口，支持图片/扫描PDF
    
    **支持的文件类型**:
    - 图片: .jpg, .jpeg, .png, .bmp, .tiff
    - 扫描PDF: .pdf
    
    **请求参数**:
    - file: 上传的文件（必填）
    - preprocess: 是否进行图像预处理（去噪、矫正，默认True）
    
    **返回结构**:
    - success: 是否成功
    - text: 识别的完整文本
    - confidence: 平均置信度（0-1）
    - page_count: 页数
    - message: 处理消息
    """
    try:
        # 检查文件大小限制
        if file.size > MAX_FILE_SIZE:
            raise HTTPException(status_code=400, detail=f"文件大小超过限制（最大{MAX_FILE_SIZE/1024/1024:.1f}MB）")
        
        # 获取文件类型
        filename = file.filename.lower()
        file_type = "pdf" if filename.endswith(".pdf") else "image"
        
        # 读取文件内容
        file_bytes = await file.read()
        
        # 执行OCR识别
        result = ocr_service.recognize_bytes(file_bytes, file_type, preprocess)
        
        logger.info(f"OCR识别文件: {file.filename}, 类型: {file_type}")
        
        # 构建返回结构
        return {
            "success": result.get("success", False),
            "text": result.get("text", ""),
            "confidence": result.get("confidence", 0.0),
            "page_count": len(result.get("pages", [])) if result.get("pages") else 1,
            "message": result.get("message", "")
        }
    
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"OCR识别失败: {e}")
        raise HTTPException(status_code=500, detail=f"OCR识别失败: {str(e)}")


@app.post("/api/text/clean", response_model=TextCleanResponse, tags=["文本清洗"], summary="金融专属文本清洗")
async def clean_text(request: TextCleanRequest):
    """
    金融专属文本清洗接口
    
    **清洗功能**:
    - 去除页眉页脚
    - 去除水印
    - 去除冗余空行
    - 去除制式垃圾文本
    
    **请求参数**:
    - text: 待清洗的文本内容（必填）
    - remove_header_footer: 是否去除页眉页脚（默认True）
    - remove_watermark: 是否去除水印（默认True）
    - remove_empty_lines: 是否去除冗余空行（默认True）
    - remove_junk_text: 是否去除制式垃圾文本（默认True）
    
    **返回结构**:
    - success: 是否成功
    - text: 原始文本（缩写）
    - cleaned_text: 清洗后的文本
    - cleaned_length: 清洗后长度
    - original_length: 原始长度
    - message: 处理消息
    """
    try:
        if not request.text or not request.text.strip():
            raise HTTPException(status_code=400, detail="文本内容不能为空")
        
        # 执行文本清洗
        cleaned_text = text_cleaner.clean(
            text=request.text,
            remove_header_footer=request.remove_header_footer,
            remove_watermark=request.remove_watermark,
            remove_empty_lines=request.remove_empty_lines,
            remove_junk_text=request.remove_junk_text
        )
        
        logger.info(f"文本清洗完成: 原长度={len(request.text)}, 清洗后={len(cleaned_text)}")
        
        # 构建返回结构
        return {
            "success": True,
            "text": request.text[:200] + "..." if len(request.text) > 200 else request.text,
            "cleaned_text": cleaned_text,
            "cleaned_length": len(cleaned_text),
            "original_length": len(request.text),
            "message": "清洗成功"
        }
    
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"文本清洗失败: {e}")
        raise HTTPException(status_code=500, detail=f"文本清洗失败: {str(e)}")


@app.post("/api/text/split", response_model=TextSplitResponse, tags=["文本分块"], summary="金融三策略智能分块")
async def split_text(request: TextSplitRequest):
    """
    金融三策略智能分块接口
    
    **分块策略**:
    - regulatory: 监管文件策略（如法律法规、监管通知）
    - credit: 信贷合同策略（如贷款合同、担保协议）
    - official: 公文策略（如内部公告、通知）
    
    **请求参数**:
    - text: 待分块的文本内容（必填）
    - strategy: 分块策略（默认regulatory）
    - chunk_size: 自定义分块大小（可选，默认根据策略自动调整）
    - chunk_overlap: 自定义重叠度（可选，默认根据策略自动调整）
    
    **返回结构**:
    - success: 是否成功
    - chunks: 分块列表
      - content: 块内容
      - chunk_index: 块索引
      - start_pos: 起始位置
      - end_pos: 结束位置
    - strategy: 使用的策略
    - total_chunks: 块总数
    - message: 处理消息
    """
    try:
        if not request.text or not request.text.strip():
            raise HTTPException(status_code=400, detail="文本内容不能为空")
        
        # 验证策略类型
        valid_strategies = ["regulatory", "credit", "official"]
        if request.strategy not in valid_strategies:
            raise HTTPException(status_code=400, detail=f"无效的分块策略: {request.strategy}，可选值: {valid_strategies}")
        
        # 执行分块
        result = text_chunker.chunk_text(
            text=request.text,
            strategy=request.strategy,
            chunk_size=request.chunk_size,
            chunk_overlap=request.chunk_overlap
        )
        
        logger.info(f"文本分块完成: 策略={request.strategy}, 块数={result['total_chunks']}")
        
        return result
    
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"文本分块失败: {e}")
        raise HTTPException(status_code=500, detail=f"文本分块失败: {str(e)}")


# ---------------------------
# 启动服务
# ---------------------------
if __name__ == "__main__":
    logger.info(f"启动FinRag4j Python预处理微服务")
    logger.info(f"服务地址: http://{SERVER_HOST}:{SERVER_PORT}")
    logger.info(f"API文档: http://{SERVER_HOST}:{SERVER_PORT}/docs")
    
    uvicorn.run(
        "main:app",
        host=SERVER_HOST,
        port=SERVER_PORT,
        reload=DEBUG_MODE,
        log_level=LOG_LEVEL.lower()
    )
