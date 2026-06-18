package com.finrag4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finrag4j.entity.WorkflowDefinition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 工作流定义Mapper
 */
@Mapper
public interface WorkflowDefinitionMapper extends BaseMapper<WorkflowDefinition> {

    @Select("SELECT * FROM workflow_definition WHERE workflow_code = #{workflowCode} AND deleted = 0")
    WorkflowDefinition selectByCode(@Param("workflowCode") String workflowCode);

    @Select("SELECT * FROM workflow_definition WHERE tenant_id = #{tenantId} AND deleted = 0")
    List<WorkflowDefinition> selectByTenantId(@Param("tenantId") Long tenantId);

    @Select("SELECT * FROM workflow_definition WHERE trigger_type = 'cron' AND status = 'active' AND deleted = 0")
    List<WorkflowDefinition> selectCronWorkflows();
}