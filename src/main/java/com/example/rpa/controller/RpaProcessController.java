package com.example.rpa.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.annotation.RequireAdmin;
import com.example.rpa.common.Result;
import com.example.rpa.entity.RpaProcess;
import com.example.rpa.service.RpaProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/process")
public class RpaProcessController {

    @Autowired
    private RpaProcessService rpaProcessService;

    @GetMapping("/list")
    public Result<Page<RpaProcess>> getProcessList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            RpaProcess process) {
        Page<RpaProcess> page = rpaProcessService.getProcessPage(current, size, process);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<RpaProcess> getProcessById(@PathVariable Long id) {
        RpaProcess process = rpaProcessService.getProcessById(id);
        log.info("获取流程详情 - ID: {}, processData: {}", id, process.getProcessData());
        return Result.success(process);
    }

    @PostMapping
    @RequireAdmin("创建流程")
    public Result<Void> addProcess(@RequestBody RpaProcess process) {
        rpaProcessService.addProcess(process);
        return Result.success();
    }

    @PutMapping("/{id}")
    @RequireAdmin("修改流程")
    public Result<Void> updateProcess(@PathVariable Long id, @RequestBody RpaProcess process) {
        process.setId(id);
        rpaProcessService.updateProcess(process);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireAdmin("删除流程")
    public Result<Void> deleteProcess(@PathVariable Long id) {
        rpaProcessService.deleteProcess(id);
        return Result.success();
    }

    @PutMapping("/{id}/design")
    @RequireAdmin("保存流程设计")
    public Result<Void> saveProcessDesign(@PathVariable Long id, @RequestBody RpaProcess process) {
        log.info("保存流程设计 - ID: {}, processData: {}", id, process.getProcessData());
        process.setId(id);
        rpaProcessService.updateProcess(process);
        return Result.success();
    }

    @PostMapping("/test-script")
    public Result<String> testProcessScript(@RequestBody RpaProcess process) {
        return Result.success("脚本测试成功");
    }

    @GetMapping("/checkProcessCode")
    public Result<Boolean> checkProcessCode(RpaProcess process) {
        boolean unique = rpaProcessService.checkProcessCodeUnique(process);
        return Result.success(unique);
    }
}
