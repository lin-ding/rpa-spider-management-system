-- 流程管理表
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RPA流程管理表';

-- 插入测试数据
INSERT INTO rpa_process (id, process_name, process_code, description, process_type, script_content, status, create_by) VALUES 
(1, '股票数据采集流程', 'stock_collection', '采集股票市场数据并存储到数据库', 1, 
'// 股票数据采集流程\ndef execute() {\n    def stocks = getStockList()\n    stocks.each { stock ->\n        def data = fetchStockData(stock.code)\n        saveToDatabase(data)\n    }\n    return "采集完成"\n}', 
1, 1),

(2, '基金净值处理流程', 'fund_processing', '处理基金净值数据并生成报告', 2, 
'// 基金净值处理流程\ndef execute() {\n    def funds = getFundList()\n    funds.each { fund ->\n        def navData = calculateNav(fund)\n        generateReport(navData)\n    }\n    return "处理完成"\n}', 
1, 1),

(3, '日报自动生成流程', 'daily_report', '自动生成每日数据采集报告', 3, 
'// 日报自动生成流程\ndef execute() {\n    def today = LocalDate.now()\n    def tasks = getTodayTasks()\n    def report = generateDailyReport(today, tasks)\n    sendEmail(report)\n    return "报告已发送"\n}', 
1, 1),

(4, '汇率数据同步流程', 'exchange_sync', '同步汇率数据到本地数据库', 1, 
'// 汇率数据同步流程\ndef execute() {\n    def rates = fetchExchangeRates()\n    rates.each { rate ->\n        updateExchangeRate(rate)\n    }\n    return "同步完成"\n}', 
0, 1),

(5, '财务报表处理流程', 'finance_report', '处理财务报表数据', 2, 
'// 财务报表处理流程\ndef execute() {\n    def reports = getFinanceReports()\n    reports.each { report ->\n        processData(report)\n        validateData(report)\n    }\n    return "处理完成"\n}', 
1, 1);
