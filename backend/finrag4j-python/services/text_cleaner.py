"""
FinRag4j Python 文本清洗服务

金融专属文本清洗功能：
1. 去除页眉页脚
2. 去除水印
3. 去除冗余空行
4. 去除制式垃圾文本
"""

import re
import logging

logger = logging.getLogger(__name__)


class TextCleaner:
    """文本清洗器"""
    
    # 常见金融文档页眉页脚模式
    HEADER_FOOTER_PATTERNS = [
        # 页码模式
        r'^\s*[\d]+[\s-]*[\d]*\s*$',  # 单独的页码
        r'^\s*第\s*[\d]+\s*页\s*$',    # 第X页
        r'^\s*[\d]+\s*/\s*[\d]+\s*$',  # X/Y 格式页码
        
        # 文档标题重复（通常出现在页眉）
        r'^\s*[【\(（][^\)】]+[）\)】]\s*$',  # 【标题】格式
        r'^\s*[《][^》]+[》]\s*$',        # 《标题》格式
        
        # 日期时间
        r'^\s*\d{4}[\-/]\d{2}[\-/]\d{2}\s*$',      # YYYY-MM-DD
        r'^\s*\d{4}[\-/]\d{1,2}[\-/]\d{1,2}\s*$',  # YYYY-M-D
        r'^\s*\d{2}[\-/]\d{2}[\-/]\d{4}\s*$',      # DD-MM-YYYY
        
        # 公司名称/机构名称模式
        r'^\s*[中国|中华][人民]?[共和]?[国]?[银行|保险|证券|基金|信托]\s*$',
        r'^\s*[股份]?[有限]?[公司]\s*$',
        
        # 空白行
        r'^\s*$',
    ]
    
    # 水印模式
    WATERMARK_PATTERNS = [
        r'[（\(]水印[）\)]',
        r'watermark',
        r'CONFIDENTIAL',
        r'保密',
        r'内部资料',
        r'仅供参考',
        r'草稿',
        r'DRAFT',
    ]
    
    # 制式垃圾文本模式
    JUNK_PATTERNS = [
        # 打印时间
        r'打印时间[\s：:][^\n]+',
        r'打印日期[\s：:][^\n]+',
        r'Printed[\s]*[On|at][^\n]+',
        
        # 文件名
        r'文件名[\s：:][^\n]+',
        r'File[\s]*[Nn]ame[\s：:][^\n]+',
        
        # 路径
        r'[A-Za-z]:[\\/][^\n]+',
        r'https?://[^\s]+',
        
        # 制表符和控制字符
        r'[\t\r]',
        
        # 连续空白
        r'[ \t]+',
    ]
    
    def __init__(self):
        # 编译正则表达式
        self.header_footer_regex = re.compile('|'.join(self.HEADER_FOOTER_PATTERNS), re.IGNORECASE)
        self.watermark_regex = re.compile('|'.join(self.WATERMARK_PATTERNS), re.IGNORECASE)
        self.junk_regex = re.compile('|'.join(self.JUNK_PATTERNS), re.IGNORECASE)
    
    def clean(self, text: str, remove_header_footer: bool = True, 
              remove_watermark: bool = True, remove_empty_lines: bool = True,
              remove_junk_text: bool = True) -> str:
        """
        清洗文本
        
        Args:
            text: 原始文本
            remove_header_footer: 是否去除页眉页脚
            remove_watermark: 是否去除水印
            remove_empty_lines: 是否去除冗余空行
            remove_junk_text: 是否去除制式垃圾文本
        
        Returns:
            清洗后的文本
        """
        if not text:
            return ""
        
        result = text
        
        # 去除页眉页脚
        if remove_header_footer:
            result = self._remove_header_footer(result)
        
        # 去除水印
        if remove_watermark:
            result = self._remove_watermark(result)
        
        # 去除制式垃圾文本
        if remove_junk_text:
            result = self._remove_junk_text(result)
        
        # 去除冗余空行
        if remove_empty_lines:
            result = self._remove_empty_lines(result)
        
        # 去除首尾空白
        result = result.strip()
        
        logger.info(f"文本清洗完成: 原长度={len(text)}, 清洗后={len(result)}")
        
        return result
    
    def _remove_header_footer(self, text: str) -> str:
        """去除页眉页脚"""
        lines = text.split('\n')
        cleaned_lines = []
        
        for line in lines:
            # 检查是否为页眉页脚模式
            if not self.header_footer_regex.match(line.strip()):
                cleaned_lines.append(line)
        
        return '\n'.join(cleaned_lines)
    
    def _remove_watermark(self, text: str) -> str:
        """去除水印"""
        return self.watermark_regex.sub('', text)
    
    def _remove_junk_text(self, text: str) -> str:
        """去除制式垃圾文本"""
        result = self.junk_regex.sub(' ', text)
        # 替换多个连续空格为单个空格
        result = re.sub(r' {2,}', ' ', result)
        return result
    
    def _remove_empty_lines(self, text: str) -> str:
        """去除冗余空行（连续多个空行合并为一个）"""
        lines = text.split('\n')
        
        # 过滤掉完全空白的行
        non_empty_lines = [line for line in lines if line.strip()]
        
        # 重新加入空行（段落之间保留一个空行）
        result = ''
        for i, line in enumerate(non_empty_lines):
            result += line
            # 如果不是最后一行，添加换行
            if i < len(non_empty_lines) - 1:
                result += '\n\n'
        
        return result


# 创建单例实例
text_cleaner = TextCleaner()

if __name__ == "__main__":
    # 测试示例
    test_text = """
第 1 页 / 共 5 页

【中国人民银行公告】
2024年01月15日

关于加强金融消费者权益保护的通知

打印时间：2024-01-15 10:30:00
文件名：通知.pdf

各银行业金融机构：

为进一步加强金融消费者权益保护工作，现就有关事项通知如下：

一、提高认识，切实增强责任感（水印）

各机构要充分认识保护金融消费者权益的重要性。

二、强化措施，完善保护机制

要建立健全金融消费者权益保护制度。

第 2 页 / 共 5 页
"""
    
    cleaner = TextCleaner()
    cleaned = cleaner.clean(test_text)
    print("原始文本:")
    print(test_text)
    print("\n" + "="*50 + "\n")
    print("清洗后:")
    print(cleaned)