package com.example.rpa.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.entity.RpaProcess;

public interface RpaProcessService {
    
    Page<RpaProcess> getProcessPage(Integer current, Integer size, RpaProcess process);
    
    RpaProcess getProcessById(Long id);
    
    void addProcess(RpaProcess process);
    
    void updateProcess(RpaProcess process);
    
    void deleteProcess(Long id);
    
    boolean checkProcessCodeUnique(RpaProcess process);
}
