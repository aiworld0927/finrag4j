package com.finrag4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finrag4j.entity.ExtractTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 抽取模板Mapper
 */
@Mapper
public interface ExtractTemplateMapper extends BaseMapper<ExtractTemplate> {

    @Select("SELECT * FROM extract_template WHERE template_code = #{templateCode} AND deleted = 0")
    ExtractTemplate selectByCode(@Param("templateCode") String templateCode);

    @Select("SELECT * FROM extract_template WHERE tenant_id = #{tenantId} AND deleted = 0")
    List<ExtractTemplate> selectByTenantId(@Param("tenantId") Long tenantId);
}