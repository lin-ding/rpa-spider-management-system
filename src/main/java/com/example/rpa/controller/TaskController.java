package com.example.rpa.controller;

import com.example.rpa.common.Result;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 任务管理 Controller（临时实现）
 */
@RestController
@RequestMapping("/task")
public class TaskController {

    /**
     * 获取任务列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getTaskList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> result = new HashMap<>();
        result.put("records", new ArrayList<>());
        result.put("total", 0);
        result.put("current", current);
        result.put("size", size);
        return Result.success(result);
    }
}
