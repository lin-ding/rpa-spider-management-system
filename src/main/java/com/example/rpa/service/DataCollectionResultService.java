package com.example.rpa.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.entity.DataCollectionResult;

import java.util.List;

public interface DataCollectionResultService {
    
    Page<DataCollectionResult> getDataPage(Integer current, Integer size, DataCollectionResult data);
    
    DataCollectionResult getDataById(Long id);

    void updateDataStatus(List<Long> ids, String dataStatus, String errorMessage);

    void deleteDataById(Long id);

    void deleteDataBatch(List<Long> ids);

    void parseDataBatch(List<Long> ids, String parseType);

    void processDataBatch(List<Long> ids, String processingType);
}
