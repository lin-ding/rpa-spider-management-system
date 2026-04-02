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
 * 任务管理 Controller（临时实现）
 */
@RestController
@RequestMapping("/task")
@Tag(name = "任务管理", description = "提供任务列表查询入口，当前为占位实现")
public class TaskController {

    /**
     * 获取任务列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询任务列表", description = "分页获取任务配置列表，当前返回空数据")
    public Result<Map<String, Object>> getTaskList(
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
