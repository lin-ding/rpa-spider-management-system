package com.example.rpa.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.entity.DataCollectionResult;

public interface DataCollectionResultService {
    
    Page<DataCollectionResult> getDataPage(Integer current, Integer size, DataCollectionResult data);
    
    DataCollectionResult getDataById(Long id);
}
