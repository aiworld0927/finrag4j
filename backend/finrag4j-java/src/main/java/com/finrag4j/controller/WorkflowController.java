package com.finrag4j.controller;

import com.finrag4j.common.response.Result;
import com.finrag4j.entity.WorkflowDefinition;
import com.finrag4j.entity.WorkflowExecutionLog;
import com.finrag4j.entity.WorkflowInstance;
import com.finrag4j.entity.WorkflowTask;
import com.finrag4j.service.WorkflowEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag as ApiTag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工作流管理控制器
 */
@RestController
@RequestMapping("/api/workflow")
@ApiTag(name = "工作流管理")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowEngineService workflowEngineService;

    @Operation(summary = "创建工作流定义")
    @PostMapping("/definitions")
    public Result<WorkflowDefinition> create(
            @RequestBody WorkflowDefinition workflow,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        workflow.setTenantId(tenantId);
        WorkflowDefinition result = workflowEngineService.createWorkflow(workflow);
        return Result.success(result);
    }

    @Operation(summary = "更新工作流定义")
    @PutMapping("/definitions/{id}")
    public Result<WorkflowDefinition> update(
            @Parameter(description = "工作流ID") @PathVariable Long id,
            @RequestBody WorkflowDefinition workflow
    ) {
        WorkflowDefinition result = workflowEngineService.updateWorkflow(id, workflow);
        return Result.success(result);
    }

    @Operation(summary = "删除工作流定义")
    @DeleteMapping("/definitions/{id}")
    public Result<Void> delete(@Parameter(description = "工作流ID") @PathVariable Long id) {
        workflowEngineService.deleteWorkflow(id);
        return Result.success();
    }

    @Operation(summary = "获取工作流列表")
    @GetMapping("/definitions")
    public Result<List<WorkflowDefinition>> list(@Parameter(description = "租户ID") @RequestParam Long tenantId) {
        List<WorkflowDefinition> result = workflowEngineService.getWorkflows(tenantId);
        return Result.success(result);
    }

    @Operation(summary = "触发工作流")
    @PostMapping("/trigger")
    public Result<WorkflowInstance> trigger(
            @Parameter(description = "工作流编码") @RequestParam String workflowCode,
            @Parameter(description = "上下文数据JSON") @RequestParam(defaultValue = "{}") String contextData,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        WorkflowInstance result = workflowEngineService.triggerWorkflow(workflowCode, contextData, tenantId);
        return Result.success(result);
    }

    @Operation(summary = "获取待办任务")
    @GetMapping("/tasks/pending")
    public Result<List<WorkflowTask>> getPendingTasks(@Parameter(description = "用户ID") @RequestParam Long assigneeId) {
        List<WorkflowTask> result = workflowEngineService.getPendingTasks(assigneeId);
        return Result.success(result);
    }

    @Operation(summary = "完成任务")
    @PostMapping("/tasks/{taskId}/complete")
    public Result<WorkflowTask> completeTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @Parameter(description = "备注") @RequestParam(required = false) String comment,
            @Parameter(description = "是否通过") @RequestParam(defaultValue = "true") boolean approved
    ) {
        WorkflowTask result = workflowEngineService.completeTask(taskId, comment, approved);
        return Result.success(result);
    }

    @Operation(summary = "获取工作流执行日志")
    @GetMapping("/instances/{instanceId}/logs")
    public Result<List<WorkflowExecutionLog>> getLogs(@Parameter(description = "实例ID") @PathVariable Long instanceId) {
        List<WorkflowExecutionLog> result = workflowEngineService.getExecutionLogs(instanceId);
        return Result.success(result);
    }
}