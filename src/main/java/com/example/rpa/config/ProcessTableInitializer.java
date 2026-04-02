package com.example.rpa.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProcessTableInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        createProcessTable();
        insertProcessTestData();
        insertDataCollectionTestData();
    }

    private void createProcessTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS rpa_process (
                id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '流程 ID',
                process_name VARCHAR(100) NOT NULL COMMENT '流程名称',
                process_code VARCHAR(50) NOT NULL COMMENT '流程编码',
                description VARCHAR(500) DEFAULT NULL COMMENT '流程描述',
                process_type TINYINT(1) DEFAULT 1 COMMENT '流程类型：1-数据采集，2-数据处理，3-自动化任务',
                script_content TEXT COMMENT '流程脚本内容',
                status TINYINT(1) DEFAULT 1 COMMENT '状态：0-停用，1-启用',
                create_by BIGINT(20) DEFAULT NULL COMMENT '创建人',
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
                PRIMARY KEY (id),
                UNIQUE KEY uk_process_code (process_code)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RPA流程管理表'
            """;
        
        try {
            jdbcTemplate.execute(sql);
            log.info("流程管理表创建成功或已存在");
        } catch (Exception e) {
            log.error("创建流程管理表失败", e);
        }
    }

    private void insertProcessTestData() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM rpa_process", Integer.class);
        
        if (count != null && count > 0) {
            log.info("流程数据已存在，跳过初始化");
            return;
        }

        String[][] processData = {
            {"股票数据采集流程", "stock_collection", "采集股票市场数据并存储到数据库", "1", "1"},
            {"基金净值处理流程", "fund_processing", "处理基金净值数据并生成报告", "2", "1"},
            {"日报自动生成流程", "daily_report", "自动生成每日数据采集报告", "3", "1"},
            {"汇率数据同步流程", "exchange_sync", "同步汇率数据到本地数据库", "1", "0"},
            {"财务报表处理流程", "finance_report", "处理财务报表数据", "2", "1"}
        };

        String insertSql = """
            INSERT INTO rpa_process (process_name, process_code, description, process_type, status, create_by)
            VALUES (?, ?, ?, ?, ?, 1)
            """;

        for (String[] data : processData) {
            try {
                jdbcTemplate.update(insertSql, 
                    data[0], 
                    data[1], 
                    data[2], 
                    Integer.parseInt(data[3]),
                    Integer.parseInt(data[4]));
            } catch (Exception e) {
                log.warn("插入流程数据失败: {}", data[0]);
            }
        }
        
        log.info("流程测试数据初始化完成，共 {} 条", processData.length);
    }

    private void insertDataCollectionTestData() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM data_collection_result", Integer.class);
        
        if (count != null && count > 0) {
            log.info("数据采集结果已存在，跳过初始化");
            return;
        }

        String insertSql = """
            INSERT INTO data_collection_result (id, task_id, task_name, data_source, data_content, collection_time)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        Object[][] dataResults = {
            {1L, 1001L, "股票行情采集", "东方财富网", "{\"stock\":\"平安银行\",\"code\":\"000001\",\"price\":15.68,\"change\":0.52}", "2026-03-25 09:00:15"},
            {2L, 1001L, "股票行情采集", "东方财富网", "{\"stock\":\"招商银行\",\"code\":\"600036\",\"price\":38.92,\"change\":-0.28}", "2026-03-25 09:00:15"},
            {3L, 1002L, "基金净值采集", "天天基金网", "{\"fund\":\"华夏成长\",\"code\":\"000001\",\"nav\":2.356,\"date\":\"2026-03-24\"}", "2026-03-24 18:30:25"},
            {4L, 1003L, "汇率数据采集", "中国银行", "{\"currency\":\"USD\",\"rate\":7.2456,\"date\":\"2026-03-25\"}", "2026-03-25 08:00:08"},
            {5L, 1004L, "财经新闻采集", "新浪财经", "{\"title\":\"央行降准释放流动性\",\"date\":\"2026-03-25\"}", "2026-03-25 09:00:20"},
            {6L, 1001L, "股票行情采集", "东方财富网", "{\"stock\":\"贵州茅台\",\"code\":\"600519\",\"price\":1856.00,\"change\":12.50}", "2026-03-24 15:00:12"},
            {7L, 1005L, "债券数据采集", "中国债券信息网", "{\"bond\":\"国债2301\",\"yield\":2.85,\"date\":\"2026-03-24\"}", "2026-03-24 17:00:30"},
            {8L, 1003L, "汇率数据采集", "中国银行", "{\"currency\":\"EUR\",\"rate\":7.8523,\"date\":\"2026-03-25\"}", "2026-03-25 06:00:07"},
            {9L, 1002L, "基金净值采集", "天天基金网", "{\"fund\":\"易方达蓝筹\",\"code\":\"005827\",\"nav\":1.892,\"date\":\"2026-03-23\"}", "2026-03-23 18:30:22"},
            {10L, 1004L, "财经新闻采集", "新浪财经", "{\"title\":\"A股三大指数集体收涨\",\"date\":\"2026-03-25\"}", "2026-03-25 08:00:18"}
        };

        for (Object[] data : dataResults) {
            try {
                jdbcTemplate.update(insertSql, data);
            } catch (Exception e) {
                log.warn("插入数据采集结果失败: {}", data[2]);
            }
        }
        
        log.info("数据采集结果初始化完成，共 {} 条", dataResults.length);
    }
}
