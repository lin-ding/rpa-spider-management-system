-- 为 rpa_process 表添加 process_data 字段，用于存储流程设计数据
ALTER TABLE `rpa_process` ADD COLUMN `process_data` TEXT COMMENT '流程设计数据(JSON格式)' AFTER `script_content`;
