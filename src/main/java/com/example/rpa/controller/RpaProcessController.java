package com.example.rpa.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.annotation.RequireAdmin;
import com.example.rpa.common.Result;
import com.example.rpa.entity.RpaProcess;
import com.example.rpa.service.RpaProcessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/process")
@Tag(name = "流程管理", description = "提供 RPA 流程的分页查询、详情查看、增删改查、设计保存和编码校验接口")
public class RpaProcessController {

    @Autowired
    private RpaProcessService rpaProcessService;

    @GetMapping("/list")
    @Operation(summary = "分页查询流程列表", description = "按流程名称、流程编码、流程类型和状态等条件分页查询流程定义列表")
    public Result<Page<RpaProcess>> getProcessList(
            @Parameter(description = "当前页码", example = "1")
            @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页条数", example = "10")
            @RequestParam(defaultValue = "10") Integer size,
            RpaProcess process) {
        Page<RpaProcess> page = rpaProcessService.getProcessPage(current, size, process);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询流程详情", description = "根据流程主键 ID 查询单个流程的完整定义信息")
    public Result<RpaProcess> getProcessById(@Parameter(description = "流程主键 ID", required = true)
                                             @PathVariable Long id) {
        RpaProcess process = rpaProcessService.getProcessById(id);
        log.info("获取流程详情 - ID: {}, processData: {}", id, process.getProcessData());
        return Result.success(process);
    }

    @PostMapping
    @RequireAdmin("创建流程")
    @Operation(summary = "新增流程", description = "新增一条流程定义，保存流程基础信息、脚本内容和启用状态")
    public Result<Void> addProcess(@RequestBody RpaProcess process) {
        rpaProcessService.addProcess(process);
        return Result.success();
    }

    @PutMapping("/{id}")
    @RequireAdmin("修改流程")
    @Operation(summary = "修改流程", description = "根据流程主键 ID 更新流程基础信息、脚本内容、设计数据和状态")
    public Result<Void> updateProcess(@Parameter(description = "流程主键 ID", required = true)
                                      @PathVariable Long id,
                                      @RequestBody RpaProcess process) {
        process.setId(id);
        rpaProcessService.updateProcess(process);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireAdmin("删除流程")
    @Operation(summary = "删除流程", description = "根据流程主键 ID 删除流程定义")
    public Result<Void> deleteProcess(@Parameter(description = "流程主键 ID", required = true)
                                      @PathVariable Long id) {
        rpaProcessService.deleteProcess(id);
        return Result.success();
    }

    @PutMapping("/{id}/design")
    @RequireAdmin("保存流程设计")
    @Operation(summary = "保存流程设计", description = "保存流程设计页提交的四步流程设计数据")
    public Result<Void> saveProcessDesign(@Parameter(description = "流程主键 ID", required = true)
                                          @PathVariable Long id,
                                          @RequestBody RpaProcess process) {
        log.info("保存流程设计 - ID: {}, processData: {}", id, process.getProcessData());
        process.setId(id);
        rpaProcessService.updateProcess(process);
        return Result.success();
    }



    @PostMapping("/test-script")
    @Operation(summary = "测试流程脚本", description = "对提交的流程脚本进行测试，当前为占位接口，固定返回测试成功")
    public Result<String> testProcessScript(@RequestBody RpaProcess process) {
        return Result.success("脚本测试成功");
    }

    @GetMapping("/checkProcessCode")
    @Operation(summary = "校验流程编码唯一性", description = "校验流程编码是否已被其他流程占用，返回 true 表示可用")
    public Result<Boolean> checkProcessCode(RpaProcess process) {
        boolean unique = rpaProcessService.checkProcessCodeUnique(process);
        return Result.success(unique);
    }
}
