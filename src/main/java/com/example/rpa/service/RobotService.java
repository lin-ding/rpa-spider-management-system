package com.example.rpa.service;

import com.example.rpa.dto.AddRobotRequest;
import com.example.rpa.dto.UpdateRobotRequest;
import com.example.rpa.entity.RpaRobot;

public interface RobotService {

    void addRobot(AddRobotRequest request);

    void updateRobot(UpdateRobotRequest request);

    void deleteRobot(Long id);

    boolean checkRobotCodeUnique(String robotCode);

    RpaRobot getRobotById(Long id);
}
