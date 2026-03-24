package com.example.rpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.entity.DataCollectionResult;
import com.example.rpa.exception.BusinessException;
import com.example.rpa.mapper.DataCollectionResultMapper;
import com.example.rpa.service.DataCollectionResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DataCollectionResultServiceImpl implements DataCollectionResultService {

    @Autowired
    private DataCollectionResultMapper dataCollectionResultMapper;

    @Override
    public Page<DataCollectionResult> getDataPage(Integer current, Integer size, DataCollectionResult data) {
        Page<DataCollectionResult> page = new Page<>(current, size);
        LambdaQueryWrapper<DataCollectionResult> wrapper = new LambdaQueryWrapper<>();
        
        if (data.getTaskId() != null) {
            wrapper.eq(DataCollectionResult::getTaskId, data.getTaskId());
        }
        if (StringUtils.hasText(data.getTaskName())) {
            wrapper.like(DataCollectionResult::getTaskName, data.getTaskName());
        }
        if (StringUtils.hasText(data.getDataSource())) {
            wrapper.like(DataCollectionResult::getDataSource, data.getDataSource());
        }
        
        wrapper.orderByDesc(DataCollectionResult::getCollectionTime);
        return dataCollectionResultMapper.selectPage(page, wrapper);
    }

    @Override
    public DataCollectionResult getDataById(Long id) {
        DataCollectionResult result = dataCollectionResultMapper.selectById(id);
        if (result == null) {
            throw new BusinessException("数据不存在");
        }
        return result;
    }
}
