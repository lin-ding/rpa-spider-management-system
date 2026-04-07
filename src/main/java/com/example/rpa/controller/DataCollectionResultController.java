package com.example.rpa.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.common.Result;
import com.example.rpa.entity.DataCollectionResult;
import com.example.rpa.service.DataCollectionResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/data")
@Tag(name = "数据采集结果", description = "提供数据采集结果的分页查询、详情查看和导出接口")
public class DataCollectionResultController {

    @Autowired
    private DataCollectionResultService dataCollectionResultService;

    @GetMapping("/query")
    @Operation(summary = "分页查询采集结果", description = "按任务和来源条件分页查询数据采集结果")
    public Result<Page<DataCollectionResult>> getDataList(
            @Parameter(description = "当前页码", example = "1")
            @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页条数", example = "10")
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String dataSource) {
        
        DataCollectionResult data = new DataCollectionResult();
        data.setTaskId(taskId);
        data.setTaskName(taskName);
        data.setDataSource(dataSource);
        
        Page<DataCollectionResult> page = dataCollectionResultService.getDataPage(current, size, data);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询采集结果详情", description = "根据采集结果主键 ID 查询单条数据详情")
    public Result<DataCollectionResult> getDataById(@Parameter(description = "采集结果主键 ID", required = true)
                                                    @PathVariable Long id) {
        DataCollectionResult result = dataCollectionResultService.getDataById(id);
        return Result.success(result);
    }

    @GetMapping("/export")
    @Operation(summary = "导出采集结果", description = "按筛选条件导出数据采集结果为 Excel 文件")
    public void exportData(
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String dataSource,
            HttpServletResponse response) throws IOException {
        
        DataCollectionResult data = new DataCollectionResult();
        data.setTaskId(taskId);
        data.setTaskName(taskName);
        data.setDataSource(dataSource);
        
        Page<DataCollectionResult> page = dataCollectionResultService.getDataPage(1, 10000, data);
        List<DataCollectionResult> dataList = page.getRecords();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("数据采集结果");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"数据ID", "任务ID", "任务名称", "数据来源", "采集时间", "数据内容"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = workbook.createCellStyle();
                style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                cell.setCellStyle(style);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < dataList.size(); i++) {
                Row row = sheet.createRow(i + 1);
                DataCollectionResult item = dataList.get(i);
                row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
                row.createCell(1).setCellValue(item.getTaskId() != null ? item.getTaskId() : 0);
                row.createCell(2).setCellValue(item.getTaskName() != null ? item.getTaskName() : "");
                row.createCell(3).setCellValue(item.getDataSource() != null ? item.getDataSource() : "");
                row.createCell(4).setCellValue(item.getCollectionTime() != null ? item.getCollectionTime().format(formatter) : "");
                row.createCell(5).setCellValue(item.getDataContent() != null ? item.getDataContent() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = URLEncoder.encode("数据采集结果_" + LocalDateTime.now().format(formatter) + ".xlsx", StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            workbook.write(response.getOutputStream());
        }
    }
}
