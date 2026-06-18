package com.finrag4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finrag4j.entity.DocumentVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文档版本Mapper接口
 */
@Mapper
public interface DocumentVersionMapper extends BaseMapper<DocumentVersion> {

    @Select("SELECT dv.* FROM document_version dv WHERE dv.document_id = #{documentId} AND dv.tenant_id = #{tenantId} AND dv.deleted = 0 ORDER BY dv.created_at DESC")
    List<DocumentVersion> selectByDocumentId(@Param("documentId") Long documentId, @Param("tenantId") Long tenantId);

    @Select("SELECT dv.* FROM document_version dv WHERE dv.document_id = #{documentId} AND dv.version_number = #{versionNumber} AND dv.tenant_id = #{tenantId} AND dv.deleted = 0")
    DocumentVersion selectByVersion(@Param("documentId") Long documentId, @Param("versionNumber") String versionNumber, @Param("tenantId") Long tenantId);
}