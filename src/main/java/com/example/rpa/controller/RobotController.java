package com.example.rpa.controller;

import com.example.rpa.common.Result;
import com.example.rpa.dto.AddRobotRequest;
import com.example.rpa.dto.UpdateRobotRequest;
import com.example.rpa.service.RobotService;
import com.example.rpa.annotation.RequireAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Tag(name = "机器人管理", description = "提供机器人列表和状态统计查询接口，当前为占位实现")
public class RobotController {

    private final RobotService robotService;

    @PostMapping
    @RequireAdmin("新增机器人")
    @Operation(summary = "新增机器人", description = "新增机器人并校验编码唯一性，初始化状态为离线")
    public Result<Void> addRobot(@Valid @RequestBody AddRobotRequest request) {
        robotService.addRobot(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    @RequireAdmin("修改机器人")
    @Operation(summary = "修改机器人", description = "按机器人 ID 修改名称、类型、IP、端口和描述等信息")
    public Result<Void> updateRobot(@Parameter(description = "机器人主键 ID", required = true)
                                    @PathVariable Long id,
                                    @Valid @RequestBody UpdateRobotRequest request) {
        request.setId(id);
        robotService.updateRobot(request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireAdmin("删除机器人")
    @Operation(summary = "删除机器人", description = "删除前校验机器人是否正在执行任务，满足条件后执行逻辑删除")
    public Result<Void> deleteRobot(@Parameter(description = "机器人主键 ID", required = true)
                                    @PathVariable Long id) {
        robotService.deleteRobot(id);
        return Result.success();
    }

    /**
     * 获取机器人列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询机器人列表", description = "分页获取机器人基础信息列表，当前返回空数据")
    public Result<Map<String, Object>> getRobotList(
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

    /**
     * 获取机器人状态统计
     */
    @GetMapping("/status-statistics")
    @Operation(summary = "查询机器人状态统计", description = "统计机器人总数、在线数、工作中数量、离线数量和运行中任务数，当前返回 0 值占位数据")
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
