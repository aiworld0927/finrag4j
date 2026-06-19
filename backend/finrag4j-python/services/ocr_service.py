"""
OCR识别服务模块

功能：
1. 图片OCR识别
2. 扫描PDF识别（先转换为图片，再逐页识别）
3. 支持中文、英文等多种语言
"""

import os
import tempfile
import cv2
import numpy as np
from paddleocr import PaddleOCR
from typing import Dict, List, Any, Optional
from config import OCR_MODEL_DIR, OCR_LANG, OCR_USE_GPU
from utils.image_processor import preprocess_image


class OCRService:
    """OCR识别服务"""
    
    def __init__(self):
        # 初始化PaddleOCR
        self.ocr = PaddleOCR(
            use_angle_cls=True,
            lang=OCR_LANG
        )
    
    def recognize_image(self, image_path: str, preprocess: bool = True) -> Dict[str, Any]:
        """
        识别单张图片
        
        Args:
            image_path: 图片路径
            preprocess: 是否进行图像预处理
        
        Returns:
            识别结果
        """
        result = {
            "success": False,
            "text": "",
            "boxes": [],
            "message": ""
        }
        
        try:
            # 图像预处理
            if preprocess:
                processed_image = preprocess_image(image_path)
                # 保存预处理后的临时图像
                with tempfile.NamedTemporaryFile(delete=False, suffix=".png") as f:
                    cv2.imwrite(f.name, processed_image)
                    temp_path = f.name
            else:
                temp_path = image_path
            
            # 使用PaddleOCR识别
            ocr_result = self.ocr.ocr(temp_path)
            
            # 提取文本和位置信息
            text_parts = []
            boxes = []
            
            for page in ocr_result:
                if page:
                    for line in page:
                        box = line[0]
                        text = line[1][0]
                        confidence = line[1][1]
                        
                        text_parts.append(text)
                        boxes.append({
                            "box": box,
                            "text": text,
                            "confidence": float(confidence)
                        })
            
            result["success"] = True
            result["text"] = "\n".join(text_parts)
            result["boxes"] = boxes
            result["message"] = "识别成功"
            
            # 清理临时文件
            if preprocess and os.path.exists(temp_path):
                os.unlink(temp_path)
            
        except Exception as e:
            result["message"] = f"识别失败: {str(e)}"
        
        return result
    
    def recognize_pdf(self, pdf_path: str, preprocess: bool = True) -> Dict[str, Any]:
        """
        识别扫描PDF（将PDF转换为图片后逐页识别）
        
        Args:
            pdf_path: PDF文件路径
            preprocess: 是否进行图像预处理
        
        Returns:
            识别结果
        """
        result = {
            "success": False,
            "text": "",
            "pages": [],
            "message": ""
        }
        
        try:
            # 将PDF转换为图片
            images = self._pdf_to_images(pdf_path)
            
            if not images:
                result["message"] = "无法将PDF转换为图片"
                return result
            
            # 逐页识别
            all_text = []
            pages_result = []
            
            for page_num, image in enumerate(images, 1):
                # 保存临时图片
                with tempfile.NamedTemporaryFile(delete=False, suffix=".png") as f:
                    cv2.imwrite(f.name, image)
                    temp_image_path = f.name
                
                # 识别单页
                page_result = self.recognize_image(temp_image_path, preprocess)
                
                if page_result["success"]:
                    all_text.append(page_result["text"])
                    pages_result.append({
                        "page_number": page_num,
                        "text": page_result["text"],
                        "boxes": page_result["boxes"]
                    })
                
                # 清理临时文件
                os.unlink(temp_image_path)
            
            result["success"] = True
            result["text"] = "\n".join(all_text)
            result["pages"] = pages_result
            result["message"] = f"识别完成，共{len(pages_result)}页"
        
        except Exception as e:
            result["message"] = f"PDF识别失败: {str(e)}"
        
        return result
    
    def recognize_bytes(self, file_bytes: bytes, file_type: str = "image", preprocess: bool = True) -> Dict[str, Any]:
        """
        识别文件字节流
        
        Args:
            file_bytes: 文件字节内容
            file_type: 文件类型（image或pdf）
            preprocess: 是否进行图像预处理
        
        Returns:
            识别结果
        """
        # 创建临时文件
        suffix = ".png" if file_type == "image" else ".pdf"
        with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as f:
            f.write(file_bytes)
            temp_path = f.name
        
        try:
            if file_type == "image":
                return self.recognize_image(temp_path, preprocess)
            elif file_type == "pdf":
                return self.recognize_pdf(temp_path, preprocess)
            else:
                return {"success": False, "message": f"不支持的文件类型: {file_type}"}
        finally:
            os.unlink(temp_path)
    
    def _pdf_to_images(self, pdf_path: str) -> List[np.ndarray]:
        """
        将PDF文件转换为图片列表
        
        Args:
            pdf_path: PDF文件路径
        
        Returns:
            图片数组列表
        """
        images = []
        
        try:
            # 尝试使用PyMuPDF（fitz）
            try:
                import fitz
                doc = fitz.open(pdf_path)
                
                for page in doc:
                    pix = page.get_pixmap()
                    img = np.frombuffer(pix.samples, dtype=np.uint8).reshape(pix.height, pix.width, 3)
                    img = cv2.cvtColor(img, cv2.COLOR_RGB2BGR)
                    images.append(img)
                
                doc.close()
            except ImportError:
                # 如果没有安装PyMuPDF，使用替代方案
                # 这里简化处理，实际生产环境建议安装PyMuPDF
                raise ImportError("需要安装PyMuPDF (fitz) 来处理PDF")
        
        except Exception as e:
            print(f"PDF转换失败: {e}")
        
        return images


# 创建全局OCR服务实例
ocr_service = OCRService()
