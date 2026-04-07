package com.example.rpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.rpa.dto.AddRobotRequest;
import com.example.rpa.dto.RobotQueryRequest;
import com.example.rpa.dto.UpdateRobotRequest;
import com.example.rpa.entity.Robot;
import com.example.rpa.exception.BusinessException;
import com.example.rpa.mapper.RobotMapper;
import com.example.rpa.service.RobotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Service
public class RobotServiceImpl extends ServiceImpl<RobotMapper, Robot> implements RobotService {

    @Autowired
    private RobotMapper robotMapper;

    private final ConcurrentHashMap<Long, ExecutorService> robotExecutorMap = new ConcurrentHashMap<>();

    @Override
    public Robot addRobot(AddRobotRequest request, Long userId) {
        LambdaQueryWrapper<Robot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Robot::getRobotCode, request.getRobotCode());
        if (robotMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("机器人编码已存在");
        }

        Robot robot = new Robot();
        robot.setRobotCode(request.getRobotCode());
        robot.setRobotName(request.getRobotName());
        robot.setType(request.getType());
        robot.setIp(request.getIp());
        robot.setPort(request.getPort());
        robot.setStatus("online");
        robot.setDescription(request.getDescription());
        robot.setRemark(request.getRemark());
        robot.setCreateBy(userId);

        robotMapper.insert(robot);
        allocateExecutor(robot.getId());
        log.info("创建机器人成功，已分配专属线程: id={}, code={}", robot.getId(), robot.getRobotCode());
        return robot;
    }

    @Override
    public void updateRobot(Long id, UpdateRobotRequest request) {
        Robot robot = robotMapper.selectById(id);
        if (robot == null) {
            throw new BusinessException("机器人不存在");
        }
        robot.setRobotName(request.getRobotName());
        if (request.getType() != null) {
            robot.setType(request.getType());
        }
        if (request.getIp() != null) {
            robot.setIp(request.getIp());
        }
        if (request.getPort() != null) {
            robot.setPort(request.getPort());
        }
        robot.setDescription(request.getDescription());
        robot.setRemark(request.getRemark());
        robotMapper.updateById(robot);
    }

    @Override
    public void deleteRobot(Long id) {
        Robot robot = robotMapper.selectById(id);
        if (robot == null) {
            throw new BusinessException("机器人不存在");
        }
        recycleExecutor(id);
        robotMapper.deleteById(id);
        log.info("删除机器人成功，已回收线程: id={}, code={}", id, robot.getRobotCode());
    }

    @Override
    public Robot getRobotDetail(Long id) {
        Robot robot = robotMapper.selectById(id);
        if (robot == null) {
            throw new BusinessException("机器人不存在");
        }
        return robot;
    }

    @Override
    public IPage<Robot> getRobotList(RobotQueryRequest request) {
        Page<Robot> page = new Page<>(request.getCurrent(), request.getSize());
        LambdaQueryWrapper<Robot> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(Robot::getStatus, request.getStatus());
        }
        if (StringUtils.hasText(request.getRobotName())) {
            wrapper.and(w -> w.like(Robot::getRobotName, request.getRobotName())
                    .or().like(Robot::getRobotCode, request.getRobotName()));
        }

        wrapper.orderByDesc(Robot::getUpdateTime);
        return robotMapper.selectPage(page, wrapper);
    }

    @Override
    public void executeTaskOnRobot(Long robotId, Long taskId, String taskName, Runnable task) {
        Robot robot = robotMapper.selectById(robotId);
        if (robot == null) {
            throw new BusinessException("机器人不存在");
        }
        if (!"online".equals(robot.getStatus()) && !"working".equals(robot.getStatus())) {
            throw new BusinessException("机器人当前不可用，状态: " + robot.getStatus());
        }
        if ("working".equals(robot.getStatus())) {
            throw new BusinessException("机器人正在执行任务，请稍后再试");
        }

        ExecutorService executor = robotExecutorMap.get(robotId);
        if (executor == null) {
            executor = allocateExecutor(robotId);
        }

        robot.setCurrentTaskId(String.valueOf(taskId));
        robot.setCurrentTaskName(taskName);
        robot.setStatus("working");
        robot.setLastHeartbeat(LocalDateTime.now());
        robotMapper.updateById(robot);

        executor.submit(() -> {
            try {
                task.run();
                log.info("机器人 [{}] 任务执行完成: taskId={}", robot.getRobotCode(), taskId);
            } catch (Exception e) {
                log.error("机器人 [{}] 任务执行异常: taskId={}, error={}", robot.getRobotCode(), taskId, e.getMessage(), e);
            } finally {
                recoverRobotStatus(robotId);
            }
        });
        log.info("机器人 [{}] 开始执行任务: taskId={}", robot.getRobotCode(), taskId);
    }

    @Override
    public Map<String, Object> getStatusStatistics() {
        Map<String, Object> stats = new HashMap<>();
        long total = robotMapper.selectCount(new LambdaQueryWrapper<Robot>());
        long online = robotMapper.selectCount(new LambdaQueryWrapper<Robot>().eq(Robot::getStatus, "online"));
        long working = robotMapper.selectCount(new LambdaQueryWrapper<Robot>().eq(Robot::getStatus, "working"));
        long offline = robotMapper.selectCount(new LambdaQueryWrapper<Robot>().eq(Robot::getStatus, "offline"));

        stats.put("total", total);
        stats.put("online", online);
        stats.put("working", working);
        stats.put("offline", offline);
        stats.put("runningTasks", working);
        return stats;
    }

    private ExecutorService allocateExecutor(Long robotId) {
        return robotExecutorMap.computeIfAbsent(robotId, id -> {
            log.info("为机器人 [{}] 分配专属单线程执行器", id);
            return Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "robot-executor-" + id);
                t.setDaemon(true);
                return t;
            });
        });
    }

    private void recycleExecutor(Long robotId) {
        ExecutorService executor = robotExecutorMap.remove(robotId);
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    log.warn("机器人 [{}] 线程池强制关闭", robotId);
                } else {
                    log.info("机器人 [{}] 线程池已回收", robotId);
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
                log.warn("机器人 [{}] 线程池回收被中断", robotId);
            }
        }
    }

    private void recoverRobotStatus(Long robotId) {
        try {
            Robot robot = robotMapper.selectById(robotId);
            if (robot != null) {
                robot.setStatus("online");
                robot.setCurrentTaskId(null);
                robot.setCurrentTaskName(null);
                robot.setLastHeartbeat(LocalDateTime.now());
                robotMapper.updateById(robot);
                log.info("机器人 [{}] 状态恢复为 online", robot.getRobotCode());
            }
        } catch (Exception e) {
            log.error("恢复机器人 [{}] 状态失败", robotId, e);
        }
    }
}
