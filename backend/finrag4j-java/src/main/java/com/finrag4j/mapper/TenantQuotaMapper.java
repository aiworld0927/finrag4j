package com.finrag4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finrag4j.entity.TenantQuota;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 租户配额Mapper接口
 */
@Mapper
public interface TenantQuotaMapper extends BaseMapper<TenantQuota> {

    @Select("SELECT tq.* FROM tenant_quota tq WHERE tq.tenant_id = #{tenantId}")
    TenantQuota selectByTenantId(@Param("tenantId") Long tenantId);
}