-- 插入测试数据脚本
-- 包含：爬虫任务、执行日志、部门等数据

USE rpa_system;

-- ========================================
-- 1. 部门数据
-- ========================================
INSERT INTO sys_department (id, parent_id, dept_name, dept_code, sort_order, leader, phone, email, status) VALUES 
(2, 1, '技术研发部', 'tech_dept', 1, '张三', '13800138010', 'tech@rpa.com', 1),
(3, 1, '数据采集部', 'data_dept', 2, '李四', '13800138011', 'data@rpa.com', 1),
(4, 1, '运营管理部', 'operation_dept', 3, '王五', '13800138012', 'operation@rpa.com', 1),
(5, 1, '财务部', 'finance_dept', 4, '赵六', '13800138013', 'finance@rpa.com', 1);

-- ========================================
-- 2. 爬虫任务数据
-- ========================================
INSERT INTO spider_task (id, task_name, task_code, task_type, url, script_content, cron_expression, status, last_run_time, next_run_time, run_count, create_by) VALUES 
-- 定时任务
(1001, '股票行情采集', 'stock_quote', 1, 'https://api.example.com/stock', 
 '-- 股票数据采集脚本
 def crawl() {
     def url = "https://api.example.com/stock"
     def data = httpGet(url)
     saveToDatabase(data)
     return "采集成功"
 }', 
 '0 0 9,15 * * ?', 1, '2026-03-25 09:00:00', '2026-03-25 15:00:00', 156, 1),

(1002, '基金净值采集', 'fund_nav', 1, 'https://api.example.com/fund', 
 '-- 基金净值采集脚本
 def crawl() {
     def url = "https://api.example.com/fund"
     def data = httpGet(url)
     processData(data)
     return "采集成功"
 }', 
 '0 30 18 * * ?', 1, '2026-03-24 18:30:00', '2026-03-25 18:30:00', 89, 1),

(1003, '汇率数据采集', 'exchange_rate', 1, 'https://api.example.com/exchange', 
 '-- 汇率数据采集脚本
 def crawl() {
     def url = "https://api.example.com/exchange"
     def data = httpGet(url)
     saveExchangeRate(data)
     return "采集成功"
 }', 
 '0 0 */2 * * ?', 1, '2026-03-25 08:00:00', '2026-03-25 10:00:00', 234, 1),

(1004, '财经新闻采集', 'finance_news', 1, 'https://api.example.com/news', 
 '-- 财经新闻采集脚本
 def crawl() {
     def url = "https://api.example.com/news"
     def data = httpGet(url)
     parseNews(data)
     return "采集成功"
 }', 
 '0 0 */1 * * ?', 1, '2026-03-25 09:00:00', '2026-03-25 10:00:00', 567, 1),

(1005, '债券数据采集', 'bond_data', 1, 'https://api.example.com/bond', 
 '-- 债券数据采集脚本
 def crawl() {
     def url = "https://api.example.com/bond"
     def data = httpGet(url)
     processBondData(data)
     return "采集成功"
 }', 
 '0 0 17 * * ?', 1, '2026-03-24 17:00:00', '2026-03-25 17:00:00', 78, 1),

-- 手动任务
(1006, '银行利率采集', 'bank_rate', 2, 'https://api.example.com/bank-rate', 
 '-- 银行利率采集脚本
 def crawl() {
     def url = "https://api.example.com/bank-rate"
     def data = httpGet(url)
     saveBankRate(data)
     return "采集成功"
 }', 
 NULL, 1, '2026-03-23 14:30:00', NULL, 12, 1),

(1007, '期货数据采集', 'futures_data', 2, 'https://api.example.com/futures', 
 '-- 期货数据采集脚本
 def crawl() {
     def url = "https://api.example.com/futures"
     def data = httpGet(url)
     processFuturesData(data)
     return "采集成功"
 }', 
 NULL, 0, '2026-03-22 16:00:00', NULL, 8, 1),

(1008, '上市公司财报采集', 'company_report', 2, 'https://api.example.com/reports', 
 '-- 上市公司财报采集脚本
 def crawl() {
     def url = "https://api.example.com/reports"
     def data = httpGet(url)
     parseFinancialReport(data)
     return "采集成功"
 }', 
 NULL, 1, '2026-03-21 10:00:00', NULL, 5, 1),

(1009, '宏观经济数据采集', 'macro_economy', 2, 'https://api.example.com/macro', 
 '-- 宏观经济数据采集脚本
 def crawl() {
     def url = "https://api.example.com/macro"
     def data = httpGet(url)
     saveMacroData(data)
     return "采集成功"
 }', 
 NULL, 1, '2026-03-20 09:30:00', NULL, 3, 1),

(1010, '行业指数采集', 'industry_index', 2, 'https://api.example.com/index', 
 '-- 行业指数采集脚本
 def crawl() {
     def url = "https://api.example.com/index"
     def data = httpGet(url)
     processIndexData(data)
     return "采集成功"
 }', 
 NULL, 0, '2026-03-19 15:00:00', NULL, 6, 1);

-- ========================================
-- 3. 任务执行日志数据
-- ========================================
INSERT INTO task_execution_log (id, task_id, task_name, start_time, end_time, duration, status, error_message, log_detail) VALUES 
-- 股票行情采集日志
(2001, 1001, '股票行情采集', '2026-03-25 09:00:00', '2026-03-25 09:00:15', 15000, 1, NULL, 
 '开始采集股票行情数据...
 连接API成功
 获取数据: 3500条
 数据解析成功
 保存到数据库成功
 任务执行完成'),

(2002, 1001, '股票行情采集', '2026-03-24 15:00:00', '2026-03-24 15:00:12', 12000, 1, NULL, 
 '开始采集股票行情数据...
 连接API成功
 获取数据: 3480条
 数据解析成功
 保存到数据库成功
 任务执行完成'),

(2003, 1001, '股票行情采集', '2026-03-24 09:00:00', '2026-03-24 09:00:18', 18000, 1, NULL, 
 '开始采集股票行情数据...
 连接API成功
 获取数据: 3520条
 数据解析成功
 保存到数据库成功
 任务执行完成'),

-- 基金净值采集日志
(2004, 1002, '基金净值采集', '2026-03-24 18:30:00', '2026-03-24 18:30:25', 25000, 1, NULL, 
 '开始采集基金净值数据...
 连接API成功
 获取数据: 8500条
 数据解析成功
 保存到数据库成功
 任务执行完成'),

(2005, 1002, '基金净值采集', '2026-03-23 18:30:00', '2026-03-23 18:30:22', 22000, 1, NULL, 
 '开始采集基金净值数据...
 连接API成功
 获取数据: 8450条
 数据解析成功
 保存到数据库成功
 任务执行完成'),

-- 汇率数据采集日志
(2006, 1003, '汇率数据采集', '2026-03-25 08:00:00', '2026-03-25 08:00:08', 8000, 1, NULL, 
 '开始采集汇率数据...
 连接API成功
 获取数据: 156条
 数据解析成功
 保存到数据库成功
 任务执行完成'),

(2007, 1003, '汇率数据采集', '2026-03-25 06:00:00', '2026-03-25 06:00:07', 7000, 1, NULL, 
 '开始采集汇率数据...
 连接API成功
 获取数据: 156条
 数据解析成功
 保存到数据库成功
 任务执行完成'),

-- 财经新闻采集日志
(2008, 1004, '财经新闻采集', '2026-03-25 09:00:00', '2026-03-25 09:00:20', 20000, 1, NULL, 
 '开始采集财经新闻...
 连接API成功
 获取数据: 120条
 数据解析成功
 保存到数据库成功
 任务执行完成'),

(2009, 1004, '财经新闻采集', '2026-03-25 08:00:00', '2026-03-25 08:00:18', 18000, 1, NULL, 
 '开始采集财经新闻...
 连接API成功
 获取数据: 115条
 数据解析成功
 保存到数据库成功
 任务执行完成'),

-- 债券数据采集日志
(2010, 1005, '债券数据采集', '2026-03-24 17:00:00', '2026-03-24 17:00:30', 30000, 1, NULL, 
 '开始采集债券数据...
 连接API成功
 获取数据: 2200条
 数据解析成功
 保存到数据库成功
 任务执行完成'),

-- 失败的任务日志
(2011, 1006, '银行利率采集', '2026-03-23 14:30:00', '2026-03-23 14:30:10', 10000, 0, 
 '连接超时: Connection timed out', 
 '开始采集银行利率数据...
 连接API失败
 错误: Connection timed out
 任务执行失败'),

(2012, 1007, '期货数据采集', '2026-03-22 16:00:00', '2026-03-22 16:00:05', 5000, 0, 
 'API返回错误: 403 Forbidden', 
 '开始采集期货数据...
 连接API失败
 错误: 403 Forbidden
 任务执行失败'),

(2013, 1010, '行业指数采集', '2026-03-19 15:00:00', '2026-03-19 15:00:08', 8000, 0, 
 '数据解析失败: Invalid JSON format', 
 '开始采集行业指数数据...
 连接API成功
 获取数据成功
 数据解析失败: Invalid JSON format
 任务执行失败'),

-- 手动任务执行日志
(2014, 1008, '上市公司财报采集', '2026-03-21 10:00:00', '2026-03-21 10:02:30', 150000, 1, NULL, 
 '开始采集上市公司财报...
 连接API成功
 获取数据: 450家公司
 数据解析成功
 保存到数据库成功
 任务执行完成'),

(2015, 1009, '宏观经济数据采集', '2026-03-20 09:30:00', '2026-03-20 09:31:20', 80000, 1, NULL, 
 '开始采集宏观经济数据...
 连接API成功
 获取数据: GDP、CPI、PMI等指标
 数据解析成功
 保存到数据库成功
 任务执行完成');

-- ========================================
-- 4. 更新用户部门信息
-- ========================================
UPDATE sys_user SET dept_id = 2 WHERE username = 'operator';
UPDATE sys_user SET dept_id = 3 WHERE username = 'tech';
UPDATE sys_user SET dept_id = 4 WHERE username = 'finance';
UPDATE sys_user SET dept_id = 5 WHERE username = 'guest';

-- ========================================
-- 5. 查询验证
-- ========================================
SELECT '=== 部门数据 ===' AS info;
SELECT id, dept_name, dept_code, leader FROM sys_department WHERE id > 1;

SELECT '=== 任务数据 ===' AS info;
SELECT id, task_name, task_code, task_type, 
       CASE task_type WHEN 1 THEN '定时任务' WHEN 2 THEN '手动任务' END AS task_type_name,
       status, run_count 
FROM spider_task;

SELECT '=== 执行日志统计 ===' AS info;
SELECT 
    task_name,
    COUNT(*) AS total_count,
    SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS success_count,
    SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) AS fail_count,
    AVG(duration) AS avg_duration
FROM task_execution_log
GROUP BY task_name
ORDER BY total_count DESC;
