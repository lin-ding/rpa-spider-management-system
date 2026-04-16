package com.example.rpa.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.dto.AddRobotRequest;
import com.example.rpa.dto.RobotHeartbeatRequest;
import com.example.rpa.dto.RobotQueryRequest;
import com.example.rpa.dto.TaskExecutionQueryRequest;
import com.example.rpa.dto.UpdateRobotRequest;
import com.example.rpa.entity.RpaRobot;
import com.example.rpa.vo.TaskExecutionListItemVO;

import java.util.Map;

public interface RobotService {

    void addRobot(AddRobotRequest request);

    void updateRobot(UpdateRobotRequest request);

    void updateRobotStatus(Long id, Integer status);

    void deleteRobot(Long id);

    boolean checkRobotCodeUnique(String robotCode);

    RpaRobot getRobotById(Long id);

    Page<RpaRobot> getRobotPage(RobotQueryRequest request);

    Page<TaskExecutionListItemVO> getRobotExecutionPage(Long robotId, TaskExecutionQueryRequest request);

    Map<String, Object> getStatusStatistics();

    void reportHeartbeat(RobotHeartbeatRequest request);

    boolean executeTaskOnRobot(Long robotId, Long taskId, Long processId, Runnable task);

    boolean cancelQueuedTask(Long robotId, Long taskId);
}
