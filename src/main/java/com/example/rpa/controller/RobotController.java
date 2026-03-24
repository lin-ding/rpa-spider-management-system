package com.example.rpa.controller;

import com.example.rpa.common.Result;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 机器人管理 Controller（临时实现）
 */
@RestController
@RequestMapping("/robot")
public class RobotController {

    /**
     * 获取机器人列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getRobotList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> result = new HashMap<>();
        result.put("records", new ArrayList<>());
        result.put("total", 0);
        result.put("current", current);
        result.put("size", size);
        return Result.success(result);
    }

    /**
     * 获取机器人状态统计
     */
    @GetMapping("/status-statistics")
    public Result<Map<String, Object>> getStatusStatistics() {
        Map<String, Object> result = new HashMap<>();
        result.put("total", 0);
        result.put("online", 0);
        result.put("working", 0);
        result.put("offline", 0);
        result.put("runningTasks", 0);
        return Result.success(result);
    }
}
