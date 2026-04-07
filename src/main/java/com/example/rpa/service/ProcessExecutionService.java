package com.example.rpa.service;

import java.util.Map;

public interface ProcessExecutionService {
    
    Map<String, Object> executeProcess(Long processId);
    
    Map<String, Object> executeStage(String script, Map<String, Object> context);
    
    Map<String, Object> testScript(String script);
}
