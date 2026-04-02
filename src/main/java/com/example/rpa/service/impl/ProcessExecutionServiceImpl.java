package com.example.rpa.service.impl;

import com.example.rpa.entity.RpaProcess;
import com.example.rpa.exception.BusinessException;
import com.example.rpa.mapper.RpaProcessMapper;
import com.example.rpa.service.ProcessExecutionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ProcessExecutionServiceImpl implements ProcessExecutionService {

    @Autowired
    private RpaProcessMapper rpaProcessMapper;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Map<String, Object> executeProcess(Long processId) {
        RpaProcess process = rpaProcessMapper.selectById(processId);
        if (process == null) {
            throw new BusinessException("流程不存在");
        }
        
        Map<String, Object> context = new HashMap<>();
        context.put("processId", processId);
        context.put("processName", process.getProcessName());
        context.put("processCode", process.getProcessCode());
        
        Map<String, Object> result = new HashMap<>();
        result.put("processId", processId);
        result.put("processName", process.getProcessName());
        
        try {
            if (process.getProcessData() != null && !process.getProcessData().isEmpty()) {
                Map<String, Object> processData = objectMapper.readValue(process.getProcessData(), Map.class);
                List<Map<String, Object>> stages = (List<Map<String, Object>>) processData.get("stages");
                
                if (stages != null && !stages.isEmpty()) {
                    int stageIndex = 0;
                    for (Map<String, Object> stage : stages) {
                        String stageName = (String) stage.get("name");
                        String script = (String) stage.get("script");
                        
                        log.info("执行阶段 [{}]: {}", stageIndex, stageName);
                        
                        if (script != null && !script.trim().isEmpty()) {
                            Map<String, Object> stageResult = executeStage(script, context);
                            context.putAll(stageResult);
                            log.info("阶段 [{}] 执行完成，结果: {}", stageName, stageResult);
                        } else {
                            log.info("阶段 [{}] 无脚本，跳过", stageName);
                        }
                        stageIndex++;
                    }
                    result.put("success", true);
                    result.put("message", "流程执行成功");
                    result.put("executedStages", stages.size());
                    result.put("context", context);
                } else {
                    result.put("success", false);
                    result.put("message", "流程没有配置阶段");
                }
            } else {
                result.put("success", false);
                result.put("message", "流程没有配置数据");
            }
        } catch (Exception e) {
            log.error("流程执行失败", e);
            result.put("success", false);
            result.put("message", "流程执行失败: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> executeStage(String script, Map<String, Object> context) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Binding binding = new Binding();
            if (context != null) {
                context.forEach(binding::setVariable);
                binding.setVariable("context", context);
            } else {
                binding.setVariable("context", new HashMap<>());
            }
            
            binding.setVariable("log", log);
            binding.setVariable("result", result);
            
            GroovyShell shell = new GroovyShell(binding);
            Object executionResult = shell.evaluate(script);
            
            if (executionResult != null) {
                if (executionResult instanceof Map) {
                    result.putAll((Map<String, Object>) executionResult);
                } else {
                    result.put("returnValue", executionResult);
                }
            }
            
            result.put("stageSuccess", true);
            
        } catch (Exception e) {
            log.error("脚本执行失败", e);
            result.put("stageSuccess", false);
            result.put("errorMessage", e.getMessage());
            throw new BusinessException("脚本执行失败: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> testScript(String script) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> context = new HashMap<>();
        context.put("test", true);
        
        try {
            Map<String, Object> executionResult = executeStage(script, context);
            result.put("success", true);
            result.put("message", "脚本测试成功");
            result.put("result", executionResult);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "脚本测试失败: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        
        return result;
    }
}
