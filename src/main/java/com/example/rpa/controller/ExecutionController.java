package com.example.rpa.controller;

import com.example.rpa.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 执行监控 Controller（临时实现）
 */
@RestController
@RequestMapping("/execution")
@Tag(name = "执行监控", description = "提供流程或任务执行记录的查询入口，当前为占位实现")
public class ExecutionController {

    /**
     * 获取执行记录列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询执行记录", description = "分页获取任务或流程的执行记录列表，当前返回空数据")
    public Result<Map<String, Object>> getExecutionList(
            @Parameter(description = "当前页码", example = "1")
            @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页条数", example = "10")
            @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> result = new HashMap<>();
        result.put("records", new ArrayList<>());
        result.put("total", 0);
        result.put("current", current);
        result.put("size", size);
        return Result.success(result);
    }
}
