package com.example.rpa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.rpa.dto.AddRobotRequest;
import com.example.rpa.dto.RobotQueryRequest;
import com.example.rpa.dto.UpdateRobotRequest;
import com.example.rpa.entity.Robot;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.Map;

public interface RobotService extends IService<Robot> {

    Robot addRobot(AddRobotRequest request, Long userId);

    void updateRobot(Long id, UpdateRobotRequest request);

    void deleteRobot(Long id);

    Robot getRobotDetail(Long id);

    IPage<Robot> getRobotList(RobotQueryRequest request);

    void executeTaskOnRobot(Long robotId, Long taskId, String taskName, Runnable task);

    Map<String, Object> getStatusStatistics();
}
