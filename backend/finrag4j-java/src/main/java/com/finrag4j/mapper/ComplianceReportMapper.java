package com.finrag4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finrag4j.entity.ComplianceReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 合规报告Mapper
 */
@Mapper
public interface ComplianceReportMapper extends BaseMapper<ComplianceReport> {

    @Select("SELECT * FROM compliance_report WHERE report_no = #{reportNo}")
    ComplianceReport selectByReportNo(@Param("reportNo") String reportNo);

    @Select("SELECT * FROM compliance_report WHERE tenant_id = #{tenantId}")
    List<ComplianceReport> selectByTenantId(@Param("tenantId") Long tenantId);

    @Select("SELECT * FROM compliance_report WHERE status = #{status}")
    List<ComplianceReport> selectByStatus(@Param("status") String status);
}