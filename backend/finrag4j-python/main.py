"""
FinRag4j Python 预处理微服务入口文件

服务功能：
1. 文档解析（PDF/Word/Excel/TXT）
2. 离线OCR识别（图片/扫描PDF）
3. 文本分块（支持多种金融文档策略）

技术栈：FastAPI + Uvicorn + PaddleOCR + jieba
"""

import os
import io
from fastapi import FastAPI, File, UploadFile, HTTPException, Body
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from loguru import logger
from typing import Optional, List, Dict, Any
import uvicorn

# 导入配置和服务
from config import (
    SERVER_HOST,
    SERVER_PORT,
    DEBUG_MODE,
    MAX_FILE_SIZE,
    LOG_FILE,
    LOG_LEVEL,
    LOG_ROTATION,
    LOG_RETENTION
)
from services import document_parser, ocr_service, text_chunker

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
    description="金融文档解析、OCR识别、文本分块服务",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
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
    """健康检查响应模型"""
    status: str
    service: str
    timestamp: str


class ParseRequest(BaseModel):
    """文档解析请求模型"""
    file_path: Optional[str] = None
    file_type: Optional[str] = None


class ParseResponse(BaseModel):
    """文档解析响应模型"""
    success: bool
    file_type: str
    content: Optional[Dict[str, Any]]
    message: str


class OCRRequest(BaseModel):
    """OCR识别请求模型"""
    file_path: Optional[str] = None
    preprocess: bool = True


class OCRResponse(BaseModel):
    """OCR识别响应模型"""
    success: bool
    text: str
    pages: Optional[List[Dict[str, Any]]]
    boxes: Optional[List[Dict[str, Any]]]
    message: str


class ChunkRequest(BaseModel):
    """文本分块请求模型"""
    text: str
    strategy: str = "regulation"
    chunk_size: Optional[int] = None
    chunk_overlap: Optional[int] = None


class ChunkResponse(BaseModel):
    """文本分块响应模型"""
    success: bool
    chunks: List[Dict[str, Any]]
    strategy: str
    total_chunks: int
    message: str


# ---------------------------
# API端点
# ---------------------------
@app.get("/health", response_model=HealthResponse, tags=["系统"])
async def health_check():
    """
    健康检查接口
    
    返回服务状态信息
    """
    import datetime
    return {
        "status": "healthy",
        "service": "finrag4j-python",
        "timestamp": datetime.datetime.now().isoformat()
    }


@app.post("/api/parse", response_model=ParseResponse, tags=["文档解析"])
async def parse_document(
    file: Optional[UploadFile] = File(None),
    file_path: Optional[str] = Body(None),
    file_type: Optional[str] = Body(None)
):
    """
    文档解析接口
    
    支持上传文件或指定文件路径进行解析。
    
    **支持的文件类型**:
    - PDF (.pdf)
    - Word (.doc, .docx)
    - Excel (.xls, .xlsx)
    - TXT (.txt)
    - PPT (.pptx)
    
    **请求参数**:
    - file: 上传的文件（优先）
    - file_path: 文件路径（备选）
    - file_type: 文件类型（可选，自动检测）
    
    **返回结构**:
    - success: 是否成功
    - file_type: 文件类型
    - content: 解析内容
      - text: 纯文本内容
      - tables: 表格列表（如果有）
      - pages: 页面列表（PDF）
      - sheets: 工作表列表（Excel）
    - message: 处理消息
    """
    try:
        # 检查文件大小限制
        if file and file.size > MAX_FILE_SIZE:
            raise HTTPException(status_code=400, detail=f"文件大小超过限制（最大{MAX_FILE_SIZE/1024/1024:.1f}MB）")
        
        if file:
            # 获取文件类型
            if not file_type:
                file_type = file.filename.split(".")[-1].lower()
            
            # 读取文件内容
            file_bytes = await file.read()
            
            # 解析文件
            result = document_parser.parse_bytes(file_bytes, file_type)
            
            logger.info(f"解析文件: {file.filename}, 类型: {file_type}, 大小: {len(file_bytes)} bytes")
            
            return result
        
        elif file_path:
            # 检查文件路径是否存在
            if not os.path.exists(file_path):
                raise HTTPException(status_code=404, detail=f"文件不存在: {file_path}")
            
            # 解析文件
            result = document_parser.parse_file(file_path, file_type)
            
            logger.info(f"解析文件路径: {file_path}")
            
            return result
        
        else:
            raise HTTPException(status_code=400, detail="请提供上传文件或文件路径")
    
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"文档解析失败: {e}")
        raise HTTPException(status_code=500, detail=f"文档解析失败: {str(e)}")


@app.post("/api/ocr", response_model=OCRResponse, tags=["OCR识别"])
async def ocr_recognize(
    file: Optional[UploadFile] = File(None),
    file_path: Optional[str] = Body(None),
    preprocess: bool = Body(True)
):
    """
    离线OCR识别接口
    
    支持图片和扫描PDF的文字识别，使用PaddleOCR离线识别。
    
    **支持的文件类型**:
    - 图片: .jpg, .jpeg, .png, .bmp, .tiff
    - 扫描PDF: .pdf
    
    **请求参数**:
    - file: 上传的文件（优先）
    - file_path: 文件路径（备选）
    - preprocess: 是否进行图像预处理（默认True）
    
    **返回结构**:
    - success: 是否成功
    - text: 识别的完整文本
    - pages: 每页识别结果（PDF）
    - boxes: 识别框位置信息
    - message: 处理消息
    """
    try:
        # 检查文件大小限制
        if file and file.size > MAX_FILE_SIZE:
            raise HTTPException(status_code=400, detail=f"文件大小超过限制（最大{MAX_FILE_SIZE/1024/1024:.1f}MB）")
        
        if file:
            # 获取文件类型
            filename = file.filename.lower()
            file_type = "pdf" if filename.endswith(".pdf") else "image"
            
            # 读取文件内容
            file_bytes = await file.read()
            
            # 执行OCR识别
            result = ocr_service.recognize_bytes(file_bytes, file_type)
            
            logger.info(f"OCR识别文件: {file.filename}, 类型: {file_type}")
            
            return result
        
        elif file_path:
            # 检查文件路径是否存在
            if not os.path.exists(file_path):
                raise HTTPException(status_code=404, detail=f"文件不存在: {file_path}")
            
            # 判断文件类型
            file_type = "pdf" if file_path.lower().endswith(".pdf") else "image"
            
            # 执行OCR识别
            if file_type == "pdf":
                result = ocr_service.recognize_pdf(file_path, preprocess)
            else:
                result = ocr_service.recognize_image(file_path, preprocess)
            
            logger.info(f"OCR识别文件路径: {file_path}")
            
            return result
        
        else:
            raise HTTPException(status_code=400, detail="请提供上传文件或文件路径")
    
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"OCR识别失败: {e}")
        raise HTTPException(status_code=500, detail=f"OCR识别失败: {str(e)}")


@app.post("/api/chunk", response_model=ChunkResponse, tags=["文本分块"])
async def chunk_text(request: ChunkRequest):
    """
    文本分块接口
    
    根据金融文档类型进行智能分块，支持三种策略。
    
    **分块策略**:
    - regulation: 监管文件策略（如法律法规、监管通知）
    - contract: 信贷合同策略（如贷款合同、担保协议）
    - notice: 内部通知策略（如内部公告、通知）
    
    **请求参数**:
    - text: 待分块的文本内容
    - strategy: 分块策略（默认regulation）
    - chunk_size: 自定义分块大小（可选）
    - chunk_overlap: 自定义重叠大小（可选）
    
    **返回结构**:
    - success: 是否成功
    - chunks: 分块列表
      - content: 块内容
      - section: 所属段落（如果有）
      - start_pos: 起始位置
      - end_pos: 结束位置
    - strategy: 使用的策略
    - total_chunks: 块总数
    - message: 处理消息
    """
    try:
        if not request.text or not request.text.strip():
            raise HTTPException(status_code=400, detail="文本内容不能为空")
        
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


@app.get("/api/chunk/strategies", tags=["文本分块"])
async def get_chunk_strategies(strategy: Optional[str] = None):
    """
    获取分块策略配置
    
    **请求参数**:
    - strategy: 策略名称（可选，不传则返回所有策略）
    
    **返回结构**:
    - strategies: 策略列表（未指定策略时）
    - details: 策略详细配置
    """
    try:
        return text_chunker.get_strategy_info(strategy)
    except Exception as e:
        logger.error(f"获取策略配置失败: {e}")
        raise HTTPException(status_code=500, detail=f"获取策略配置失败: {str(e)}")


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