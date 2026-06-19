package com.finrag4j.document.mapper;

import com.finrag4j.document.entity.KbDocument;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库文档关联Mapper
 */
@Mapper
public interface KbDocumentMapper extends BaseMapper<KbDocument> {
}
