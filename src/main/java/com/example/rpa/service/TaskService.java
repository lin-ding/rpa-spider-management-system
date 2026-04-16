package com.example.rpa.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.dto.AddTaskRequest;
import com.example.rpa.dto.TaskExecutionQueryRequest;
import com.example.rpa.dto.TaskQueryRequest;
import com.example.rpa.dto.UpdateTaskRequest;
import com.example.rpa.vo.TaskDetailVO;
import com.example.rpa.vo.TaskExecutionListItemVO;
import com.example.rpa.vo.TaskListItemVO;
import com.example.rpa.vo.TaskStageLogVO;
import com.example.rpa.vo.TaskStatisticsVO;

public interface TaskService {

    void addTask(AddTaskRequest request);

    Page<TaskListItemVO> getTaskPage(TaskQueryRequest request);

    TaskDetailVO getTaskDetail(Long id);

    void updateTask(Long id, UpdateTaskRequest request);

    void executeTask(Long id);

    void cancelTaskExecution(Long id);

    void retryTaskExecution(Long id);

    void reconcileExecutionStateOnStartup();

    Page<TaskExecutionListItemVO> getTaskExecutionPage(Long taskId, TaskExecutionQueryRequest request);

    java.util.List<TaskStageLogVO> getTaskExecutionStages(Long executionId);

    TaskStatisticsVO getTaskStatistics();

    void deleteTask(Long id);
}
