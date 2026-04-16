package com.example.rpa.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.annotation.RequireAdmin;
import com.example.rpa.common.Result;
import com.example.rpa.dto.AddTaskRequest;
import com.example.rpa.dto.TaskExecutionQueryRequest;
import com.example.rpa.dto.TaskQueryRequest;
import com.example.rpa.dto.UpdateTaskRequest;
import com.example.rpa.service.TaskService;
import com.example.rpa.vo.TaskDetailVO;
import com.example.rpa.vo.TaskExecutionListItemVO;
import com.example.rpa.vo.TaskListItemVO;
import com.example.rpa.vo.TaskStageLogVO;
import com.example.rpa.vo.TaskStatisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 任务管理 Controller
 */
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
@Tag(name = "任务管理", description = "提供任务新增、分页查询和删除等基础接口")
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/list")
    @Operation(summary = "分页查询任务列表", description = "支持按任务编码、任务名称、执行状态、流程、机器人和开始时间范围分页查询")
    public Result<Page<TaskListItemVO>> getTaskList(TaskQueryRequest request) {
        return Result.success(taskService.getTaskPage(request));
    }

    @GetMapping("/statistics")
    @Operation(summary = "查询任务统计汇总", description = "统计总任务数、待执行、排队中、执行中、已完成和失败数量")
    public Result<TaskStatisticsVO> getTaskStatistics() {
        return Result.success(taskService.getTaskStatistics());
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询任务详情", description = "根据任务主键 ID 查询任务基础信息以及关联流程、机器人名称")
    public Result<TaskDetailVO> getTaskDetail(@Parameter(description = "任务主键 ID", required = true)
                                              @PathVariable Long id) {
        return Result.success(taskService.getTaskDetail(id));
    }

    @GetMapping("/{id}/executions")
    @Operation(summary = "查询任务执行记录", description = "根据任务主键 ID 分页查询执行历史")
    public Result<Page<TaskExecutionListItemVO>> getTaskExecutions(@Parameter(description = "任务主键 ID", required = true)
                                                                   @PathVariable Long id,
                                                                   TaskExecutionQueryRequest request) {
        return Result.success(taskService.getTaskExecutionPage(id, request));
    }

    @GetMapping("/execution/{executionId}/stages")
    @Operation(summary = "查询执行阶段日志", description = "根据执行记录 ID 查询阶段日志列表")
    public Result<java.util.List<TaskStageLogVO>> getTaskExecutionStages(
            @Parameter(description = "执行记录 ID", required = true)
            @PathVariable Long executionId) {
        return Result.success(taskService.getTaskExecutionStages(executionId));
    }

    @PostMapping
    @RequireAdmin("新增任务")
    @Operation(summary = "新增任务", description = "新增任务配置，并校验任务编码唯一、流程启用和机器人可绑定")
    public Result<Void> addTask(@Valid @RequestBody AddTaskRequest request) {
        taskService.addTask(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    @RequireAdmin("编辑任务")
    @Operation(summary = "编辑任务", description = "更新任务基础信息，执行中的任务不允许编辑")
    public Result<Void> updateTask(@Parameter(description = "任务主键 ID", required = true)
                                   @PathVariable Long id,
                                   @Valid @RequestBody UpdateTaskRequest request) {
        taskService.updateTask(id, request);
        return Result.success();
    }

    @PostMapping("/{id}/execute")
    @RequireAdmin("启动任务执行")
    @Operation(summary = "启动任务执行", description = "创建执行记录并提交到机器人执行流程")
    public Result<Void> executeTask(@Parameter(description = "任务主键 ID", required = true)
                                    @PathVariable Long id) {
        taskService.executeTask(id);
        return Result.success();
    }

    @PostMapping("/{id}/cancel")
    @RequireAdmin("取消任务排队")
    @Operation(summary = "取消任务排队", description = "取消待执行或排队中的任务执行，执行中的任务暂不支持取消")
    public Result<Void> cancelTaskExecution(@Parameter(description = "任务主键 ID", required = true)
                                            @PathVariable Long id) {
        taskService.cancelTaskExecution(id);
        return Result.success();
    }

    @PostMapping("/{id}/retry")
    @RequireAdmin("重试任务执行")
    @Operation(summary = "重试任务执行", description = "仅允许最近一次执行失败且当前未排队/未执行中的任务重试")
    public Result<Void> retryTaskExecution(@Parameter(description = "任务主键 ID", required = true)
                                           @PathVariable Long id) {
        taskService.retryTaskExecution(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireAdmin("删除任务")
    @Operation(summary = "删除任务", description = "逻辑删除任务，若任务正在执行则不允许删除")
    public Result<Void> deleteTask(@Parameter(description = "任务主键 ID", required = true)
                                   @PathVariable Long id) {
        taskService.deleteTask(id);
        return Result.success();
    }
}
