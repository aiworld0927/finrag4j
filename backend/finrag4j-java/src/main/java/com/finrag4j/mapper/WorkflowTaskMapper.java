package com.finrag4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finrag4j.entity.WorkflowTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 工作流任务Mapper
 */
@Mapper
public interface WorkflowTaskMapper extends BaseMapper<WorkflowTask> {

    @Select("SELECT * FROM workflow_task WHERE instance_id = #{instanceId}")
    List<WorkflowTask> selectByInstanceId(@Param("instanceId") Long instanceId);

    @Select("SELECT * FROM workflow_task WHERE assignee_id = #{assigneeId} AND status = 'pending'")
    List<WorkflowTask> selectPendingTasks(@Param("assigneeId") Long assigneeId);

    @Select("SELECT * FROM workflow_task WHERE instance_id = #{instanceId} AND status = 'pending' ORDER BY created_at ASC")
    List<WorkflowTask> selectPendingByInstanceId(@Param("instanceId") Long instanceId);
}