"""
文本清洗工具模块

功能：
1. 去除多余空白字符
2. 去除特殊字符
3. 去除停用词
4. 文本规范化处理
"""

import re
import os
from typing import List, Optional
from config import CLEAN_REMOVE_WHITESPACE, CLEAN_REMOVE_SPECIAL_CHARS, CLEAN_REMOVE_STOPWORDS, STOPWORDS_FILE

# 全局停用词集合
STOPWORDS = set()


def load_stopwords() -> None:
    """
    加载金融领域停用词文件
    """
    global STOPWORDS
    if os.path.exists(STOPWORDS_FILE):
        try:
            with open(STOPWORDS_FILE, "r", encoding="utf-8") as f:
                STOPWORDS = set(line.strip() for line in f if line.strip())
        except Exception as e:
            print(f"加载停用词文件失败: {e}")
    else:
        # 默认金融停用词
        STOPWORDS = {
            "的", "是", "在", "和", "有", "我", "他", "她", "它", "这", "那", "这些", "那些",
            "什么", "怎么", "为什么", "因为", "所以", "但是", "然而", "如果", "虽然", "但是",
            "一个", "一些", "所有", "每个", "任何", "许多", "很少", "更多", "更少",
            "可以", "可能", "应该", "必须", "需要", "会", "不会", "能", "不能",
            "已经", "正在", "将要", "曾经", "从来", "总是", "经常", "偶尔", "有时",
            "这里", "那里", "哪里", "到处", "附近", "上面", "下面", "前面", "后面",
            "金融", "公司", "银行", "证券", "保险", "基金", "投资", "理财", "贷款",
            "金额", "利率", "期限", "还款", "逾期", "罚息", "违约金", "保证金",
            "合同", "协议", "条款", "规定", "要求", "通知", "公告", "报告", "说明",
            "第一条", "第二条", "第三条", "第四条", "第五条", "第六条", "第七条", "第八条",
            "第一章", "第二章", "第三章", "第四章", "第五章", "第一节", "第二节", "第三节",
        }


def remove_extra_whitespace(text: str) -> str:
    """
    去除多余空白字符（空格、换行、制表符等）
    """
    if not text:
        return ""
    
    # 去除多余空格
    text = re.sub(r"\s+", " ", text)
    
    # 去除首尾空格
    text = text.strip()
    
    return text


def remove_special_chars(text: str, keep_chars: Optional[str] = None) -> str:
    """
    去除特殊字符，保留中文、英文、数字和基本标点
    
    Args:
        text: 输入文本
        keep_chars: 额外保留的字符
    
    Returns:
        清洗后的文本
    """
    if not text:
        return ""
    
    # 保留中文、英文、数字、中文标点、英文标点
    pattern = r"[^\u4e00-\u9fa5a-zA-Z0-9，。！？、；：""''（）{}【】<>《》·…—\\s]"
    
    if keep_chars:
        # 添加额外保留的字符到正则表达式
        escaped_chars = re.escape(keep_chars)
        pattern = rf"[^\u4e00-\u9fa5a-zA-Z0-9，。！？、；：""''（）{}【】<>《》·…—\\s{escaped_chars}]"
    
    text = re.sub(pattern, "", text)
    
    return text


def remove_stopwords(text: str) -> str:
    """
    去除停用词
    """
    if not text or not CLEAN_REMOVE_STOPWORDS:
        return text
    
    words = text.split()
    filtered_words = [word for word in words if word not in STOPWORDS]
    return " ".join(filtered_words)


def normalize_text(text: str) -> str:
    """
    文本规范化处理
    
    包括：
    1. 全角转半角
    2. 去除零宽字符
    3. 统一标点符号
    """
    if not text:
        return ""
    
    # 全角转半角
    text = full_width_to_half_width(text)
    
    # 去除零宽字符
    text = remove_zero_width_chars(text)
    
    # 统一标点符号
    text = normalize_punctuation(text)
    
    return text


def full_width_to_half_width(text: str) -> str:
    """
    全角字符转半角字符
    """
    result = []
    for char in text:
        char_code = ord(char)
        # 全角空格
        if char_code == 12288:
            result.append(" ")
        # 全角ASCII字符
        elif 65281 <= char_code <= 65374:
            result.append(chr(char_code - 65248))
        else:
            result.append(char)
    return "".join(result)


def remove_zero_width_chars(text: str) -> str:
    """
    去除零宽字符（如零宽空格、零宽连接符等）
    """
    zero_width_pattern = r"[\u200B\u200C\u200D\uFEFF]"
    return re.sub(zero_width_pattern, "", text)


def normalize_punctuation(text: str) -> str:
    """
    统一标点符号为中文标点
    """
    punctuation_map = {
        ",": "，",
        ".": "。",
        "!": "！",
        "?": "？",
        ";": "；",
        ":": "：",
        "\"": "“",
        "'": "‘",
        "(": "（",
        ")": "）",
        "[": "【",
        "]": "】",
        "{": "｛",
        "}": "｝",
        "<": "《",
        ">": "》",
    }
    
    for eng_punc, chn_punc in punctuation_map.items():
        text = text.replace(eng_punc, chn_punc)
    
    return text


def clean_text(text: str, remove_stopwords_flag: bool = True) -> str:
    """
    完整文本清洗流程
    
    Args:
        text: 原始文本
        remove_stopwords_flag: 是否去除停用词
    
    Returns:
        清洗后的文本
    """
    if not text:
        return ""
    
    # 文本规范化
    text = normalize_text(text)
    
    # 去除多余空白
    if CLEAN_REMOVE_WHITESPACE:
        text = remove_extra_whitespace(text)
    
    # 去除特殊字符
    if CLEAN_REMOVE_SPECIAL_CHARS:
        text = remove_special_chars(text)
    
    # 去除停用词
    if remove_stopwords_flag and CLEAN_REMOVE_STOPWORDS:
        text = remove_stopwords(text)
    
    return text


def split_sentences(text: str) -> List[str]:
    """
    将文本按中文标点分割成句子列表
    
    Args:
        text: 输入文本
    
    Returns:
        句子列表
    """
    if not text:
        return []
    
    # 按中文句号、感叹号、问号分割
    sentences = re.split(r"([。！？])", text)
    
    # 重组句子（保留标点）
    result = []
    for i in range(0, len(sentences) - 1, 2):
        sentence = sentences[i] + sentences[i + 1]
        sentence = sentence.strip()
        if sentence:
            result.append(sentence)
    
    return result


# 初始化加载停用词
load_stopwords()
