-- 为 data_collection_result 表添加 category 和 data_status 字段
ALTER TABLE `data_collection_result` 
ADD COLUMN `category` varchar(100) DEFAULT NULL COMMENT '数据分类' AFTER `data_source`,
ADD COLUMN `data_status` varchar(20) DEFAULT 'available' COMMENT '数据状态：available-可用，invalid-无效' AFTER `category`;
