package com.example.rpa.config;

import com.example.rpa.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(100)
@RequiredArgsConstructor
public class TaskExecutionRecoveryInitializer implements CommandLineRunner {

    private final TaskService taskService;

    @Override
    public void run(String... args) {
        try {
            taskService.reconcileExecutionStateOnStartup();
        } catch (Exception e) {
            log.warn("启动时纠正任务执行状态失败: {}", e.getMessage());
        }
    }
}
