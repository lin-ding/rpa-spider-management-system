package com.example.rpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.entity.RpaProcess;
import com.example.rpa.exception.BusinessException;
import com.example.rpa.mapper.RpaProcessMapper;
import com.example.rpa.service.RpaProcessService;
import com.example.rpa.util.SecurityUtil;
import com.example.rpa.vo.ProcessDesignVO;
import com.example.rpa.vo.ProcessDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RpaProcessServiceImpl implements RpaProcessService {

    @Autowired
    private RpaProcessMapper rpaProcessMapper;
    
    @Autowired
    private SecurityUtil securityUtil;

    @Override
    public Page<RpaProcess> getProcessPage(Integer current, Integer size, RpaProcess process) {
        Page<RpaProcess> page = new Page<>(current, size);
        LambdaQueryWrapper<RpaProcess> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(process.getProcessName())) {
            wrapper.like(RpaProcess::getProcessName, process.getProcessName());
        }
        if (StringUtils.hasText(process.getProcessCode())) {
            wrapper.like(RpaProcess::getProcessCode, process.getProcessCode());
        }
        if (process.getStatus() != null) {
            wrapper.eq(RpaProcess::getStatus, process.getStatus());
        }
        if (process.getProcessType() != null) {
            wrapper.eq(RpaProcess::getProcessType, process.getProcessType());
        }
        
        wrapper.orderByDesc(RpaProcess::getCreateTime);
        return rpaProcessMapper.selectPage(page, wrapper);
    }

    @Override
    public ProcessDetailVO getProcessById(Long id) {
        RpaProcess process = rpaProcessMapper.selectById(id);
        if (process == null) {
            throw new BusinessException("流程不存在");
        }
        return toDetailVO(process);
    }

    @Override
    public void addProcess(RpaProcess process) {
        if (!checkProcessCodeUnique(process)) {
            throw new BusinessException("流程编码已存在");
        }
        process.setCreateBy(securityUtil.getCurrentUserId());
        rpaProcessMapper.insert(process);
    }

    @Override
    public void updateProcess(RpaProcess process) {
        RpaProcess existing = rpaProcessMapper.selectById(process.getId());
        if (existing == null) {
            throw new BusinessException("流程不存在");
        }
        
        if (!existing.getProcessCode().equals(process.getProcessCode())) {
            if (!checkProcessCodeUnique(process)) {
                throw new BusinessException("流程编码已存在");
            }
        }

        existing.setProcessName(process.getProcessName());
        existing.setProcessCode(process.getProcessCode());
        existing.setDescription(process.getDescription());
        existing.setProcessType(process.getProcessType());
        existing.setScriptContent(process.getScriptContent());
        existing.setStatus(process.getStatus());

        rpaProcessMapper.updateById(existing);
    }

    @Override
    public void saveProcessDesign(Long id, ProcessDesignVO designVO) {
        RpaProcess existing = rpaProcessMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("流程不存在");
        }

        if (!StringUtils.hasText(designVO.getProcessData())) {
            throw new BusinessException("流程设计数据不能为空");
        }

        // 当前阶段先将四步流程 JSON 暂存到 scriptContent 字段中，
        // 等后续流程模型稳定后再拆分为专用结构。
        existing.setScriptContent(designVO.getProcessData());
        rpaProcessMapper.updateById(existing);
    }

    @Override
    public void deleteProcess(Long id) {
        RpaProcess process = rpaProcessMapper.selectById(id);
        if (process == null) {
            throw new BusinessException("流程不存在");
        }
        rpaProcessMapper.deleteById(id);
    }

    @Override
    public boolean checkProcessCodeUnique(RpaProcess process) {
        LambdaQueryWrapper<RpaProcess> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RpaProcess::getProcessCode, process.getProcessCode());
        if (process.getId() != null) {
            wrapper.ne(RpaProcess::getId, process.getId());
        }
        return rpaProcessMapper.selectCount(wrapper) == 0;
    }

    private ProcessDetailVO toDetailVO(RpaProcess process) {
        ProcessDetailVO vo = new ProcessDetailVO();
        vo.setId(process.getId());
        vo.setProcessName(process.getProcessName());
        vo.setProcessCode(process.getProcessCode());
        vo.setDescription(process.getDescription());
        vo.setProcessType(process.getProcessType());
        vo.setScriptContent(process.getScriptContent());
        vo.setProcessData(process.getScriptContent());
        vo.setStatus(process.getStatus());
        vo.setCreateBy(process.getCreateBy());
        vo.setCreateTime(process.getCreateTime());
        vo.setUpdateTime(process.getUpdateTime());
        return vo;
    }
}
