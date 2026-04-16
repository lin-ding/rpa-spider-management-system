package com.example.rpa.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.dto.ExecutionQueryRequest;
import com.example.rpa.vo.TaskExecutionListItemVO;
import com.example.rpa.vo.TaskStageLogVO;

import java.util.List;

public interface ExecutionService {

    Page<TaskExecutionListItemVO> getExecutionPage(ExecutionQueryRequest request);

    List<TaskStageLogVO> getStageLogs(Long executionId);
}
