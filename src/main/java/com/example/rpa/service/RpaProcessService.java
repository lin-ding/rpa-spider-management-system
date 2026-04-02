package com.example.rpa.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.entity.RpaProcess;
import com.example.rpa.vo.ProcessDesignVO;
import com.example.rpa.vo.ProcessDetailVO;

public interface RpaProcessService {
    
    Page<RpaProcess> getProcessPage(Integer current, Integer size, RpaProcess process);
    
    ProcessDetailVO getProcessById(Long id);
    
    void addProcess(RpaProcess process);
    
    void updateProcess(RpaProcess process);

    void saveProcessDesign(Long id, ProcessDesignVO designVO);
    
    void deleteProcess(Long id);
    
    boolean checkProcessCodeUnique(RpaProcess process);
}
