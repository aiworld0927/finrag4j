package com.finrag4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finrag4j.entity.Tenant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 租户Mapper
 */
@Mapper
public interface TenantMapper extends BaseMapper<Tenant> {

    @Select("SELECT * FROM tenant WHERE tenant_code = #{tenantCode} AND deleted = 0")
    Tenant selectByCode(@Param("tenantCode") String tenantCode);
}