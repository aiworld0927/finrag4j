package com.finrag4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finrag4j.entity.ExtractRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 抽取台账Mapper
 */
@Mapper
public interface ExtractRecordMapper extends BaseMapper<ExtractRecord> {

    @Select("SELECT * FROM extract_record WHERE template_id = #{templateId}")
    List<ExtractRecord> selectByTemplateId(@Param("templateId") Long templateId);

    @Select("SELECT * FROM extract_record WHERE document_id = #{documentId}")
    List<ExtractRecord> selectByDocumentId(@Param("documentId") Long documentId);

    @Select("SELECT * FROM extract_record WHERE tenant_id = #{tenantId} AND status = #{status}")
    List<ExtractRecord> selectByTenantAndStatus(@Param("tenantId") Long tenantId, @Param("status") String status);
}