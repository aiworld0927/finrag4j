package com.finrag4j.service;

import com.finrag4j.common.exception.BusinessException;
import com.finrag4j.entity.*;
import com.finrag4j.mapper.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 可视化工作流引擎服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowEngineService {

    private final WorkflowDefinitionMapper workflowDefinitionMapper;
    private final WorkflowInstanceMapper workflowInstanceMapper;
    private final WorkflowTaskMapper workflowTaskMapper;
    private final WorkflowExecutionLogMapper executionLogMapper;
    private final ObjectMapper objectMapper;

    // ==================== 工作流定义管理 ====================

    /**
     * 创建工作流定义
     */
    @Transactional
    public WorkflowDefinition createWorkflow(WorkflowDefinition workflow) {
        if (workflowDefinitionMapper.selectByCode(workflow.getWorkflowCode()) != null) {
            throw new BusinessException("工作流编码已存在");
        }
        workflowDefinitionMapper.insert(workflow);
        log.info("创建工作流定义: {}", workflow.getWorkflowName());
        return workflow;
    }

    /**
     * 更新工作流定义
     */
    @Transactional
    public WorkflowDefinition updateWorkflow(Long id, WorkflowDefinition workflow) {
        WorkflowDefinition existing = workflowDefinitionMapper.selectById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("工作流不存在");
        }
        workflow.setId(id);
        workflowDefinitionMapper.updateById(workflow);
        return workflow;
    }

    /**
     * 删除工作流定义
     */
    @Transactional
    public void deleteWorkflow(Long id) {
        WorkflowDefinition workflow = workflowDefinitionMapper.selectById(id);
        if (workflow == null) {
            throw new BusinessException("工作流不存在");
        }
        workflow.setDeleted(1);
        workflowDefinitionMapper.updateById(workflow);
        log.info("删除工作流定义: {}", workflow.getWorkflowName());
    }

    /**
     * 获取工作流列表
     */
    public List<WorkflowDefinition> getWorkflows(Long tenantId) {
        return workflowDefinitionMapper.selectByTenantId(tenantId);
    }

    // ==================== 工作流执行 ====================

    /**
     * 手动触发工作流
     */
    @Transactional
    public WorkflowInstance triggerWorkflow(String workflowCode, String contextData, Long tenantId) {
        WorkflowDefinition workflow = workflowDefinitionMapper.selectByCode(workflowCode);
        if (workflow == null || workflow.getStatus() != "active") {
            throw new BusinessException("工作流不存在或未启用");
        }

        String instanceNo = generateInstanceNo();
        
        WorkflowInstance instance = WorkflowInstance.builder()
                .workflowId(workflow.getId())
                .instanceNo(instanceNo)
                .status("running")
                .contextData(contextData)
                .build();
        
        workflowInstanceMapper.insert(instance);
        
        // 记录执行日志
        logExecution(instance.getId(), null, "INFO", "工作流启动");
        
        // 开始执行流程
        executeWorkflow(instance.getId(), workflow);
        
        return instance;
    }

    /**
     * 定时触发工作流
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void triggerCronWorkflows() {
        List<WorkflowDefinition> cronWorkflows = workflowDefinitionMapper.selectCronWorkflows();
        
        for (WorkflowDefinition workflow : cronWorkflows) {
            try {
                triggerWorkflow(workflow.getWorkflowCode(), "{}", workflow.getTenantId());
            } catch (Exception e) {
                log.error("定时触发工作流失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 执行工作流
     */
    @Transactional
    public void executeWorkflow(Long instanceId, WorkflowDefinition workflow) {
        try {
            // 解析工作流定义
            WorkflowJson workflowJson = parseWorkflowJson(workflow.getWorkflowJson());
            
            // 执行流程节点
            executeNodes(instanceId, workflowJson.getNodes(), workflowJson.getConnections(), "start");
            
        } catch (Exception e) {
            log.error("执行工作流失败: {}", e.getMessage());
            logExecution(instanceId, null, "ERROR", e.getMessage());
            
            WorkflowInstance instance = workflowInstanceMapper.selectById(instanceId);
            if (instance != null) {
                instance.setStatus("failed");
                workflowInstanceMapper.updateById(instance);
            }
        }
    }

    /**
     * 解析工作流JSON
     */
    private WorkflowJson parseWorkflowJson(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, WorkflowJson.class);
    }

    /**
     * 执行节点
     */
    private void executeNodes(Long instanceId, List<Node> nodes, List<Connection> connections, String currentNodeId) {
        // 查找当前节点
        Node currentNode = nodes.stream()
                .filter(n -> n.getId().equals(currentNodeId))
                .findFirst()
                .orElse(null);
        
        if (currentNode == null) {
            logExecution(instanceId, null, "ERROR", "节点不存在: " + currentNodeId);
            return;
        }
        
        // 执行节点
        executeNode(instanceId, currentNode);
        
        // 获取下一节点
        List<String> nextNodeIds = getNextNodes(connections, currentNodeId);
        
        if (nextNodeIds.isEmpty()) {
            // 流程结束
            completeWorkflow(instanceId);
            return;
        }
        
        // 并行执行多个节点
        for (String nextNodeId : nextNodeIds) {
            executeNodes(instanceId, nodes, connections, nextNodeId);
        }
    }

    /**
     * 执行单个节点
     */
    private void executeNode(Long instanceId, Node node) {
        logExecution(instanceId, null, "INFO", "执行节点: " + node.getName());
        
        switch (node.getType()) {
            case "start":
                // 开始节点，无需特殊处理
                break;
                
            case "end":
                // 结束节点
                completeWorkflow(instanceId);
                break;
                
            case "task":
                // 自动任务节点
                executeAutomaticTask(instanceId, node);
                break;
                
            case "approval":
                // 人工审批节点
                createApprovalTask(instanceId, node);
                break;
                
            case "review":
                // 复核节点
                createReviewTask(instanceId, node);
                break;
                
            case "branch":
                // 分支节点
                executeBranch(instanceId, node);
                break;
                
            default:
                logExecution(instanceId, null, "WARN", "未知节点类型: " + node.getType());
        }
    }

    /**
     * 执行自动任务
     */
    private void executeAutomaticTask(Long instanceId, Node node) {
        // 根据任务类型执行不同的业务逻辑
        String taskType = node.getConfig().get("taskType");
        
        logExecution(instanceId, null, "INFO", "执行自动任务: " + taskType);
        
        // 模拟任务执行
        try {
            Thread.sleep(1000); // 模拟执行时间
            logExecution(instanceId, null, "INFO", "自动任务完成: " + taskType);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logExecution(instanceId, null, "ERROR", "自动任务中断");
        }
    }

    /**
     * 创建审批任务
     */
    private void createApprovalTask(Long instanceId, Node node) {
        Long assigneeId = Long.parseLong(node.getConfig().get("assignee"));
        
        WorkflowTask task = WorkflowTask.builder()
                .instanceId(instanceId)
                .taskName(node.getName())
                .taskType("approval")
                .assigneeId(assigneeId)
                .status("pending")
                .taskData(node.getConfig().toString())
                .build();
        
        workflowTaskMapper.insert(task);
        
        logExecution(instanceId, task.getId(), "INFO", "创建审批任务: " + node.getName());
    }

    /**
     * 创建复核任务
     */
    private void createReviewTask(Long instanceId, Node node) {
        Long assigneeId = Long.parseLong(node.getConfig().get("assignee"));
        
        WorkflowTask task = WorkflowTask.builder()
                .instanceId(instanceId)
                .taskName(node.getName())
                .taskType("review")
                .assigneeId(assigneeId)
                .status("pending")
                .taskData(node.getConfig().toString())
                .build();
        
        workflowTaskMapper.insert(task);
        
        logExecution(instanceId, task.getId(), "INFO", "创建复核任务: " + node.getName());
    }

    /**
     * 执行分支
     */
    private void executeBranch(Long instanceId, Node node) {
        String condition = node.getConfig().get("condition");
        
        logExecution(instanceId, null, "INFO", "分支判断: " + condition);
        
        // 简化的分支逻辑
        boolean conditionResult = evaluateCondition(condition);
        
        // 标记分支结果到上下文中
        WorkflowInstance instance = workflowInstanceMapper.selectById(instanceId);
        if (instance != null) {
            String context = instance.getContextData();
            try {
                Map<String, Object> contextMap = objectMapper.readValue(context, new TypeReference<Map<String, Object>>() {});
                contextMap.put("branchResult", conditionResult);
                instance.setContextData(objectMapper.writeValueAsString(contextMap));
                workflowInstanceMapper.updateById(instance);
            } catch (JsonProcessingException e) {
                log.error("更新上下文失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 评估条件
     */
    private boolean evaluateCondition(String condition) {
        // 简化实现
        return true;
    }

    /**
     * 获取下一节点
     */
    private List<String> getNextNodes(List<Connection> connections, String currentNodeId) {
        return connections.stream()
                .filter(c -> c.getSource().equals(currentNodeId))
                .map(Connection::getTarget)
                .collect(Collectors.toList());
    }

    /**
     * 完成工作流
     */
    @Transactional
    public void completeWorkflow(Long instanceId) {
        WorkflowInstance instance = workflowInstanceMapper.selectById(instanceId);
        if (instance != null) {
            instance.setStatus("completed");
            instance.setCompletedAt(LocalDateTime.now());
            workflowInstanceMapper.updateById(instance);
            
            logExecution(instanceId, null, "INFO", "工作流完成");
            log.info("工作流完成: instanceId={}", instanceId);
        }
    }

    // ==================== 任务处理 ====================

    /**
     * 获取待办任务
     */
    public List<WorkflowTask> getPendingTasks(Long assigneeId) {
        return workflowTaskMapper.selectPendingTasks(assigneeId);
    }

    /**
     * 完成任务
     */
    @Transactional
    public WorkflowTask completeTask(Long taskId, String comment, boolean approved) {
        WorkflowTask task = workflowTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        if (!"pending".equals(task.getStatus())) {
            throw new BusinessException("任务状态不正确");
        }
        
        task.setStatus(approved ? "completed" : "rejected");
        task.setComment(comment);
        task.setCompletedAt(LocalDateTime.now());
        
        workflowTaskMapper.updateById(task);
        
        logExecution(task.getInstanceId(), taskId, "INFO", "任务完成: " + task.getTaskName());
        
        // 检查是否所有待办任务都已完成
        checkAndContinueWorkflow(task.getInstanceId());
        
        return task;
    }

    /**
     * 检查并继续工作流
     */
    private void checkAndContinueWorkflow(Long instanceId) {
        List<WorkflowTask> pendingTasks = workflowTaskMapper.selectPendingByInstanceId(instanceId);
        
        if (pendingTasks.isEmpty()) {
            // 所有待办任务已完成，继续执行流程
            WorkflowInstance instance = workflowInstanceMapper.selectById(instanceId);
            if (instance != null && "running".equals(instance.getStatus())) {
                WorkflowDefinition workflow = workflowDefinitionMapper.selectById(instance.getWorkflowId());
                if (workflow != null) {
                    executeWorkflow(instanceId, workflow);
                }
            }
        }
    }

    // ==================== 执行日志 ====================

    private void logExecution(Long instanceId, Long taskId, String level, String message) {
        WorkflowExecutionLog logEntry = WorkflowExecutionLog.builder()
                .instanceId(instanceId)
                .taskId(taskId)
                .logLevel(level)
                .message(message)
                .build();
        
        executionLogMapper.insert(logEntry);
    }

    /**
     * 获取工作流执行日志
     */
    public List<WorkflowExecutionLog> getExecutionLogs(Long instanceId) {
        return executionLogMapper.selectByInstanceId(instanceId);
    }

    // ==================== 辅助方法 ====================

    private String generateInstanceNo() {
        return "WF-" + System.currentTimeMillis();
    }

    // ==================== 内部类 ====================

    public static class WorkflowJson {
        private List<Node> nodes;
        private List<Connection> connections;

        public List<Node> getNodes() { return nodes; }
        public void setNodes(List<Node> nodes) { this.nodes = nodes; }
        public List<Connection> getConnections() { return connections; }
        public void setConnections(List<Connection> connections) { this.connections = connections; }
    }

    public static class Node {
        private String id;
        private String name;
        private String type; // start, end, task, approval, review, branch
        private Map<String, String> config;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Map<String, String> getConfig() { return config; }
        public void setConfig(Map<String, String> config) { this.config = config; }
    }

    public static class Connection {
        private String source;
        private String target;

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }
    }
}