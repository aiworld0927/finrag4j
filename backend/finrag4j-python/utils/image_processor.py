"""
图像预处理工具模块

功能：
1. 图像矫正（倾斜校正）
2. 图像去噪
3. 图像增强（对比度、亮度调整）
4. 图像格式转换
"""

import cv2
import numpy as np
from PIL import Image
from typing import Optional
from config import IMAGE_ENABLE_CORRECTION, IMAGE_ENABLE_DENOISING, IMAGE_ENABLE_ENHANCEMENT


def preprocess_image(image_path: str, output_path: Optional[str] = None) -> np.ndarray:
    """
    图像预处理完整流程
    
    Args:
        image_path: 输入图像路径
        output_path: 输出图像路径（可选）
    
    Returns:
        预处理后的图像数组
    """
    # 读取图像
    image = cv2.imread(image_path)
    if image is None:
        raise ValueError(f"无法读取图像文件: {image_path}")
    
    # 转换为灰度图（OCR识别用）
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    
    # 图像矫正
    if IMAGE_ENABLE_CORRECTION:
        gray = correct_image_skew(gray)
    
    # 图像去噪
    if IMAGE_ENABLE_DENOISING:
        gray = denoise_image(gray)
    
    # 图像增强
    if IMAGE_ENABLE_ENHANCEMENT:
        gray = enhance_image(gray)
    
    # 如果指定输出路径，保存处理后的图像
    if output_path:
        cv2.imwrite(output_path, gray)
    
    return gray


def correct_image_skew(image: np.ndarray) -> np.ndarray:
    """
    图像倾斜校正
    
    Args:
        image: 输入灰度图像
    
    Returns:
        校正后的图像
    """
    # 使用边缘检测
    edges = cv2.Canny(image, 50, 150, apertureSize=3)
    
    # 使用霍夫变换检测直线
    lines = cv2.HoughLines(edges, 1, np.pi / 180, 200)
    
    if lines is not None:
        # 计算所有直线的角度
        angles = []
        for line in lines:
            rho, theta = line[0]
            angle = theta * 180 / np.pi
            # 只考虑接近水平的直线
            if abs(angle - 90) < 30:
                angles.append(angle)
        
        if angles:
            # 计算平均角度
            avg_angle = np.mean(angles)
            
            # 如果角度偏离水平超过1度，进行旋转
            if abs(avg_angle - 90) > 1:
                # 计算旋转角度
                rotation_angle = avg_angle - 90
                
                # 获取图像尺寸
                height, width = image.shape[:2]
                center = (width // 2, height // 2)
                
                # 计算旋转矩阵
                rotation_matrix = cv2.getRotationMatrix2D(center, rotation_angle, 1.0)
                
                # 执行旋转
                image = cv2.warpAffine(image, rotation_matrix, (width, height), flags=cv2.INTER_CUBIC)
    
    return image


def denoise_image(image: np.ndarray) -> np.ndarray:
    """
    图像去噪
    
    Args:
        image: 输入灰度图像
    
    Returns:
        去噪后的图像
    """
    # 使用双边滤波（保留边缘的同时去噪）
    denoised = cv2.bilateralFilter(image, 9, 75, 75)
    
    return denoised


def enhance_image(image: np.ndarray) -> np.ndarray:
    """
    图像增强（对比度和亮度调整）
    
    Args:
        image: 输入灰度图像
    
    Returns:
        增强后的图像
    """
    # 使用直方图均衡化增强对比度
    enhanced = cv2.equalizeHist(image)
    
    # 可选：自适应直方图均衡化
    # clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
    # enhanced = clahe.apply(image)
    
    return enhanced


def convert_to_grayscale(image_path: str, output_path: str) -> None:
    """
    将彩色图像转换为灰度图像
    
    Args:
        image_path: 输入图像路径
        output_path: 输出图像路径
    """
    image = Image.open(image_path).convert("L")
    image.save(output_path)


def resize_image(image: np.ndarray, max_width: int = 2000, max_height: int = 2000) -> np.ndarray:
    """
    调整图像大小，保持比例
    
    Args:
        image: 输入图像
        max_width: 最大宽度
        max_height: 最大高度
    
    Returns:
        调整后的图像
    """
    height, width = image.shape[:2]
    
    # 计算缩放比例
    scale = min(max_width / width, max_height / height)
    
    if scale < 1:
        new_width = int(width * scale)
        new_height = int(height * scale)
        image = cv2.resize(image, (new_width, new_height), interpolation=cv2.INTER_AREA)
    
    return image


def binarize_image(image: np.ndarray, threshold: int = 127) -> np.ndarray:
    """
    图像二值化
    
    Args:
        image: 输入灰度图像
        threshold: 二值化阈值
    
    Returns:
        二值化后的图像
    """
    _, binary = cv2.threshold(image, threshold, 255, cv2.THRESH_BINARY)
    return binary


def extract_text_region(image: np.ndarray) -> np.ndarray:
    """
    提取文本区域（去除边框、噪声区域）
    
    Args:
        image: 输入灰度图像
    
    Returns:
        只包含文本区域的图像
    """
    # 使用形态学操作去除噪声
    kernel = np.ones((5, 5), np.uint8)
    opening = cv2.morphologyEx(image, cv2.MORPH_OPEN, kernel, iterations=2)
    
    # 找到轮廓
    contours, _ = cv2.findContours(opening.copy(), cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    
    if contours:
        # 找到最大的轮廓（假设是文本区域）
        max_contour = max(contours, key=cv2.contourArea)
        x, y, w, h = cv2.boundingRect(max_contour)
        
        # 裁剪文本区域
        image = image[y:y+h, x:x+w]
    
    return image
