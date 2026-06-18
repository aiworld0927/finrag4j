package com.finrag4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finrag4j.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志Mapper
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {

    @Select("SELECT * FROM audit_log WHERE tenant_id = #{tenantId} " +
            "AND operation_time BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY operation_time DESC")
    List<AuditLog> selectByTimeRange(
            @Param("tenantId") Long tenantId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Select("SELECT * FROM audit_log WHERE user_id = #{userId} " +
            "ORDER BY operation_time DESC")
    List<AuditLog> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM audit_log WHERE operation_module = #{module} " +
            "ORDER BY operation_time DESC")
    List<AuditLog> selectByModule(@Param("module") String module);
}