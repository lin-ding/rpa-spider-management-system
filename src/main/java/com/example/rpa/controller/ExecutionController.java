package com.example.rpa.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.common.Result;
import com.example.rpa.dto.ExecutionQueryRequest;
import com.example.rpa.service.ExecutionService;
import com.example.rpa.vo.TaskExecutionListItemVO;
import com.example.rpa.vo.TaskStageLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/execution")
@RequiredArgsConstructor
@Tag(name = "执行监控", description = "提供执行记录分页查询和阶段日志查看能力")
public class ExecutionController {

    private final ExecutionService executionService;

    @GetMapping("/list")
    @Operation(summary = "分页查询执行记录", description = "支持按任务名称、机器人名称、状态和开始时间范围查询执行记录")
    public Result<Page<TaskExecutionListItemVO>> getExecutionList(ExecutionQueryRequest request) {
        return Result.success(executionService.getExecutionPage(request));
    }

    @GetMapping("/{id}/stages")
    @Operation(summary = "查询执行阶段日志", description = "根据执行记录 ID 查询阶段日志")
    public Result<List<TaskStageLogVO>> getExecutionStages(
            @Parameter(description = "执行记录 ID", required = true)
            @PathVariable Long id) {
        return Result.success(executionService.getStageLogs(id));
    }
}
