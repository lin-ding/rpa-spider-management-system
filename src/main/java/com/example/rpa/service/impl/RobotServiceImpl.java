package com.example.rpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.dto.AddRobotRequest;
import com.example.rpa.dto.RobotQueryRequest;
import com.example.rpa.dto.UpdateRobotRequest;
import com.example.rpa.entity.RpaRobot;
import com.example.rpa.exception.BusinessException;
import com.example.rpa.mapper.RobotMapper;
import com.example.rpa.service.RobotService;
import com.example.rpa.util.SecurityUtil;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RobotServiceImpl implements RobotService {

    private static final int STATUS_DISABLED = 0;
    private static final int STATUS_OFFLINE = 3;
    private static final int STATUS_BUSY = 2;
    private static final int STATUS_IDLE = 1;

    private final RobotMapper robotMapper;
    private final SecurityUtil securityUtil;
    private final ConcurrentHashMap<Long, ExecutorService> robotExecutorMap = new ConcurrentHashMap<>();

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
        allocateExecutor(robot.getId());
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
        recycleExecutor(id);
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

    @Override
    public Page<RpaRobot> getRobotPage(RobotQueryRequest request) {
        Page<RpaRobot> page = new Page<>(request.getCurrent(), request.getSize());
        LambdaQueryWrapper<RpaRobot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(request.getStatus() != null, RpaRobot::getStatus, request.getStatus());

        if (StringUtils.hasText(request.getKeyword())) {
            String keyword = request.getKeyword().trim();
            wrapper.and(w -> w.like(RpaRobot::getRobotName, keyword)
                    .or()
                    .like(RpaRobot::getRobotCode, keyword));
        }

        wrapper.orderByDesc(RpaRobot::getUpdateTime);
        return robotMapper.selectPage(page, wrapper);
    }

    @Override
    public Map<String, Object> getStatusStatistics() {
        Map<String, Object> result = new HashMap<>();
        long total = robotMapper.selectCount(new LambdaQueryWrapper<>());
        long idle = robotMapper.selectCount(new LambdaQueryWrapper<RpaRobot>()
                .eq(RpaRobot::getStatus, STATUS_IDLE));
        long working = robotMapper.selectCount(new LambdaQueryWrapper<RpaRobot>()
                .eq(RpaRobot::getStatus, STATUS_BUSY));
        long offline = robotMapper.selectCount(new LambdaQueryWrapper<RpaRobot>()
                .eq(RpaRobot::getStatus, STATUS_OFFLINE));

        result.put("total", total);
        result.put("online", idle + working);
        result.put("working", working);
        result.put("offline", offline);
        result.put("runningTasks", working);
        return result;
    }

    @Override
    public void executeTaskOnRobot(Long robotId, Long taskId, Long processId, Runnable task) {
        RpaRobot robot = getRobotById(robotId);
        int status = safeInt(robot.getStatus());
        if (status == STATUS_DISABLED) {
            throw new BusinessException("机器人已禁用，无法执行任务");
        }
        if (status == STATUS_BUSY || robot.getCurrentTaskId() != null) {
            throw new BusinessException("机器人正在执行任务，请稍后再试");
        }

        ExecutorService executor = allocateExecutor(robotId);
        LocalDateTime now = LocalDateTime.now();
        robot.setStatus(STATUS_BUSY);
        robot.setCurrentTaskId(taskId);
        robot.setCurrentProcessId(processId);
        robot.setLastHeartbeatTime(now);
        robot.setLastOnlineTime(now);
        robot.setUpdateTime(now);
        robotMapper.updateById(robot);

        executor.submit(() -> {
            try {
                task.run();
                recoverRobotStatus(robotId);
            } catch (Exception e) {
                markRobotError(robotId);
                throw e;
            }
        });
    }

    @PreDestroy
    public void shutdownExecutors() {
        robotExecutorMap.keySet().forEach(this::recycleExecutor);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private ExecutorService allocateExecutor(Long robotId) {
        return robotExecutorMap.computeIfAbsent(robotId, id ->
                Executors.newSingleThreadExecutor(r -> {
                    Thread thread = new Thread(r, "robot-executor-" + id);
                    thread.setDaemon(true);
                    return thread;
                })
        );
    }

    private void recycleExecutor(Long robotId) {
        ExecutorService executor = robotExecutorMap.remove(robotId);
        if (executor == null) {
            return;
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void recoverRobotStatus(Long robotId) {
        RpaRobot robot = getRobotById(robotId);
        LocalDateTime now = LocalDateTime.now();
        robot.setStatus(STATUS_IDLE);
        robot.setCurrentTaskId(null);
        robot.setCurrentProcessId(null);
        robot.setLastHeartbeatTime(now);
        robot.setLastOnlineTime(now);
        robot.setUpdateTime(now);
        robotMapper.updateById(robot);
    }

    private void markRobotError(Long robotId) {
        RpaRobot robot = getRobotById(robotId);
        robot.setStatus(4);
        robot.setUpdateTime(LocalDateTime.now());
        robotMapper.updateById(robot);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
