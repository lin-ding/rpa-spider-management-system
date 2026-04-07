package com.example.rpa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.rpa.common.Result;
import com.example.rpa.dto.AddRobotRequest;
import com.example.rpa.dto.RobotQueryRequest;
import com.example.rpa.dto.UpdateRobotRequest;
import com.example.rpa.entity.Robot;
import com.example.rpa.service.RobotService;
import com.example.rpa.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/robot")
public class RobotController {

    @Autowired
    private RobotService robotService;

    @Autowired
    private SecurityUtil securityUtil;

    @PostMapping
    public Result<Robot> addRobot(@Valid @RequestBody AddRobotRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        Robot robot = robotService.addRobot(request, userId);
        return Result.success(robot);
    }

    @PutMapping("/{id}")
    public Result<Void> updateRobot(@PathVariable Long id, @Valid @RequestBody UpdateRobotRequest request) {
        robotService.updateRobot(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRobot(@PathVariable Long id) {
        robotService.deleteRobot(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Robot> getRobotDetail(@PathVariable Long id) {
        Robot robot = robotService.getRobotDetail(id);
        return Result.success(robot);
    }

    @GetMapping("/list")
    public Result<IPage<Robot>> getRobotList(RobotQueryRequest request) {
        if (request.getCurrent() == null) {
            request.setCurrent(1);
        }
        if (request.getSize() == null) {
            request.setSize(10);
        }
        IPage<Robot> page = robotService.getRobotList(request);
        return Result.success(page);
    }

    @GetMapping("/status-statistics")
    public Result<Map<String, Object>> getStatusStatistics() {
        Map<String, Object> stats = robotService.getStatusStatistics();
        return Result.success(stats);
    }
}
