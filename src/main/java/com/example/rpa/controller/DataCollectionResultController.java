package com.example.rpa.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.common.Result;
import com.example.rpa.entity.DataCollectionResult;
import com.example.rpa.service.DataCollectionResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/data")
@Tag(name = "数据采集结果", description = "提供数据采集结果的分页查询、详情查看和导出入口")
public class DataCollectionResultController {

    @Autowired
    private DataCollectionResultService dataCollectionResultService;

    @GetMapping("/query")
    @Operation(summary = "分页查询采集结果", description = "按任务、来源等条件分页查询已经入库的数据采集结果")
    public Result<Page<DataCollectionResult>> getDataList(
            @Parameter(description = "当前页码", example = "1")
            @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页条数", example = "10")
            @RequestParam(defaultValue = "10") Integer size,
            DataCollectionResult data) {
        Page<DataCollectionResult> page = dataCollectionResultService.getDataPage(current, size, data);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询采集结果详情", description = "根据采集结果主键查询单条数据详情")
    public Result<DataCollectionResult> getDataById(@Parameter(description = "采集结果主键 ID", required = true)
                                                    @PathVariable Long id) {
        DataCollectionResult result = dataCollectionResultService.getDataById(id);
        return Result.success(result);
    }

    @GetMapping("/export")
    @Operation(summary = "导出采集结果", description = "导出符合条件的数据采集结果，当前为占位接口，尚未实现真实导出")
    public Result<String> exportData(
            @Parameter(description = "当前页码", example = "1")
            @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页条数", example = "10")
            @RequestParam(defaultValue = "10") Integer size,
            DataCollectionResult data) {
        return Result.success("导出功能开发中");
    }
}
