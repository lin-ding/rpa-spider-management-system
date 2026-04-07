package com.example.rpa.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.dto.AddRobotRequest;
import com.example.rpa.dto.RobotQueryRequest;
import com.example.rpa.dto.UpdateRobotRequest;
import com.example.rpa.entity.RpaRobot;

import java.util.Map;

public interface RobotService {

    void addRobot(AddRobotRequest request);

    void updateRobot(UpdateRobotRequest request);

    void deleteRobot(Long id);

    boolean checkRobotCodeUnique(String robotCode);

    RpaRobot getRobotById(Long id);

    Page<RpaRobot> getRobotPage(RobotQueryRequest request);

    Map<String, Object> getStatusStatistics();

    void executeTaskOnRobot(Long robotId, Long taskId, Long processId, Runnable task);
}
