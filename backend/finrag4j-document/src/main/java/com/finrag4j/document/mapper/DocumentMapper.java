package com.finrag4j.document.mapper;

import com.finrag4j.document.entity.Document;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档Mapper
 */
@Mapper
public interface DocumentMapper extends BaseMapper<Document> {
}
