package com.example.rpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.entity.RpaProcess;
import com.example.rpa.exception.BusinessException;
import com.example.rpa.mapper.RpaProcessMapper;
import com.example.rpa.service.RpaProcessService;
import com.example.rpa.util.SecurityUtil;
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
    public RpaProcess getProcessById(Long id) {
        RpaProcess process = rpaProcessMapper.selectById(id);
        if (process == null) {
            throw new BusinessException("流程不存在");
        }
        return process;
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
        
        rpaProcessMapper.updateById(process);
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
}
