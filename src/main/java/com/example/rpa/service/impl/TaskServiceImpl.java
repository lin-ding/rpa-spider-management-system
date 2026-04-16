package com.example.rpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.dto.AddTaskRequest;
import com.example.rpa.dto.TaskExecutionQueryRequest;
import com.example.rpa.dto.TaskQueryRequest;
import com.example.rpa.dto.UpdateTaskRequest;
import com.example.rpa.entity.RpaProcess;
import com.example.rpa.entity.RpaRobot;
import com.example.rpa.entity.RpaTask;
import com.example.rpa.entity.RpaTaskExecution;
import com.example.rpa.entity.RpaTaskStageLog;
import com.example.rpa.exception.BusinessException;
import com.example.rpa.mapper.RobotMapper;
import com.example.rpa.mapper.RpaProcessMapper;
import com.example.rpa.mapper.RpaTaskMapper;
import com.example.rpa.mapper.RpaTaskExecutionMapper;
import com.example.rpa.mapper.RpaTaskStageLogMapper;
import com.example.rpa.service.ProcessExecutionService;
import com.example.rpa.service.RobotService;
import com.example.rpa.service.TaskService;
import com.example.rpa.util.SecurityUtil;
import com.example.rpa.vo.TaskDetailVO;
import com.example.rpa.vo.TaskExecutionListItemVO;
import com.example.rpa.vo.TaskListItemVO;
import com.example.rpa.vo.TaskStageLogVO;
import com.example.rpa.vo.TaskStatisticsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private static final int PROCESS_STATUS_ENABLED = 1;
    private static final int ROBOT_STATUS_DISABLED = 0;
    private static final int ROBOT_STATUS_IDLE = 1;
    private static final int ROBOT_STATUS_BUSY = 2;
    private static final int ROBOT_STATUS_OFFLINE = 3;
    private static final int ROBOT_STATUS_ERROR = 4;
    private static final int DEFAULT_PRIORITY = 2;
    private static final int DEFAULT_CONFIG_STATUS = 1;
    private static final String EXECUTION_STATUS_PENDING = "pending";
    private static final String EXECUTION_STATUS_QUEUED = "queued";
    private static final String EXECUTION_STATUS_RUNNING = "running";
    private static final String EXECUTION_STATUS_SUCCESS = "success";
    private static final String EXECUTION_STATUS_FAILED = "failed";
    private static final String TRIGGER_TYPE_MANUAL = "manual";

    private final RpaTaskMapper rpaTaskMapper;
    private final RpaTaskExecutionMapper rpaTaskExecutionMapper;
    private final RpaTaskStageLogMapper rpaTaskStageLogMapper;
    private final RpaProcessMapper rpaProcessMapper;
    private final RobotMapper robotMapper;
    private final RobotService robotService;
    private final ProcessExecutionService processExecutionService;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTask(AddTaskRequest request) {
        String taskCode = request.getTaskCode().trim();
        if (!checkTaskCodeUnique(taskCode)) {
            throw new BusinessException("任务编码已存在");
        }

        RpaProcess process = getAvailableProcess(request.getProcessId());
        RpaRobot robot = getAvailableRobot(request.getRobotId());

        RpaTask task = new RpaTask();
        task.setTaskCode(taskCode);
        task.setTaskName(request.getTaskName().trim());
        task.setProcessId(process.getId());
        task.setRobotId(robot.getId());
        task.setTaxpayerId(trimToNull(request.getTaxpayerId()));
        task.setEnterpriseName(trimToNull(request.getEnterpriseName()));
        task.setCategory(trimToNull(request.getCategory()));
        task.setPriority(request.getPriority() == null ? DEFAULT_PRIORITY : request.getPriority());
        task.setConfigStatus(request.getConfigStatus() == null ? DEFAULT_CONFIG_STATUS : request.getConfigStatus());
        task.setExecutionStatus(EXECUTION_STATUS_PENDING);
        task.setRemark(trimToNull(request.getRemark()));
        task.setCreateBy(securityUtil.getCurrentUserId());
        task.setDeleted(0);

        rpaTaskMapper.insert(task);
    }

    @Override
    public Page<TaskListItemVO> getTaskPage(TaskQueryRequest request) {
        reconcileStaleQueuedTasks();
        Page<RpaTask> page = new Page<>(request.getCurrent(), request.getSize());
        LambdaQueryWrapper<RpaTask> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(request.getTaskCode())) {
            wrapper.like(RpaTask::getTaskCode, request.getTaskCode().trim());
        }
        if (StringUtils.hasText(request.getTaskName())) {
            wrapper.like(RpaTask::getTaskName, request.getTaskName().trim());
        }
        if (StringUtils.hasText(request.getExecutionStatus())) {
            wrapper.eq(RpaTask::getExecutionStatus, request.getExecutionStatus().trim());
        }
        wrapper.eq(request.getProcessId() != null, RpaTask::getProcessId, request.getProcessId());
        wrapper.eq(request.getRobotId() != null, RpaTask::getRobotId, request.getRobotId());
        wrapper.ge(request.getStartTimeFrom() != null, RpaTask::getLastStartTime, request.getStartTimeFrom());
        wrapper.le(request.getStartTimeTo() != null, RpaTask::getLastStartTime, request.getStartTimeTo());
        wrapper.orderByDesc(RpaTask::getCreateTime);

        Page<RpaTask> taskPage = rpaTaskMapper.selectPage(page, wrapper);
        List<RpaTask> tasks = taskPage.getRecords();
        Map<Long, String> processNameMap = buildProcessNameMap(tasks);
        Map<Long, String> robotNameMap = buildRobotNameMap(tasks);
        Map<Long, String> latestExecutionStatusMap = buildLatestExecutionStatusMap(tasks);

        List<TaskListItemVO> records = tasks.stream()
                .map(task -> toListItem(task, processNameMap, robotNameMap, latestExecutionStatusMap))
                .toList();

        Page<TaskListItemVO> resultPage = new Page<>(taskPage.getCurrent(), taskPage.getSize(), taskPage.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public TaskDetailVO getTaskDetail(Long id) {
        reconcileStaleQueuedTasks();
        RpaTask task = getTaskById(id);
        RpaProcess process = getProcessById(task.getProcessId());
        RpaRobot robot = getRobotById(task.getRobotId());
        return toDetailVO(task, process, robot, getLatestExecution(task.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTask(Long id, UpdateTaskRequest request) {
        RpaTask existing = getTaskById(id);
        if (isTaskSubmitted(existing.getExecutionStatus())) {
            throw new BusinessException("任务已在排队或执行中，无法编辑");
        }

        RpaProcess process = getAvailableProcess(request.getProcessId());
        RpaRobot robot = getAvailableRobot(request.getRobotId());

        existing.setTaskName(request.getTaskName().trim());
        existing.setProcessId(process.getId());
        existing.setRobotId(robot.getId());
        existing.setTaxpayerId(trimToNull(request.getTaxpayerId()));
        existing.setEnterpriseName(trimToNull(request.getEnterpriseName()));
        existing.setCategory(trimToNull(request.getCategory()));
        existing.setPriority(request.getPriority() == null ? DEFAULT_PRIORITY : request.getPriority());
        existing.setConfigStatus(request.getConfigStatus() == null ? DEFAULT_CONFIG_STATUS : request.getConfigStatus());
        existing.setRemark(trimToNull(request.getRemark()));

        rpaTaskMapper.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeTask(Long id) {
        RpaTask task = getTaskById(id);
        validateTaskCanExecute(task);

        RpaProcess process = getAvailableProcess(task.getProcessId());
        RpaRobot robot = getAvailableRobot(task.getRobotId());
        validateRobotCanExecute(robot);

        RpaTaskExecution execution = buildExecution(task, process, robot);
        rpaTaskExecutionMapper.insert(execution);
        updateTaskExecutionStatus(task, EXECUTION_STATUS_PENDING, null, null);

        try {
            boolean queued = robotService.executeTaskOnRobot(robot.getId(), task.getId(), process.getId(),
                    () -> runTaskExecution(execution.getId(), task, process, robot));
            if (queued) {
                markExecutionQueued(execution.getId(), task.getId());
            }
        } catch (Exception e) {
            markExecutionFailed(execution.getId(), task.getId(), "任务提交执行失败: " + e.getMessage(), null);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTaskExecution(Long id) {
        RpaTask task = getTaskById(id);
        RpaRobot robot = getRobotById(task.getRobotId());
        RpaTaskExecution latestExecution = getLatestExecution(task.getId());

        if (isExecutionRunning(latestExecution)) {
            throw new BusinessException("任务已开始执行，暂不支持取消");
        }

        if (!isTaskCancelable(task, latestExecution)) {
            throw new BusinessException("当前任务没有可取消的排队执行");
        }

        if (!isOpenExecution(latestExecution)) {
            reconcileTaskSnapshot(task, latestExecution);
            releaseRobotIfStale(robot, task.getId());
            return;
        }

        boolean removed = robotService.cancelQueuedTask(task.getRobotId(), task.getId());
        if (!removed && isRobotActuallyExecutingTask(robot, task.getId())) {
            throw new BusinessException("任务已开始执行，暂不支持取消");
        }

        markExecutionCancelled(latestExecution.getId(), task.getId(), "用户取消排队");
        releaseRobotIfStale(robot, task.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryTaskExecution(Long id) {
        RpaTask task = getTaskById(id);
        RpaTaskExecution latestExecution = getLatestExecution(task.getId());
        if (latestExecution == null) {
            throw new BusinessException("当前任务没有可重试的执行记录");
        }
        if (!EXECUTION_STATUS_FAILED.equalsIgnoreCase(latestExecution.getStatus())) {
            throw new BusinessException("仅支持重试最近一次执行失败的任务");
        }
        if (isTaskSubmitted(task.getExecutionStatus())) {
            throw new BusinessException("任务已在排队或执行中，无法重试");
        }

        executeTask(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reconcileExecutionStateOnStartup() {
        LocalDateTime recoveredAt = LocalDateTime.now();

        LambdaQueryWrapper<RpaTaskExecution> executionWrapper = new LambdaQueryWrapper<>();
        executionWrapper.in(RpaTaskExecution::getStatus, EXECUTION_STATUS_QUEUED, EXECUTION_STATUS_RUNNING)
                .orderByAsc(RpaTaskExecution::getCreateTime);
        List<RpaTaskExecution> staleExecutions = rpaTaskExecutionMapper.selectList(executionWrapper);

        for (RpaTaskExecution execution : staleExecutions) {
            recoverStaleExecution(execution, recoveredAt);
        }

        LambdaQueryWrapper<RpaRobot> robotWrapper = new LambdaQueryWrapper<>();
        robotWrapper.and(wrapper -> wrapper.eq(RpaRobot::getStatus, 2)
                .or()
                .isNotNull(RpaRobot::getCurrentTaskId)
                .or()
                .isNotNull(RpaRobot::getCurrentProcessId));
        List<RpaRobot> occupiedRobots = robotMapper.selectList(robotWrapper);
        for (RpaRobot robot : occupiedRobots) {
            recoverRobotOccupancy(robot, recoveredAt);
        }

        if (!staleExecutions.isEmpty() || !occupiedRobots.isEmpty()) {
            log.warn("启动恢复完成，纠正执行记录 {} 条，释放机器人 {} 台", staleExecutions.size(), occupiedRobots.size());
        }
    }

    @Override
    public Page<TaskExecutionListItemVO> getTaskExecutionPage(Long taskId, TaskExecutionQueryRequest request) {
        getTaskById(taskId);

        Page<RpaTaskExecution> page = new Page<>(request.getCurrent(), request.getSize());
        LambdaQueryWrapper<RpaTaskExecution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RpaTaskExecution::getTaskId, taskId)
                .orderByDesc(RpaTaskExecution::getCreateTime);

        Page<RpaTaskExecution> executionPage = rpaTaskExecutionMapper.selectPage(page, wrapper);
        List<TaskExecutionListItemVO> records = executionPage.getRecords().stream()
                .map(this::toExecutionListItem)
                .toList();

        Page<TaskExecutionListItemVO> resultPage = new Page<>(executionPage.getCurrent(), executionPage.getSize(), executionPage.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public List<TaskStageLogVO> getTaskExecutionStages(Long executionId) {
        RpaTaskExecution execution = getExecutionById(executionId);
        LambdaQueryWrapper<RpaTaskStageLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RpaTaskStageLog::getExecutionId, execution.getId())
                .orderByAsc(RpaTaskStageLog::getStageOrder);

        return rpaTaskStageLogMapper.selectList(wrapper).stream()
                .map(this::toStageLogVO)
                .toList();
    }

    @Override
    public TaskStatisticsVO getTaskStatistics() {
        reconcileStaleQueuedTasks();
        TaskStatisticsVO statistics = new TaskStatisticsVO();
        statistics.setTotal(rpaTaskMapper.selectCount(new LambdaQueryWrapper<>()));
        statistics.setPending(countByExecutionStatus(EXECUTION_STATUS_PENDING));
        statistics.setQueued(countByExecutionStatus(EXECUTION_STATUS_QUEUED));
        statistics.setRunning(countByExecutionStatus(EXECUTION_STATUS_RUNNING));
        statistics.setCompleted(countByExecutionStatus(EXECUTION_STATUS_SUCCESS));
        statistics.setFailed(countByExecutionStatus(EXECUTION_STATUS_FAILED));
        return statistics;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long id) {
        RpaTask task = getTaskById(id);
        if (isTaskSubmitted(task.getExecutionStatus())) {
            throw new BusinessException("任务已在排队或执行中，无法删除");
        }
        rpaTaskMapper.deleteById(id);
    }

    private boolean checkTaskCodeUnique(String taskCode) {
        LambdaQueryWrapper<RpaTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RpaTask::getTaskCode, taskCode);
        return rpaTaskMapper.selectCount(wrapper) == 0;
    }

    private RpaProcess getAvailableProcess(Long processId) {
        RpaProcess process = rpaProcessMapper.selectById(processId);
        if (process == null) {
            throw new BusinessException("关联流程不存在");
        }
        if (!isProcessEnabled(process.getStatus())) {
            throw new BusinessException("关联流程未启用，无法绑定");
        }
        return process;
    }

    private boolean isProcessEnabled(Integer status) {
        return status != null && status == PROCESS_STATUS_ENABLED;
    }

    private RpaRobot getAvailableRobot(Long robotId) {
        RpaRobot robot = robotMapper.selectById(robotId);
        if (robot == null) {
            throw new BusinessException("关联机器人不存在");
        }
        if (!isRobotBindable(robot)) {
            throw new BusinessException("关联机器人当前不可绑定，请选择空闲或忙碌状态的机器人");
        }
        return robot;
    }

    private Map<Long, String> buildProcessNameMap(List<RpaTask> tasks) {
        Set<Long> processIds = tasks.stream()
                .map(RpaTask::getProcessId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (processIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return rpaProcessMapper.selectBatchIds(processIds).stream()
                .collect(Collectors.toMap(RpaProcess::getId, RpaProcess::getProcessName));
    }

    private Map<Long, String> buildRobotNameMap(List<RpaTask> tasks) {
        Set<Long> robotIds = tasks.stream()
                .map(RpaTask::getRobotId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (robotIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return robotMapper.selectBatchIds(robotIds).stream()
                .collect(Collectors.toMap(RpaRobot::getId, RpaRobot::getRobotName));
    }

    private long countByExecutionStatus(String executionStatus) {
        LambdaQueryWrapper<RpaTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RpaTask::getExecutionStatus, executionStatus);
        return rpaTaskMapper.selectCount(wrapper);
    }

    private void reconcileStaleQueuedTasks() {
        LambdaQueryWrapper<RpaTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RpaTask::getExecutionStatus, EXECUTION_STATUS_QUEUED);

        for (RpaTask task : rpaTaskMapper.selectList(wrapper)) {
            RpaRobot robot = robotMapper.selectById(task.getRobotId());
            if (robot == null || robot.getCurrentTaskId() != null || Integer.valueOf(ROBOT_STATUS_BUSY).equals(robot.getStatus())) {
                continue;
            }

            RpaTaskExecution latestExecution = getLatestExecution(task.getId());
            if (latestExecution == null || !EXECUTION_STATUS_QUEUED.equalsIgnoreCase(latestExecution.getStatus())) {
                task.setExecutionStatus(EXECUTION_STATUS_PENDING);
                rpaTaskMapper.updateById(task);
                continue;
            }

            latestExecution.setStatus(EXECUTION_STATUS_FAILED);
            latestExecution.setEndTime(LocalDateTime.now());
            latestExecution.setErrorMessage("排队状态已失效，请重新执行任务");
            latestExecution.setLogDetail("排队状态已失效，请重新执行任务");
            rpaTaskExecutionMapper.updateById(latestExecution);
            updateTaskExecutionStatus(task, EXECUTION_STATUS_PENDING, latestExecution.getStartTime(), latestExecution.getEndTime());
        }
    }

    private Map<Long, String> buildLatestExecutionStatusMap(List<RpaTask> tasks) {
        Set<Long> taskIds = tasks.stream()
                .map(RpaTask::getId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (taskIds.isEmpty()) {
            return Collections.emptyMap();
        }

        LambdaQueryWrapper<RpaTaskExecution> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(RpaTaskExecution::getTaskId, taskIds)
                .orderByDesc(RpaTaskExecution::getCreateTime);

        Map<Long, String> latestStatusMap = new java.util.HashMap<>();
        for (RpaTaskExecution execution : rpaTaskExecutionMapper.selectList(wrapper)) {
            latestStatusMap.putIfAbsent(execution.getTaskId(), execution.getStatus());
        }
        return latestStatusMap;
    }

    @SuppressWarnings("unchecked")
    private void persistStageLogs(Long executionId, Long taskId, Object stageResultsObj) {
        if (!(stageResultsObj instanceof List<?> rawList) || rawList.isEmpty()) {
            return;
        }

        for (Object rawItem : rawList) {
            if (!(rawItem instanceof Map<?, ?> rawMap)) {
                continue;
            }
            Map<String, Object> stageMap = (Map<String, Object>) rawMap;
            RpaTaskStageLog stageLog = new RpaTaskStageLog();
            stageLog.setExecutionId(executionId);
            stageLog.setTaskId(taskId);
            stageLog.setStageOrder(asInteger(stageMap.get("stageOrder")));
            stageLog.setStageName(asString(stageMap.get("stageName")));
            stageLog.setStatus(asString(stageMap.get("status")));
            stageLog.setStartTime(asLocalDateTime(stageMap.get("startTime")));
            stageLog.setEndTime(asLocalDateTime(stageMap.get("endTime")));
            stageLog.setDurationMs(asLong(stageMap.get("durationMs")));
            stageLog.setErrorMessage(trimToNull(asString(stageMap.get("errorMessage"))));
            stageLog.setLogDetail(trimToNull(asString(stageMap.get("logDetail"))));
            stageLog.setStageResult(trimToNull(asString(stageMap.get("stageResult"))));
            rpaTaskStageLogMapper.insert(stageLog);
        }
    }

    private void validateTaskCanExecute(RpaTask task) {
        if (task.getConfigStatus() != null && task.getConfigStatus() == 0) {
            throw new BusinessException("任务已禁用，无法执行");
        }
        if (isTaskSubmitted(task.getExecutionStatus())) {
            throw new BusinessException("任务已在排队或执行中，无法重复启动");
        }
    }

    private void validateRobotCanExecute(RpaRobot robot) {
        if (!isRobotBindable(robot)) {
            throw new BusinessException("关联机器人当前不可执行，请选择空闲或忙碌状态的机器人");
        }
    }

    private boolean isRobotBindable(RpaRobot robot) {
        Integer status = robot.getStatus();
        if (status == null) {
            return false;
        }
        return status == ROBOT_STATUS_IDLE || status == ROBOT_STATUS_BUSY;
    }

    private RpaTaskExecution buildExecution(RpaTask task, RpaProcess process, RpaRobot robot) {
        RpaTaskExecution execution = new RpaTaskExecution();
        execution.setExecutionNo(generateExecutionNo());
        execution.setTaskId(task.getId());
        execution.setTaskCode(task.getTaskCode());
        execution.setTaskName(task.getTaskName());
        execution.setProcessId(process.getId());
        execution.setProcessName(process.getProcessName());
        execution.setRobotId(robot.getId());
        execution.setRobotName(robot.getRobotName());
        execution.setTaxpayerId(task.getTaxpayerId());
        execution.setEnterpriseName(task.getEnterpriseName());
        execution.setCategory(task.getCategory());
        execution.setPriority(task.getPriority());
        execution.setTriggerType(TRIGGER_TYPE_MANUAL);
        execution.setStatus(EXECUTION_STATUS_PENDING);
        execution.setCreateBy(securityUtil.getCurrentUserId());
        execution.setDeleted(0);
        return execution;
    }

    private String generateExecutionNo() {
        return "EXEC-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private void runTaskExecution(Long executionId, RpaTask task, RpaProcess process, RpaRobot robot) {
        LocalDateTime startTime = LocalDateTime.now();
        markExecutionRunning(executionId, task.getId(), startTime);

        try {
            Map<String, Object> result = processExecutionService.executeProcess(
                    process.getId(),
                    buildTaskExecutionContext(executionId, task, process, robot, startTime)
            );
            persistStageLogs(executionId, task.getId(), result.get("stageResults"));
            boolean success = Boolean.TRUE.equals(result.get("success"));
            String message = stringifyResultMessage(result);
            if (success) {
                markExecutionSuccess(executionId, task.getId(), startTime, message);
            } else {
                markExecutionFailed(executionId, task.getId(), message, startTime);
            }
        } catch (Exception e) {
            markExecutionFailed(executionId, task.getId(), e.getMessage(), startTime);
            throw e;
        }
    }

    private Map<String, Object> buildTaskExecutionContext(Long executionId,
                                                          RpaTask task,
                                                          RpaProcess process,
                                                          RpaRobot robot,
                                                          LocalDateTime startTime) {
        Map<String, Object> context = new java.util.LinkedHashMap<>();
        context.put("executionId", executionId);
        context.put("taskId", task.getId());
        context.put("taskCode", task.getTaskCode());
        context.put("taskName", task.getTaskName());
        context.put("taxpayerId", task.getTaxpayerId());
        context.put("enterpriseName", task.getEnterpriseName());
        context.put("category", task.getCategory());
        context.put("priority", task.getPriority());
        context.put("remark", task.getRemark());
        context.put("configStatus", task.getConfigStatus());
        context.put("taskStartTime", startTime);
        context.put("robotId", robot.getId());
        context.put("robotCode", robot.getRobotCode());
        context.put("robotName", robot.getRobotName());
        context.put("robotType", robot.getRobotType());
        context.put("processId", process.getId());
        context.put("processCode", process.getProcessCode());
        context.put("processName", process.getProcessName());
        return context;
    }

    private String stringifyResultMessage(Map<String, Object> result) {
        Object message = result.get("message");
        return message == null ? null : String.valueOf(message);
    }

    private void markExecutionRunning(Long executionId, Long taskId, LocalDateTime startTime) {
        RpaTaskExecution execution = rpaTaskExecutionMapper.selectById(executionId);
        execution.setStatus(EXECUTION_STATUS_RUNNING);
        execution.setStartTime(startTime);
        rpaTaskExecutionMapper.updateById(execution);
        updateTaskExecutionStatus(getTaskById(taskId), EXECUTION_STATUS_RUNNING, startTime, null);
    }

    private void markExecutionQueued(Long executionId, Long taskId) {
        RpaTaskExecution execution = rpaTaskExecutionMapper.selectById(executionId);
        execution.setStatus(EXECUTION_STATUS_QUEUED);
        rpaTaskExecutionMapper.updateById(execution);
        updateTaskExecutionStatus(getTaskById(taskId), EXECUTION_STATUS_QUEUED, null, null);
    }

    private void markExecutionCancelled(Long executionId, Long taskId, String message) {
        LocalDateTime endTime = LocalDateTime.now();
        RpaTaskExecution execution = rpaTaskExecutionMapper.selectById(executionId);
        execution.setStatus(EXECUTION_STATUS_FAILED);
        execution.setEndTime(endTime);
        execution.setErrorMessage(message);
        execution.setLogDetail(message);
        rpaTaskExecutionMapper.updateById(execution);
        updateTaskExecutionStatus(getTaskById(taskId), EXECUTION_STATUS_PENDING, null, null);
    }

    private void markExecutionSuccess(Long executionId, Long taskId, LocalDateTime startTime, String logDetail) {
        LocalDateTime endTime = LocalDateTime.now();
        RpaTaskExecution execution = rpaTaskExecutionMapper.selectById(executionId);
        execution.setStatus(EXECUTION_STATUS_SUCCESS);
        execution.setStartTime(startTime);
        execution.setEndTime(endTime);
        execution.setDurationMs(Duration.between(startTime, endTime).toMillis());
        execution.setLogDetail(trimToNull(logDetail));
        execution.setErrorMessage(null);
        rpaTaskExecutionMapper.updateById(execution);
        updateTaskExecutionStatus(getTaskById(taskId), EXECUTION_STATUS_SUCCESS, startTime, endTime);
    }

    private void markExecutionFailed(Long executionId, Long taskId, String errorMessage, LocalDateTime startTime) {
        LocalDateTime endTime = LocalDateTime.now();
        RpaTaskExecution execution = rpaTaskExecutionMapper.selectById(executionId);
        execution.setStatus(EXECUTION_STATUS_FAILED);
        if (startTime != null) {
            execution.setStartTime(startTime);
            execution.setDurationMs(Duration.between(startTime, endTime).toMillis());
        }
        execution.setEndTime(endTime);
        execution.setErrorMessage(trimToNull(errorMessage));
        execution.setLogDetail(trimToNull(errorMessage));
        rpaTaskExecutionMapper.updateById(execution);
        updateTaskExecutionStatus(getTaskById(taskId), EXECUTION_STATUS_FAILED, startTime, endTime);
    }

    private void updateTaskExecutionStatus(RpaTask task, String executionStatus, LocalDateTime startTime, LocalDateTime endTime) {
        task.setExecutionStatus(executionStatus);
        if (startTime != null) {
            task.setLastStartTime(startTime);
        }
        if (endTime != null) {
            task.setLastEndTime(endTime);
        }
        rpaTaskMapper.updateById(task);
    }

    private void recoverStaleExecution(RpaTaskExecution execution, LocalDateTime recoveredAt) {
        String originalStatus = execution.getStatus();
        execution.setStatus(EXECUTION_STATUS_FAILED);
        if (execution.getStartTime() != null) {
            execution.setDurationMs(Duration.between(execution.getStartTime(), recoveredAt).toMillis());
        }
        execution.setEndTime(recoveredAt);
        String message = EXECUTION_STATUS_RUNNING.equalsIgnoreCase(originalStatus)
                ? "服务重启导致执行中断，请重新执行任务"
                : "服务重启导致排队任务丢失，请重新执行任务";
        execution.setErrorMessage(message);
        execution.setLogDetail(message);
        rpaTaskExecutionMapper.updateById(execution);

        RpaTask task = rpaTaskMapper.selectById(execution.getTaskId());
        if (task == null) {
            return;
        }
        updateTaskExecutionStatus(task, EXECUTION_STATUS_FAILED, execution.getStartTime(), recoveredAt);
    }

    private void recoverRobotOccupancy(RpaRobot robot, LocalDateTime recoveredAt) {
        LambdaUpdateWrapper<RpaRobot> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(RpaRobot::getId, robot.getId())
                .set(RpaRobot::getCurrentTaskId, null)
                .set(RpaRobot::getCurrentProcessId, null)
                .set(RpaRobot::getUpdateTime, recoveredAt);
        if (Integer.valueOf(ROBOT_STATUS_BUSY).equals(robot.getStatus())) {
            wrapper.set(RpaRobot::getStatus, ROBOT_STATUS_IDLE)
                    .set(RpaRobot::getLastHeartbeatTime, recoveredAt)
                    .set(RpaRobot::getLastOnlineTime, recoveredAt);
        }
        robotMapper.update(null, wrapper);
    }

    private RpaTask getTaskById(Long id) {
        RpaTask task = rpaTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        return task;
    }

    private RpaProcess getProcessById(Long id) {
        RpaProcess process = rpaProcessMapper.selectById(id);
        if (process == null) {
            throw new BusinessException("关联流程不存在");
        }
        return process;
    }

    private RpaRobot getRobotById(Long id) {
        RpaRobot robot = robotMapper.selectById(id);
        if (robot == null) {
            throw new BusinessException("关联机器人不存在");
        }
        return robot;
    }

    private RpaTaskExecution getExecutionById(Long id) {
        RpaTaskExecution execution = rpaTaskExecutionMapper.selectById(id);
        if (execution == null) {
            throw new BusinessException("执行记录不存在");
        }
        return execution;
    }

    private RpaTaskExecution getLatestCancelableExecution(Long taskId) {
        LambdaQueryWrapper<RpaTaskExecution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RpaTaskExecution::getTaskId, taskId)
                .in(RpaTaskExecution::getStatus, EXECUTION_STATUS_PENDING, EXECUTION_STATUS_QUEUED)
                .orderByDesc(RpaTaskExecution::getCreateTime)
                .last("limit 1");
        RpaTaskExecution execution = rpaTaskExecutionMapper.selectOne(wrapper);
        if (execution == null) {
            throw new BusinessException("未找到可取消的执行记录");
        }
        return execution;
    }

    private RpaTaskExecution getLatestExecution(Long taskId) {
        LambdaQueryWrapper<RpaTaskExecution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RpaTaskExecution::getTaskId, taskId)
                .orderByDesc(RpaTaskExecution::getCreateTime)
                .last("limit 1");
        return rpaTaskExecutionMapper.selectOne(wrapper);
    }

    private boolean isTaskCancelable(RpaTask task, RpaTaskExecution latestExecution) {
        if (EXECUTION_STATUS_RUNNING.equalsIgnoreCase(task.getExecutionStatus())) {
            return false;
        }
        if (EXECUTION_STATUS_PENDING.equalsIgnoreCase(task.getExecutionStatus())
                || EXECUTION_STATUS_QUEUED.equalsIgnoreCase(task.getExecutionStatus())) {
            return true;
        }
        return latestExecution != null && isOpenExecution(latestExecution);
    }

    private boolean isOpenExecution(RpaTaskExecution execution) {
        if (execution == null) {
            return false;
        }
        return EXECUTION_STATUS_PENDING.equalsIgnoreCase(execution.getStatus())
                || EXECUTION_STATUS_QUEUED.equalsIgnoreCase(execution.getStatus());
    }

    private boolean isExecutionRunning(RpaTaskExecution execution) {
        if (execution == null) {
            return false;
        }
        return EXECUTION_STATUS_RUNNING.equalsIgnoreCase(execution.getStatus())
                && execution.getEndTime() == null;
    }

    private boolean isRobotActuallyExecutingTask(RpaRobot robot, Long taskId) {
        return robot.getCurrentTaskId() != null
                && robot.getCurrentTaskId().equals(taskId)
                && Integer.valueOf(ROBOT_STATUS_BUSY).equals(robot.getStatus());
    }

    private void releaseRobotIfStale(RpaRobot robot, Long taskId) {
        if (robot.getCurrentTaskId() == null || !robot.getCurrentTaskId().equals(taskId)) {
            return;
        }
        if (Integer.valueOf(ROBOT_STATUS_BUSY).equals(robot.getStatus())) {
            return;
        }
        LambdaUpdateWrapper<RpaRobot> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(RpaRobot::getId, robot.getId())
                .set(RpaRobot::getCurrentTaskId, null)
                .set(RpaRobot::getCurrentProcessId, null)
                .set(RpaRobot::getUpdateTime, LocalDateTime.now());
        robotMapper.update(null, wrapper);
    }

    private void reconcileTaskSnapshot(RpaTask task, RpaTaskExecution latestExecution) {
        if (latestExecution == null) {
            updateTaskExecutionStatus(task, EXECUTION_STATUS_PENDING, null, null);
            task.setLastEndTime(null);
            rpaTaskMapper.updateById(task);
            return;
        }
        updateTaskExecutionStatus(task, latestExecution.getStatus(), latestExecution.getStartTime(), latestExecution.getEndTime());
    }

    private TaskDetailVO toDetailVO(RpaTask task, RpaProcess process, RpaRobot robot, RpaTaskExecution latestExecution) {
        TaskDetailVO detail = new TaskDetailVO();
        detail.setId(task.getId());
        detail.setTaskCode(task.getTaskCode());
        detail.setTaskName(task.getTaskName());
        detail.setProcessId(task.getProcessId());
        detail.setProcessName(process.getProcessName());
        detail.setRobotId(task.getRobotId());
        detail.setRobotName(robot.getRobotName());
        detail.setTaxpayerId(task.getTaxpayerId());
        detail.setEnterpriseName(task.getEnterpriseName());
        detail.setCategory(task.getCategory());
        detail.setPriority(task.getPriority());
        detail.setConfigStatus(task.getConfigStatus());
        detail.setExecutionStatus(task.getExecutionStatus());
        detail.setCurrentStatus(resolveCurrentStatus(task.getExecutionStatus()));
        detail.setLatestExecutionStatus(latestExecution == null ? null : latestExecution.getStatus());
        detail.setLastStartTime(task.getLastStartTime());
        detail.setLastEndTime(task.getLastEndTime());
        detail.setRemark(task.getRemark());
        detail.setCreateBy(task.getCreateBy());
        detail.setCreateTime(task.getCreateTime());
        detail.setUpdateTime(task.getUpdateTime());
        return detail;
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

    private TaskStageLogVO toStageLogVO(RpaTaskStageLog stageLog) {
        TaskStageLogVO vo = new TaskStageLogVO();
        vo.setId(stageLog.getId());
        vo.setExecutionId(stageLog.getExecutionId());
        vo.setStageOrder(stageLog.getStageOrder());
        vo.setStageName(stageLog.getStageName());
        vo.setStatus(stageLog.getStatus());
        vo.setStartTime(stageLog.getStartTime());
        vo.setEndTime(stageLog.getEndTime());
        vo.setDurationMs(stageLog.getDurationMs());
        vo.setErrorMessage(stageLog.getErrorMessage());
        vo.setLogDetail(stageLog.getLogDetail());
        vo.setStageResult(stageLog.getStageResult());
        vo.setCreateTime(stageLog.getCreateTime());
        return vo;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Integer asInteger(Object value) {
        if (value instanceof Integer integer) {
            return integer;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String string && StringUtils.hasText(string)) {
            return Integer.parseInt(string);
        }
        return null;
    }

    private Long asLong(Object value) {
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String string && StringUtils.hasText(string)) {
            return Long.parseLong(string);
        }
        return null;
    }

    private LocalDateTime asLocalDateTime(Object value) {
        if (value instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        return null;
    }

    private boolean isTaskSubmitted(String executionStatus) {
        return EXECUTION_STATUS_RUNNING.equalsIgnoreCase(executionStatus)
                || EXECUTION_STATUS_QUEUED.equalsIgnoreCase(executionStatus);
    }

    private TaskListItemVO toListItem(RpaTask task,
                                      Map<Long, String> processNameMap,
                                      Map<Long, String> robotNameMap,
                                      Map<Long, String> latestExecutionStatusMap) {
        TaskListItemVO item = new TaskListItemVO();
        item.setId(task.getId());
        item.setTaskCode(task.getTaskCode());
        item.setTaskName(task.getTaskName());
        item.setProcessId(task.getProcessId());
        item.setProcessName(processNameMap.get(task.getProcessId()));
        item.setRobotId(task.getRobotId());
        item.setRobotName(robotNameMap.get(task.getRobotId()));
        item.setExecutionStatus(task.getExecutionStatus());
        item.setCurrentStatus(resolveCurrentStatus(task.getExecutionStatus()));
        item.setLatestExecutionStatus(latestExecutionStatusMap.get(task.getId()));
        item.setLastStartTime(task.getLastStartTime());
        item.setLastEndTime(task.getLastEndTime());
        item.setCreateTime(task.getCreateTime());
        return item;
    }

    private String resolveCurrentStatus(String executionStatus) {
        if (EXECUTION_STATUS_QUEUED.equalsIgnoreCase(executionStatus)
                || EXECUTION_STATUS_RUNNING.equalsIgnoreCase(executionStatus)) {
            return executionStatus;
        }
        return EXECUTION_STATUS_PENDING;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
