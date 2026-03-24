package com.example.rpa.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.common.Result;
import com.example.rpa.entity.DataCollectionResult;
import com.example.rpa.service.DataCollectionResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/data")
public class DataCollectionResultController {

    @Autowired
    private DataCollectionResultService dataCollectionResultService;

    @GetMapping("/query")
    public Result<Page<DataCollectionResult>> getDataList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            DataCollectionResult data) {
        Page<DataCollectionResult> page = dataCollectionResultService.getDataPage(current, size, data);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<DataCollectionResult> getDataById(@PathVariable Long id) {
        DataCollectionResult result = dataCollectionResultService.getDataById(id);
        return Result.success(result);
    }

    @GetMapping("/export")
    public Result<String> exportData(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            DataCollectionResult data) {
        return Result.success("导出功能开发中");
    }
}
