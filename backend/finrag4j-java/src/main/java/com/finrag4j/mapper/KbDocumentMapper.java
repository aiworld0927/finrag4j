package com.finrag4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finrag4j.entity.KbDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * 知识库文档关联Mapper接口
 */
@Mapper
public interface KbDocumentMapper extends BaseMapper<KbDocument> {

    @Select("SELECT kbd.* FROM kb_document kbd WHERE kbd.kb_id = #{kbId} AND kbd.tenant_id = #{tenantId}")
    List<KbDocument> selectByKbId(@Param("kbId") Long kbId, @Param("tenantId") Long tenantId);

    @Select("SELECT kbd.* FROM kb_document kbd WHERE kbd.document_id = #{documentId} AND kbd.tenant_id = #{tenantId}")
    List<KbDocument> selectByDocumentId(@Param("documentId") Long documentId, @Param("tenantId") Long tenantId);

    @Delete("DELETE FROM kb_document WHERE kb_id = #{kbId} AND tenant_id = #{tenantId}")
    int deleteByKbId(@Param("kbId") Long kbId, @Param("tenantId") Long tenantId);

    @Delete("DELETE FROM kb_document WHERE document_id = #{documentId} AND tenant_id = #{tenantId}")
    int deleteByDocumentId(@Param("documentId") Long documentId, @Param("tenantId") Long tenantId);
}