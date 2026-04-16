package com.example.rpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.dto.ExecutionQueryRequest;
import com.example.rpa.entity.RpaTaskExecution;
import com.example.rpa.entity.RpaTaskStageLog;
import com.example.rpa.exception.BusinessException;
import com.example.rpa.mapper.RpaTaskExecutionMapper;
import com.example.rpa.mapper.RpaTaskStageLogMapper;
import com.example.rpa.service.ExecutionService;
import com.example.rpa.vo.TaskExecutionListItemVO;
import com.example.rpa.vo.TaskStageLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExecutionServiceImpl implements ExecutionService {

    private final RpaTaskExecutionMapper rpaTaskExecutionMapper;
    private final RpaTaskStageLogMapper rpaTaskStageLogMapper;

    @Override
    public Page<TaskExecutionListItemVO> getExecutionPage(ExecutionQueryRequest request) {
        Page<RpaTaskExecution> page = new Page<>(request.getCurrent(), request.getSize());
        LambdaQueryWrapper<RpaTaskExecution> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(request.getTaskName())) {
            wrapper.like(RpaTaskExecution::getTaskName, request.getTaskName().trim());
        }
        if (StringUtils.hasText(request.getRobotName())) {
            wrapper.like(RpaTaskExecution::getRobotName, request.getRobotName().trim());
        }
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(RpaTaskExecution::getStatus, request.getStatus().trim());
        }
        wrapper.ge(request.getStartTimeFrom() != null, RpaTaskExecution::getStartTime, request.getStartTimeFrom());
        wrapper.le(request.getStartTimeTo() != null, RpaTaskExecution::getStartTime, request.getStartTimeTo());
        wrapper.orderByDesc(RpaTaskExecution::getCreateTime);

        Page<RpaTaskExecution> executionPage = rpaTaskExecutionMapper.selectPage(page, wrapper);
        Page<TaskExecutionListItemVO> resultPage =
                new Page<>(executionPage.getCurrent(), executionPage.getSize(), executionPage.getTotal());
        resultPage.setRecords(executionPage.getRecords().stream().map(this::toExecutionListItem).toList());
        return resultPage;
    }

    @Override
    public List<TaskStageLogVO> getStageLogs(Long executionId) {
        RpaTaskExecution execution = rpaTaskExecutionMapper.selectById(executionId);
        if (execution == null) {
            throw new BusinessException("执行记录不存在");
        }

        LambdaQueryWrapper<RpaTaskStageLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RpaTaskStageLog::getExecutionId, executionId)
                .orderByAsc(RpaTaskStageLog::getStageOrder);
        return rpaTaskStageLogMapper.selectList(wrapper).stream().map(this::toStageLogVO).toList();
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
}
