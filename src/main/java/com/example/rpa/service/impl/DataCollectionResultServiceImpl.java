package com.example.rpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.entity.DataCollectionResult;
import com.example.rpa.exception.BusinessException;
import com.example.rpa.mapper.DataCollectionResultMapper;
import com.example.rpa.service.DataCollectionResultService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DataCollectionResultServiceImpl implements DataCollectionResultService {

    private static final Set<String> ALLOWED_DATA_STATUSES = Set.of("raw", "valid", "invalid", "processed", "failed", "duplicate");

    @Autowired
    private DataCollectionResultMapper dataCollectionResultMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Page<DataCollectionResult> getDataPage(Integer current, Integer size, DataCollectionResult data) {
        Page<DataCollectionResult> page = new Page<>(current, size);
        LambdaQueryWrapper<DataCollectionResult> wrapper = new LambdaQueryWrapper<>();
        
        if (data.getTaskId() != null) {
            wrapper.eq(DataCollectionResult::getTaskId, data.getTaskId());
        }
        if (StringUtils.hasText(data.getTaskName())) {
            wrapper.like(DataCollectionResult::getTaskName, data.getTaskName());
        }
        if (StringUtils.hasText(data.getDataSource())) {
            wrapper.like(DataCollectionResult::getDataSource, data.getDataSource());
        }
        if (StringUtils.hasText(data.getExecutionNo())) {
            wrapper.like(DataCollectionResult::getExecutionNo, data.getExecutionNo().trim());
        }
        if (StringUtils.hasText(data.getProcessName())) {
            wrapper.like(DataCollectionResult::getProcessName, data.getProcessName().trim());
        }
        if (StringUtils.hasText(data.getRobotName())) {
            wrapper.like(DataCollectionResult::getRobotName, data.getRobotName().trim());
        }
        if (StringUtils.hasText(data.getDataType())) {
            wrapper.eq(DataCollectionResult::getDataType, data.getDataType().trim());
        }
        if (StringUtils.hasText(data.getBusinessKey())) {
            wrapper.like(DataCollectionResult::getBusinessKey, data.getBusinessKey().trim());
        }
        if (StringUtils.hasText(data.getDataStatus())) {
            wrapper.eq(DataCollectionResult::getDataStatus, data.getDataStatus().trim());
        } else if (Boolean.TRUE.equals(data.getUsableOnly())) {
            wrapper.in(DataCollectionResult::getDataStatus, "valid", "processed");
        }
        wrapper.eq(data.getExecutionId() != null, DataCollectionResult::getExecutionId, data.getExecutionId());
        wrapper.ge(data.getCollectionTimeFrom() != null, DataCollectionResult::getCollectionTime, data.getCollectionTimeFrom());
        wrapper.le(data.getCollectionTimeTo() != null, DataCollectionResult::getCollectionTime, data.getCollectionTimeTo());
        wrapper.eq(DataCollectionResult::getDeleted, 0);
        
        wrapper.orderByDesc(DataCollectionResult::getCollectionTime);
        return dataCollectionResultMapper.selectPage(page, wrapper);
    }

    @Override
    public DataCollectionResult getDataById(Long id) {
        DataCollectionResult result = dataCollectionResultMapper.selectById(id);
        if (result == null) {
            throw new BusinessException("数据不存在");
        }
        return result;
    }

    @Override
    public void updateDataStatus(List<Long> ids, String dataStatus, String errorMessage) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要更新的数据");
        }
        if (!StringUtils.hasText(dataStatus) || !ALLOWED_DATA_STATUSES.contains(dataStatus.trim())) {
            throw new BusinessException("数据状态不合法");
        }

        DataCollectionResult update = new DataCollectionResult();
        update.setDataStatus(dataStatus.trim());
        update.setErrorMessage(StringUtils.hasText(errorMessage) ? errorMessage.trim() : null);
        update.setUpdateTime(LocalDateTime.now());

        LambdaQueryWrapper<DataCollectionResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(DataCollectionResult::getId, ids)
                .eq(DataCollectionResult::getDeleted, 0);
        dataCollectionResultMapper.update(update, wrapper);
    }

    @Override
    public void deleteDataById(Long id) {
        if (id == null) {
            throw new BusinessException("数据ID不能为空");
        }
        deleteDataBatch(List.of(id));
    }

    @Override
    public void deleteDataBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的数据");
        }

        DataCollectionResult update = new DataCollectionResult();
        update.setDeleted(1);
        update.setUpdateTime(LocalDateTime.now());

        LambdaQueryWrapper<DataCollectionResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(DataCollectionResult::getId, ids)
                .eq(DataCollectionResult::getDeleted, 0);
        dataCollectionResultMapper.update(update, wrapper);
    }

    @Override
    public void parseDataBatch(List<Long> ids, String parseType) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要解析的数据");
        }
        String normalizedType = StringUtils.hasText(parseType) ? parseType.trim().toLowerCase() : "auto";
        if (!Set.of("auto", "json", "text").contains(normalizedType)) {
            throw new BusinessException("解析类型不合法");
        }

        for (Long id : ids) {
            DataCollectionResult result = dataCollectionResultMapper.selectById(id);
            if (result == null || Integer.valueOf(1).equals(result.getDeleted())) {
                continue;
            }
            parseSingleData(result, normalizedType);
        }
    }

    @Override
    public void processDataBatch(List<Long> ids, String processingType) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要加工的数据");
        }
        String normalizedType = StringUtils.hasText(processingType) ? processingType.trim().toLowerCase() : "cleaning";
        if (!Set.of("cleaning", "conversion", "aggregation", "calculation").contains(normalizedType)) {
            throw new BusinessException("加工类型不合法");
        }

        for (Long id : ids) {
            DataCollectionResult result = dataCollectionResultMapper.selectById(id);
            if (result == null || Integer.valueOf(1).equals(result.getDeleted())) {
                continue;
            }
            processSingleData(result, normalizedType);
        }
    }

    private void processSingleData(DataCollectionResult result, String processingType) {
        DataCollectionResult update = new DataCollectionResult();
        update.setId(result.getId());
        update.setUpdateTime(LocalDateTime.now());

        if (!StringUtils.hasText(result.getDataContent())) {
            update.setDataStatus("failed");
            update.setErrorMessage("标准化内容为空，请先完成数据解析");
            dataCollectionResultMapper.updateById(update);
            return;
        }

        try {
            Object source = objectMapper.readValue(result.getDataContent(), Object.class);
            Object processed = switch (processingType) {
                case "conversion" -> flattenObject(source);
                case "aggregation" -> aggregateObject(source);
                case "calculation" -> calculateObject(source);
                default -> cleanObject(source);
            };
            update.setProcessedContent(objectMapper.writeValueAsString(processed));
            update.setDataStatus("processed");
            update.setErrorMessage(null);
        } catch (Exception e) {
            update.setDataStatus("failed");
            update.setErrorMessage("加工失败: " + e.getMessage());
        }
        dataCollectionResultMapper.updateById(update);
    }

    private Object cleanObject(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> cleaned = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object cleanedValue = cleanObject(entry.getValue());
                if (cleanedValue == null) {
                    continue;
                }
                if (cleanedValue instanceof String text && !StringUtils.hasText(text)) {
                    continue;
                }
                if (cleanedValue instanceof List<?> list && list.isEmpty()) {
                    continue;
                }
                if (cleanedValue instanceof Map<?, ?> childMap && childMap.isEmpty()) {
                    continue;
                }
                cleaned.put(String.valueOf(entry.getKey()).trim(), cleanedValue);
            }
            return cleaned;
        }
        if (value instanceof List<?> list) {
            List<Object> cleaned = new ArrayList<>();
            for (Object item : list) {
                Object cleanedValue = cleanObject(item);
                if (cleanedValue != null) {
                    cleaned.add(cleanedValue);
                }
            }
            return cleaned;
        }
        if (value instanceof String text) {
            return text.trim();
        }
        return value;
    }

    private Object flattenObject(Object value) {
        Map<String, Object> flattened = new LinkedHashMap<>();
        flattenInto("", value, flattened);
        return flattened;
    }

    private void flattenInto(String prefix, Object value, Map<String, Object> target) {
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey()).trim();
                String nextPrefix = StringUtils.hasText(prefix) ? prefix + "." + key : key;
                flattenInto(nextPrefix, entry.getValue(), target);
            }
            return;
        }
        if (value instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                flattenInto(prefix + "[" + i + "]", list.get(i), target);
            }
            return;
        }
        target.put(prefix, value);
    }

    private Object aggregateObject(Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (value instanceof List<?> list) {
            result.put("count", list.size());
            result.put("items", cleanObject(list));
            return result;
        }
        if (value instanceof Map<?, ?> map) {
            result.put("fieldCount", map.size());
            result.put("data", cleanObject(map));
            return result;
        }
        result.put("value", value);
        return result;
    }

    private Object calculateObject(Object value) {
        Map<String, Object> flattened = objectMapper.convertValue(flattenObject(value), new TypeReference<>() {
        });
        List<Number> numbers = flattened.values().stream()
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("numericFieldCount", numbers.size());
        if (!numbers.isEmpty()) {
            double sum = numbers.stream().mapToDouble(Number::doubleValue).sum();
            result.put("sum", sum);
            result.put("avg", sum / numbers.size());
            result.put("min", numbers.stream().mapToDouble(Number::doubleValue).min().orElse(0));
            result.put("max", numbers.stream().mapToDouble(Number::doubleValue).max().orElse(0));
        }
        result.put("data", cleanObject(value));
        return result;
    }

    private void parseSingleData(DataCollectionResult result, String parseType) {
        String sourceContent = firstText(result.getRawContent(), result.getDataContent());
        DataCollectionResult update = new DataCollectionResult();
        update.setId(result.getId());
        update.setUpdateTime(LocalDateTime.now());

        if (!StringUtils.hasText(sourceContent)) {
            update.setDataStatus("invalid");
            update.setErrorMessage("原始内容为空，无法解析");
            dataCollectionResultMapper.updateById(update);
            return;
        }

        try {
            String parsedContent = switch (parseType) {
                case "json" -> parseJsonContent(sourceContent);
                case "text" -> parseTextContent(sourceContent);
                default -> parseAutoContent(sourceContent);
            };
            update.setDataContent(parsedContent);
            update.setDataStatus("valid");
            update.setErrorMessage(null);
        } catch (Exception e) {
            update.setDataStatus("invalid");
            update.setErrorMessage("解析失败: " + e.getMessage());
        }
        dataCollectionResultMapper.updateById(update);
    }

    private String parseAutoContent(String content) throws JsonProcessingException {
        try {
            return parseJsonContent(content);
        } catch (JsonProcessingException e) {
            return parseTextContent(content);
        }
    }

    private String parseJsonContent(String content) throws JsonProcessingException {
        Object value = objectMapper.readValue(content, Object.class);
        return objectMapper.writeValueAsString(value);
    }

    private String parseTextContent(String content) throws JsonProcessingException {
        return objectMapper.writeValueAsString(java.util.Map.of("text", content));
    }

    private String firstText(String first, String second) {
        if (StringUtils.hasText(first)) {
            return first;
        }
        return StringUtils.hasText(second) ? second : null;
    }
}
