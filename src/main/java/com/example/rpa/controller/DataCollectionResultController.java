package com.example.rpa.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.common.Result;
import com.example.rpa.dto.DataAnalysisParseRequest;
import com.example.rpa.dto.DataProcessingRequest;
import com.example.rpa.dto.DataResultStatusRequest;
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

    private static final int EXCEL_CELL_TEXT_LIMIT = 32767;
    private static final int EXCEL_EXPORT_TEXT_LIMIT = 30000;

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
            @RequestParam(required = false) String dataSource,
            @RequestParam(required = false) Long executionId,
            @RequestParam(required = false) String executionNo,
            @RequestParam(required = false) String processName,
            @RequestParam(required = false) String robotName,
            @RequestParam(required = false) String dataType,
            @RequestParam(required = false) String businessKey,
            @RequestParam(required = false) String dataStatus,
            @RequestParam(required = false) Boolean usableOnly,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime collectionTimeFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime collectionTimeTo) {
        
        DataCollectionResult data = new DataCollectionResult();
        data.setTaskId(taskId);
        data.setTaskName(taskName);
        data.setDataSource(dataSource);
        data.setExecutionId(executionId);
        data.setExecutionNo(executionNo);
        data.setProcessName(processName);
        data.setRobotName(robotName);
        data.setDataType(dataType);
        data.setBusinessKey(businessKey);
        data.setDataStatus(dataStatus);
        data.setUsableOnly(usableOnly);
        data.setCollectionTimeFrom(collectionTimeFrom);
        data.setCollectionTimeTo(collectionTimeTo);
        
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

    @PutMapping("/status")
    @Operation(summary = "更新采集结果状态", description = "批量更新采集结果的数据状态和备注信息")
    public Result<Void> updateDataStatus(@RequestBody DataResultStatusRequest request) {
        dataCollectionResultService.updateDataStatus(request.getIds(), request.getDataStatus(), request.getErrorMessage());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除采集结果", description = "按主键软删除单条采集结果")
    public Result<Void> deleteDataById(@PathVariable Long id) {
        dataCollectionResultService.deleteDataById(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除采集结果", description = "按主键批量软删除采集结果")
    public Result<Void> deleteDataBatch(@RequestBody List<Long> ids) {
        dataCollectionResultService.deleteDataBatch(ids);
        return Result.success();
    }

    @PostMapping("/analysis/parse")
    @Operation(summary = "解析采集结果", description = "批量解析采集结果的原始内容并写回标准化数据内容")
    public Result<Void> parseDataBatch(@RequestBody DataAnalysisParseRequest request) {
        dataCollectionResultService.parseDataBatch(request.getIds(), request.getParseType());
        return Result.success();
    }

    @PostMapping("/processing/process")
    @Operation(summary = "加工采集结果", description = "批量加工标准化数据内容并写回处理后内容")
    public Result<Void> processDataBatch(@RequestBody DataProcessingRequest request) {
        dataCollectionResultService.processDataBatch(request.getIds(), request.getProcessingType());
        return Result.success();
    }

    @GetMapping("/export")
    @Operation(summary = "导出采集结果", description = "按筛选条件导出数据采集结果为 Excel 文件")
    public void exportData(
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String dataSource,
            @RequestParam(required = false) Long executionId,
            @RequestParam(required = false) String executionNo,
            @RequestParam(required = false) String processName,
            @RequestParam(required = false) String robotName,
            @RequestParam(required = false) String dataType,
            @RequestParam(required = false) String businessKey,
            @RequestParam(required = false) String dataStatus,
            @RequestParam(required = false) Boolean usableOnly,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime collectionTimeFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime collectionTimeTo,
            HttpServletResponse response) throws IOException {
        
        DataCollectionResult data = new DataCollectionResult();
        data.setTaskId(taskId);
        data.setTaskName(taskName);
        data.setDataSource(dataSource);
        data.setExecutionId(executionId);
        data.setExecutionNo(executionNo);
        data.setProcessName(processName);
        data.setRobotName(robotName);
        data.setDataType(dataType);
        data.setBusinessKey(businessKey);
        data.setDataStatus(dataStatus);
        data.setUsableOnly(usableOnly);
        data.setCollectionTimeFrom(collectionTimeFrom);
        data.setCollectionTimeTo(collectionTimeTo);
        
        Page<DataCollectionResult> page = dataCollectionResultService.getDataPage(1, 10000, data);
        List<DataCollectionResult> dataList = page.getRecords();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String fileName = URLEncoder.encode("数据采集结果_" + LocalDateTime.now().format(formatter) + ".xlsx", StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("数据采集结果");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"数据ID", "任务ID", "任务名称", "执行单号", "流程名称", "机器人名称", "数据来源", "数据类型", "业务键", "数据状态", "来源业务时间", "采集时间", "数据内容"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = workbook.createCellStyle();
                style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                cell.setCellStyle(style);
            }

            for (int i = 0; i < dataList.size(); i++) {
                Row row = sheet.createRow(i + 1);
                DataCollectionResult item = dataList.get(i);
                row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
                row.createCell(1).setCellValue(item.getTaskId() != null ? item.getTaskId() : 0);
                setTextCell(row, 2, item.getTaskName());
                setTextCell(row, 3, item.getExecutionNo());
                setTextCell(row, 4, item.getProcessName());
                setTextCell(row, 5, item.getRobotName());
                setTextCell(row, 6, item.getDataSource());
                setTextCell(row, 7, item.getDataType());
                setTextCell(row, 8, item.getBusinessKey());
                setTextCell(row, 9, item.getDataStatus());
                setTextCell(row, 10, item.getSourceTime() != null ? item.getSourceTime().format(formatter) : "");
                setTextCell(row, 11, item.getCollectionTime() != null ? item.getCollectionTime().format(formatter) : "");
                setTextCell(row, 12, firstText(item.getProcessedContent(), item.getDataContent(), item.getRawContent()));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }

    private void setTextCell(Row row, int columnIndex, String value) {
        row.createCell(columnIndex).setCellValue(limitExcelCellText(value));
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private String limitExcelCellText(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace("\u0000", "");
        int limit = Math.min(EXCEL_EXPORT_TEXT_LIMIT, EXCEL_CELL_TEXT_LIMIT);
        if (normalized.length() <= limit) {
            return normalized;
        }
        return normalized.substring(0, limit) + "\n...(内容过长，导出已截断，原始长度=" + normalized.length() + ")";
    }
}
