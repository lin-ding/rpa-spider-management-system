package com.example.rpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.rpa.dto.AddRobotRequest;
import com.example.rpa.dto.UpdateRobotRequest;
import com.example.rpa.entity.RpaRobot;
import com.example.rpa.exception.BusinessException;
import com.example.rpa.mapper.RobotMapper;
import com.example.rpa.service.RobotService;
import com.example.rpa.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RobotServiceImpl implements RobotService {

    private static final int STATUS_OFFLINE = 3;
    private static final int STATUS_BUSY = 2;

    private final RobotMapper robotMapper;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRobot(AddRobotRequest request) {
        String robotCode = request.getRobotCode().trim();
        if (!checkRobotCodeUnique(robotCode)) {
            throw new BusinessException("机器人编码已存在");
        }

        RpaRobot robot = new RpaRobot();
        robot.setRobotName(request.getRobotName().trim());
        robot.setRobotCode(robotCode);
        robot.setRobotType(request.getRobotType());
        robot.setHostName(trimToNull(request.getHostName()));
        robot.setHostIp(trimToNull(request.getHostIp()));
        robot.setPort(request.getPort());
        robot.setClientVersion(trimToNull(request.getClientVersion()));
        robot.setDescription(trimToNull(request.getDescription()));
        robot.setStatus(STATUS_OFFLINE);
        robot.setCreateBy(securityUtil.getCurrentUserId());
        robot.setCreateTime(LocalDateTime.now());
        robot.setUpdateTime(LocalDateTime.now());
        robot.setDeleted(0);

        robotMapper.insert(robot);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRobot(UpdateRobotRequest request) {
        RpaRobot robot = getRobotById(request.getId());
        robot.setRobotName(request.getRobotName().trim());
        robot.setRobotType(request.getRobotType());
        robot.setHostName(trimToNull(request.getHostName()));
        robot.setHostIp(trimToNull(request.getHostIp()));
        robot.setPort(request.getPort());
        robot.setClientVersion(trimToNull(request.getClientVersion()));
        robot.setDescription(trimToNull(request.getDescription()));
        robot.setUpdateTime(LocalDateTime.now());

        robotMapper.updateById(robot);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRobot(Long id) {
        RpaRobot robot = getRobotById(id);
        if (STATUS_BUSY == safeInt(robot.getStatus()) || robot.getCurrentTaskId() != null) {
            throw new BusinessException("机器人正在执行任务，无法删除");
        }
        robotMapper.deleteById(id);
    }

    @Override
    public boolean checkRobotCodeUnique(String robotCode) {
        if (!StringUtils.hasText(robotCode)) {
            throw new BusinessException("机器人编码不能为空");
        }
        LambdaQueryWrapper<RpaRobot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RpaRobot::getRobotCode, robotCode.trim());
        return robotMapper.selectCount(wrapper) == 0;
    }

    @Override
    public RpaRobot getRobotById(Long id) {
        RpaRobot robot = robotMapper.selectById(id);
        if (robot == null) {
            throw new BusinessException("机器人不存在");
        }
        return robot;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
