"""
文本分块服务模块

支持的分块策略：
1. 监管文件分块策略 - 适用于法律法规、监管通知等正式文件
2. 信贷合同分块策略 - 适用于贷款合同、担保协议等法律文件
3. 内部通知分块策略 - 适用于内部公告、通知等简短文件
"""

import jieba
from typing import Dict, List, Any, Optional
from config import (
    DEFAULT_CHUNK_SIZE,
    DEFAULT_CHUNK_OVERLAP,
    MIN_CHUNK_SIZE,
    MAX_CHUNK_SIZE,
    REGULATION_CHUNK_CONFIG,
    CONTRACT_CHUNK_CONFIG,
    NOTICE_CHUNK_CONFIG
)
from utils.text_cleaner import split_sentences


class TextChunker:
    """文本分块服务"""
    
    def __init__(self):
        # 加载jieba分词
        jieba.initialize()
        
        # 分块策略配置
        self.chunk_strategies = {
            "regulation": REGULATION_CHUNK_CONFIG,
            "contract": CONTRACT_CHUNK_CONFIG,
            "notice": NOTICE_CHUNK_CONFIG
        }
    
    def chunk_text(self, text: str, strategy: str = "regulation",
                   chunk_size: Optional[int] = None,
                   chunk_overlap: Optional[int] = None) -> Dict[str, Any]:
        """
        对文本进行分块处理
        
        Args:
            text: 输入文本
            strategy: 分块策略（regulation/contract/notice）
            chunk_size: 分块大小（可选，覆盖策略默认值）
            chunk_overlap: 重叠大小（可选，覆盖策略默认值）
        
        Returns:
            分块结果
        """
        result = {
            "success": False,
            "chunks": [],
            "strategy": strategy,
            "total_chunks": 0,
            "message": ""
        }
        
        if not text or len(text.strip()) < MIN_CHUNK_SIZE:
            result["message"] = "文本长度不足，无需分块"
            return result
        
        # 获取策略配置
        strategy_config = self.chunk_strategies.get(strategy)
        if not strategy_config:
            result["message"] = f"未知的分块策略: {strategy}"
            return result
        
        # 确定分块参数
        actual_chunk_size = chunk_size if chunk_size else strategy_config["chunk_size"]
        actual_overlap = chunk_overlap if chunk_overlap else strategy_config["chunk_overlap"]
        
        # 验证参数范围
        actual_chunk_size = max(MIN_CHUNK_SIZE, min(actual_chunk_size, MAX_CHUNK_SIZE))
        actual_overlap = max(0, min(actual_overlap, actual_chunk_size // 2))
        
        # 根据策略进行分块
        chunks = self._chunk_with_strategy(text, strategy, actual_chunk_size, actual_overlap)
        
        result["success"] = True
        result["chunks"] = chunks
        result["total_chunks"] = len(chunks)
        result["message"] = f"分块完成，共{len(chunks)}个块"
        
        return result
    
    def _chunk_with_strategy(self, text: str, strategy: str,
                             chunk_size: int, chunk_overlap: int) -> List[Dict[str, Any]]:
        """
        使用指定策略进行分块
        
        Args:
            text: 输入文本
            strategy: 策略名称
            chunk_size: 分块大小
            chunk_overlap: 重叠大小
        
        Returns:
            分块列表
        """
        strategy_config = self.chunk_strategies.get(strategy)
        priority_sections = strategy_config.get("priority_sections", [])
        
        # 按优先级段落分割
        chunks = self._split_by_priority_sections(text, priority_sections)
        
        # 如果没有找到优先级段落，使用普通分块
        if len(chunks) <= 1:
            chunks = self._simple_chunk(text, chunk_size, chunk_overlap)
        else:
            # 对每个优先级段落进行进一步分块
            refined_chunks = []
            for chunk in chunks:
                if len(chunk["content"]) > chunk_size:
                    sub_chunks = self._simple_chunk(chunk["content"], chunk_size, chunk_overlap)
                    refined_chunks.extend(sub_chunks)
                else:
                    refined_chunks.append(chunk)
            chunks = refined_chunks
        
        return chunks
    
    def _split_by_priority_sections(self, text: str, priority_sections: List[str]) -> List[Dict[str, Any]]:
        """
        按优先级段落标题分割文本
        
        Args:
            text: 输入文本
            priority_sections: 优先级段落标题列表
        
        Returns:
            分割后的段落列表
        """
        chunks = []
        current_pos = 0
        
        # 查找所有优先级段落的位置
        section_positions = []
        for section in priority_sections:
            start = 0
            while True:
                pos = text.find(section, start)
                if pos == -1:
                    break
                section_positions.append((pos, section))
                start = pos + 1
        
        # 按位置排序
        section_positions.sort(key=lambda x: x[0])
        
        # 分割文本
        prev_pos = 0
        for pos, section in section_positions:
            if pos > prev_pos:
                content = text[prev_pos:pos].strip()
                if content:
                    chunks.append({
                        "content": content,
                        "section": None,
                        "start_pos": prev_pos,
                        "end_pos": pos
                    })
            
            # 找到下一个段落的起始位置
            next_pos = len(text)
            for next_section_pos, _ in section_positions:
                if next_section_pos > pos:
                    next_pos = next_section_pos
                    break
            
            content = text[pos:next_pos].strip()
            if content:
                chunks.append({
                    "content": content,
                    "section": section,
                    "start_pos": pos,
                    "end_pos": next_pos
                })
            
            prev_pos = next_pos
        
        # 添加剩余部分
        if prev_pos < len(text):
            content = text[prev_pos:].strip()
            if content:
                chunks.append({
                    "content": content,
                    "section": None,
                    "start_pos": prev_pos,
                    "end_pos": len(text)
                })
        
        return chunks
    
    def _simple_chunk(self, text: str, chunk_size: int, chunk_overlap: int) -> List[Dict[str, Any]]:
        """
        简单的固定大小分块
        
        Args:
            text: 输入文本
            chunk_size: 分块大小
            chunk_overlap: 重叠大小
        
        Returns:
            分块列表
        """
        chunks = []
        sentences = split_sentences(text)
        
        if not sentences:
            return chunks
        
        current_chunk = []
        current_length = 0
        
        for sentence in sentences:
            sentence_length = len(sentence)
            
            # 如果当前块加上新句子超过大小限制，保存当前块
            if current_length + sentence_length > chunk_size and current_chunk:
                chunks.append({
                    "content": "\n".join(current_chunk),
                    "section": None,
                    "sentence_count": len(current_chunk)
                })
                
                # 保留重叠部分
                overlap_count = max(1, int(len(current_chunk) * (chunk_overlap / chunk_size)))
                current_chunk = current_chunk[-overlap_count:]
                current_length = sum(len(s) for s in current_chunk)
            
            current_chunk.append(sentence)
            current_length += sentence_length
        
        # 添加最后一个块
        if current_chunk:
            chunks.append({
                "content": "\n".join(current_chunk),
                "section": None,
                "sentence_count": len(current_chunk)
            })
        
        return chunks
    
    def get_strategy_info(self, strategy: Optional[str] = None) -> Dict[str, Any]:
        """
        获取分块策略信息
        
        Args:
            strategy: 策略名称（可选，不传则返回所有策略）
        
        Returns:
            策略配置信息
        """
        if strategy:
            return {
                "strategy": strategy,
                "config": self.chunk_strategies.get(strategy, {})
            }
        
        return {
            "strategies": list(self.chunk_strategies.keys()),
            "details": self.chunk_strategies
        }


# 创建全局分块服务实例
text_chunker = TextChunker()
