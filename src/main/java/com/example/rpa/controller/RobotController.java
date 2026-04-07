package com.example.rpa.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.annotation.RequireAdmin;
import com.example.rpa.common.Result;
import com.example.rpa.dto.AddRobotRequest;
import com.example.rpa.dto.RobotQueryRequest;
import com.example.rpa.dto.UpdateRobotRequest;
import com.example.rpa.entity.RpaRobot;
import com.example.rpa.service.RobotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/robot")
@RequiredArgsConstructor
@Tag(name = "机器人管理", description = "提供机器人新增、编辑、删除和基础查询接口")
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

    @GetMapping("/{id}")
    @Operation(summary = "查询机器人详情", description = "根据机器人主键 ID 查询机器人详情")
    public Result<RpaRobot> getRobotById(@Parameter(description = "机器人主键 ID", required = true)
                                         @PathVariable Long id) {
        return Result.success(robotService.getRobotById(id));
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询机器人列表", description = "支持按关键字和状态分页查询机器人列表")
    public Result<Page<RpaRobot>> getRobotList(RobotQueryRequest request) {
        return Result.success(robotService.getRobotPage(request));
    }

    @GetMapping("/status-statistics")
    @Operation(summary = "查询机器人状态统计", description = "统计机器人总数、在线数、工作中数量、离线数量和运行中任务数")
    public Result<Map<String, Object>> getStatusStatistics() {
        return Result.success(robotService.getStatusStatistics());
    }
}
