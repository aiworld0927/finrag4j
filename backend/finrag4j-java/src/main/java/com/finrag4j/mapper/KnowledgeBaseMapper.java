package com.finrag4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finrag4j.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 知识库Mapper接口
 */
@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBase> {

    @Select("SELECT kb.* FROM knowledge_base kb WHERE kb.tenant_id = #{tenantId} AND kb.deleted = 0")
    List<KnowledgeBase> selectByTenantId(@Param("tenantId") Long tenantId);

    @Select("SELECT kb.* FROM knowledge_base kb WHERE kb.kb_code = #{kbCode} AND kb.deleted = 0")
    KnowledgeBase selectByCode(@Param("kbCode") String kbCode);

    @Select("SELECT COUNT(*) FROM kb_document WHERE kb_id = #{kbId} AND tenant_id = #{tenantId}")
    Integer countDocumentsByKbId(@Param("kbId") Long kbId, @Param("tenantId") Long tenantId);
}