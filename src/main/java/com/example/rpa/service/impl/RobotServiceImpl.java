package com.example.rpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.dto.AddRobotRequest;
import com.example.rpa.dto.RobotHeartbeatRequest;
import com.example.rpa.dto.RobotQueryRequest;
import com.example.rpa.dto.TaskExecutionQueryRequest;
import com.example.rpa.dto.UpdateRobotRequest;
import com.example.rpa.entity.RpaTask;
import com.example.rpa.entity.RpaTaskExecution;
import com.example.rpa.entity.RpaRobot;
import com.example.rpa.exception.BusinessException;
import com.example.rpa.mapper.RobotMapper;
import com.example.rpa.mapper.RpaTaskMapper;
import com.example.rpa.mapper.RpaTaskExecutionMapper;
import com.example.rpa.service.RobotService;
import com.example.rpa.util.SecurityUtil;
import com.example.rpa.vo.TaskExecutionListItemVO;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RobotServiceImpl implements RobotService {

    private static final int STATUS_DISABLED = 0;
    private static final int STATUS_IDLE = 1;
    private static final int STATUS_BUSY = 2;
    private static final int STATUS_OFFLINE = 3;
    private static final int STATUS_ERROR = 4;
    private static final String TASK_STATUS_QUEUED = "queued";
    private static final String TASK_STATUS_RUNNING = "running";

    private final RobotMapper robotMapper;
    private final RpaTaskMapper rpaTaskMapper;
    private final RpaTaskExecutionMapper rpaTaskExecutionMapper;
    private final SecurityUtil securityUtil;
    // 每个机器人绑定一个单线程执行器，避免同一机器人并发执行多个任务。
    private final ConcurrentHashMap<Long, ThreadPoolExecutor> robotExecutorMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Future<?>> queuedTaskFutureMap = new ConcurrentHashMap<>();

    @Value("${robot.heartbeat.timeout-seconds:120}")
    private long heartbeatTimeoutSeconds;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRobot(AddRobotRequest request) {
        String robotCode = request.getRobotCode().trim();
        if (!checkRobotCodeUnique(robotCode)) {
            throw new BusinessException("机器人编码已存在");
        }
        LocalDateTime now = LocalDateTime.now();

        RpaRobot robot = new RpaRobot();
        robot.setRobotName(request.getRobotName().trim());
        robot.setRobotCode(robotCode);
        robot.setRobotType(request.getRobotType());
        robot.setHostName(trimToNull(request.getHostName()));
        robot.setHostIp(trimToNull(request.getHostIp()));
        robot.setPort(request.getPort());
        robot.setClientVersion(trimToNull(request.getClientVersion()));
        robot.setDescription(trimToNull(request.getDescription()));
        // 控制台手工创建的机器人默认视为可用，避免每次再手动切到空闲。
        robot.setStatus(STATUS_IDLE);
        robot.setLastHeartbeatTime(now);
        robot.setLastOnlineTime(now);
        robot.setCreateBy(securityUtil.getCurrentUserId());
        robot.setCreateTime(now);
        robot.setUpdateTime(now);
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
        applyManualStatus(robot, request.getStatus());
        robot.setUpdateTime(LocalDateTime.now());

        robotMapper.updateById(robot);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRobotStatus(Long id, Integer status) {
        RpaRobot robot = getRobotById(id);
        applyManualStatus(robot, status);
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
        reconcileRobotOccupancy();
        Page<RpaRobot> page = new Page<>(request.getCurrent(), request.getSize());
        LambdaQueryWrapper<RpaRobot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(request.getStatus() != null, RpaRobot::getStatus, request.getStatus());

        if (StringUtils.hasText(request.getKeyword())) {
            String keyword = request.getKeyword().trim();
            // 关键字同时匹配名称和编码，便于列表页模糊搜索。
            wrapper.and(w -> w.like(RpaRobot::getRobotName, keyword)
                    .or()
                    .like(RpaRobot::getRobotCode, keyword));
        }

        wrapper.orderByDesc(RpaRobot::getUpdateTime);
        return robotMapper.selectPage(page, wrapper);
    }

    @Override
    public Page<TaskExecutionListItemVO> getRobotExecutionPage(Long robotId, TaskExecutionQueryRequest request) {
        getRobotById(robotId);

        Page<RpaTaskExecution> page = new Page<>(request.getCurrent(), request.getSize());
        LambdaQueryWrapper<RpaTaskExecution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RpaTaskExecution::getRobotId, robotId)
                .orderByDesc(RpaTaskExecution::getCreateTime);

        Page<RpaTaskExecution> executionPage = rpaTaskExecutionMapper.selectPage(page, wrapper);
        Page<TaskExecutionListItemVO> resultPage =
                new Page<>(executionPage.getCurrent(), executionPage.getSize(), executionPage.getTotal());
        resultPage.setRecords(executionPage.getRecords().stream().map(this::toExecutionListItem).toList());
        return resultPage;
    }

    @Override
    public Map<String, Object> getStatusStatistics() {
        reconcileRobotOccupancy();
        Map<String, Object> result = new HashMap<>();
        long total = robotMapper.selectCount(new LambdaQueryWrapper<>());
        long idle = robotMapper.selectCount(new LambdaQueryWrapper<RpaRobot>()
                .eq(RpaRobot::getStatus, STATUS_IDLE));
        long working = robotMapper.selectCount(new LambdaQueryWrapper<RpaRobot>()
                .eq(RpaRobot::getStatus, STATUS_BUSY));
        long offline = robotMapper.selectCount(new LambdaQueryWrapper<RpaRobot>()
                .eq(RpaRobot::getStatus, STATUS_OFFLINE));

        result.put("total", total);
        // 在线数按空闲 + 工作中统计，禁用和离线都不计入在线。
        result.put("online", idle + working);
        result.put("working", working);
        result.put("offline", offline);
        // 当前实现里运行中任务数与工作中机器人数量保持一致。
        result.put("runningTasks", working);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reportHeartbeat(RobotHeartbeatRequest request) {
        reconcileRobotOccupancy();
        RpaRobot robot = getRobotByCode(request.getRobotCode());
        if (safeInt(robot.getStatus()) == STATUS_DISABLED) {
            throw new BusinessException("机器人已禁用，无法上报心跳");
        }

        LocalDateTime now = LocalDateTime.now();
        robot.setLastHeartbeatTime(now);
        if (shouldRefreshOnlineTime(robot)) {
            // 离线或异常后的首次有效心跳，刷新最近在线时间。
            robot.setLastOnlineTime(now);
        }
        if (shouldRecoverToOnline(robot)) {
            // 心跳可以把离线/异常机器人恢复为在线，是否忙碌取决于当前任务占用情况。
            robot.setStatus(robot.getCurrentTaskId() == null ? STATUS_IDLE : STATUS_BUSY);
        }
        robot.setUpdateTime(now);
        robotMapper.updateById(robot);
    }

    @Override
    public boolean executeTaskOnRobot(Long robotId, Long taskId, Long processId, Runnable task) {
        RpaRobot robot = getRobotById(robotId);
        int status = safeInt(robot.getStatus());
        if (status == STATUS_DISABLED) {
            throw new BusinessException("机器人已禁用，无法执行任务");
        }
        if (status == STATUS_OFFLINE) {
            throw new BusinessException("机器人已离线，无法执行任务");
        }
        if (status == STATUS_ERROR) {
            throw new BusinessException("机器人状态异常，无法执行任务");
        }

        ThreadPoolExecutor executor = allocateExecutor(robotId);
        boolean queued = isTaskQueued(robot, executor);

        // 单线程执行器会自动串行处理队列中的任务。
        Future<?> future = executor.submit(() -> {
            boolean occupied = false;
            boolean released = false;
            try {
                occupyRobot(robotId, taskId, processId);
                occupied = true;
                task.run();
            } catch (Exception e) {
                if (occupied) {
                    recoverRobotStatus(robotId);
                    released = true;
                } else {
                    markRobotError(robotId);
                }
                throw e;
            } finally {
                if (occupied && !released) {
                    recoverRobotStatus(robotId);
                }
                queuedTaskFutureMap.remove(taskId);
            }
        });
        queuedTaskFutureMap.put(taskId, future);
        return queued;
    }

    @Override
    public boolean cancelQueuedTask(Long robotId, Long taskId) {
        ThreadPoolExecutor executor = robotExecutorMap.get(robotId);
        Future<?> future = queuedTaskFutureMap.get(taskId);
        if (executor == null || future == null) {
            return false;
        }
        boolean cancelled = future.cancel(false);
        if (cancelled) {
            executor.remove((Runnable) future);
            queuedTaskFutureMap.remove(taskId);
        }
        return cancelled;
    }

    @PreDestroy
    public void shutdownExecutors() {
        robotExecutorMap.keySet().forEach(this::recycleExecutor);
    }

    @Scheduled(
            fixedDelayString = "${robot.heartbeat.check-interval-ms:30000}",
            initialDelayString = "${robot.heartbeat.check-interval-ms:30000}"
    )
    @Transactional(rollbackFor = Exception.class)
    public void markTimeoutRobotsOffline() {
        // 超过心跳超时时间仍未上报的机器人，统一回收为离线状态。
        LocalDateTime expireBefore = LocalDateTime.now().minusSeconds(heartbeatTimeoutSeconds);
        LambdaQueryWrapper<RpaRobot> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(RpaRobot::getStatus, STATUS_DISABLED)
                .ne(RpaRobot::getStatus, STATUS_OFFLINE)
                .lt(RpaRobot::getLastHeartbeatTime, expireBefore);

        List<RpaRobot> timeoutRobots = robotMapper.selectList(wrapper);
        for (RpaRobot robot : timeoutRobots) {
            markRobotOffline(robot);
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private RpaRobot getRobotByCode(String robotCode) {
        if (!StringUtils.hasText(robotCode)) {
            throw new BusinessException("机器人编码不能为空");
        }
        LambdaQueryWrapper<RpaRobot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RpaRobot::getRobotCode, robotCode.trim());
        RpaRobot robot = robotMapper.selectOne(wrapper);
        if (robot == null) {
            throw new BusinessException("机器人不存在");
        }
        return robot;
    }

    private ThreadPoolExecutor allocateExecutor(Long robotId) {
        return robotExecutorMap.computeIfAbsent(robotId, id ->
                new ThreadPoolExecutor(
                        1,
                        1,
                        0L,
                        TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<>(),
                        r -> {
                            Thread thread = new Thread(r, "robot-executor-" + id);
                            thread.setDaemon(true);
                            return thread;
                        }
                )
        );
    }

    private void recycleExecutor(Long robotId) {
        ThreadPoolExecutor executor = robotExecutorMap.remove(robotId);
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

    private boolean isTaskQueued(RpaRobot robot, ThreadPoolExecutor executor) {
        return safeInt(robot.getStatus()) == STATUS_BUSY
                || robot.getCurrentTaskId() != null
                || executor.getActiveCount() > 0
                || !executor.getQueue().isEmpty();
    }

    private void occupyRobot(Long robotId, Long taskId, Long processId) {
        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<RpaRobot> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(RpaRobot::getId, robotId)
                .set(RpaRobot::getStatus, STATUS_BUSY)
                .set(RpaRobot::getCurrentTaskId, taskId)
                .set(RpaRobot::getCurrentProcessId, processId)
                .set(RpaRobot::getLastHeartbeatTime, now)
                .set(RpaRobot::getLastOnlineTime, now)
                .set(RpaRobot::getUpdateTime, now);
        robotMapper.update(null, wrapper);
    }

    private void applyManualStatus(RpaRobot robot, Integer targetStatus) {
        if (targetStatus == null || targetStatus.equals(robot.getStatus())) {
            return;
        }
        if (safeInt(robot.getStatus()) == STATUS_BUSY || robot.getCurrentTaskId() != null) {
            throw new BusinessException("机器人正在执行任务，无法修改状态");
        }
        if (targetStatus == STATUS_BUSY) {
            throw new BusinessException("不允许手动设置机器人为工作中状态");
        }

        switch (targetStatus) {
            case STATUS_DISABLED, STATUS_OFFLINE, STATUS_ERROR -> {
                clearRobotOccupancy(robot);
                robot.setStatus(targetStatus);
            }
            case STATUS_IDLE -> {
                LocalDateTime now = LocalDateTime.now();
                clearRobotOccupancy(robot);
                robot.setStatus(STATUS_IDLE);
                robot.setLastHeartbeatTime(now);
                robot.setLastOnlineTime(now);
            }
            default -> throw new BusinessException("机器人状态不合法");
        }
    }

    private void clearRobotOccupancy(RpaRobot robot) {
        robot.setCurrentTaskId(null);
        robot.setCurrentProcessId(null);
    }

    private TaskExecutionListItemVO toExecutionListItem(RpaTaskExecution execution) {
        TaskExecutionListItemVO item = new TaskExecutionListItemVO();
        item.setId(execution.getId());
        item.setTaskId(execution.getTaskId());
        item.setExecutionNo(execution.getExecutionNo());
        item.setTaskName(execution.getTaskName());
        item.setProcessName(execution.getProcessName());
        item.setRobotName(execution.getRobotName());
        item.setStatus(execution.getStatus());
        item.setStartTime(execution.getStartTime());
        item.setEndTime(execution.getEndTime());
        item.setDurationMs(execution.getDurationMs());
        item.setErrorMessage(execution.getErrorMessage());
        item.setTriggerType(execution.getTriggerType());
        item.setCreateTime(execution.getCreateTime());
        return item;
    }

    private void recoverRobotStatus(Long robotId) {
        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<RpaRobot> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(RpaRobot::getId, robotId)
                .set(RpaRobot::getStatus, STATUS_IDLE)
                .set(RpaRobot::getCurrentTaskId, null)
                .set(RpaRobot::getCurrentProcessId, null)
                .set(RpaRobot::getLastHeartbeatTime, now)
                .set(RpaRobot::getLastOnlineTime, now)
                .set(RpaRobot::getUpdateTime, now);
        robotMapper.update(null, wrapper);
    }

    private void markRobotError(Long robotId) {
        RpaRobot robot = getRobotById(robotId);
        robot.setStatus(STATUS_ERROR);
        robot.setUpdateTime(LocalDateTime.now());
        robotMapper.updateById(robot);
    }

    private void markRobotOffline(RpaRobot robot) {
        LambdaUpdateWrapper<RpaRobot> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(RpaRobot::getId, robot.getId())
                .set(RpaRobot::getStatus, STATUS_OFFLINE)
                .set(RpaRobot::getCurrentTaskId, null)
                .set(RpaRobot::getCurrentProcessId, null)
                .set(RpaRobot::getUpdateTime, LocalDateTime.now());
        robotMapper.update(null, wrapper);
    }

    private void reconcileRobotOccupancy() {
        LambdaQueryWrapper<RpaRobot> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(q -> q.eq(RpaRobot::getStatus, STATUS_BUSY)
                .or()
                .isNotNull(RpaRobot::getCurrentTaskId)
                .or()
                .isNotNull(RpaRobot::getCurrentProcessId));

        List<RpaRobot> robots = robotMapper.selectList(wrapper);
        for (RpaRobot robot : robots) {
            releaseRobotIfTaskClosed(robot);
        }
    }

    private void releaseRobotIfTaskClosed(RpaRobot robot) {
        if (robot.getCurrentTaskId() == null) {
            if (safeInt(robot.getStatus()) == STATUS_BUSY) {
                recoverRobotStatus(robot.getId());
            }
            return;
        }

        RpaTask task = rpaTaskMapper.selectById(robot.getCurrentTaskId());
        if (task == null || !isTaskActive(task.getExecutionStatus())) {
            recoverRobotStatus(robot.getId());
        }
    }

    private boolean isTaskActive(String executionStatus) {
        return TASK_STATUS_QUEUED.equalsIgnoreCase(executionStatus)
                || TASK_STATUS_RUNNING.equalsIgnoreCase(executionStatus);
    }

    private boolean shouldRefreshOnlineTime(RpaRobot robot) {
        int status = safeInt(robot.getStatus());
        return status == STATUS_OFFLINE || status == STATUS_ERROR || robot.getLastOnlineTime() == null;
    }

    private boolean shouldRecoverToOnline(RpaRobot robot) {
        int status = safeInt(robot.getStatus());
        return status == STATUS_OFFLINE || status == STATUS_ERROR;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
