DROP TABLE IF EXISTS `rpa_robot`;
CREATE TABLE `rpa_robot` (
                             `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '机器人ID',
                             `robot_name` varchar(100) NOT NULL COMMENT '机器人名称',
                             `robot_code` varchar(64) NOT NULL COMMENT '机器人编码，唯一',
                             `robot_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '机器人类型：1-无人值守，2-有人值守，3-爬虫节点',
                             `status` tinyint(1) NOT NULL DEFAULT '3' COMMENT '机器人状态：0-禁用，1-空闲，2-忙碌，3-离线，4-异常',
                             `host_name` varchar(100) DEFAULT NULL COMMENT '主机名称',
                             `host_ip` varchar(64) DEFAULT NULL COMMENT '主机IP',
                             `port` int(11) DEFAULT NULL COMMENT '服务端口',
                             `client_version` varchar(32) DEFAULT NULL COMMENT '客户端版本',
                             `last_heartbeat_time` datetime DEFAULT NULL COMMENT '最后心跳时间',
                             `last_online_time` datetime DEFAULT NULL COMMENT '最后在线时间',
                             `current_task_id` bigint(20) DEFAULT NULL COMMENT '当前任务ID',
                             `current_process_id` bigint(20) DEFAULT NULL COMMENT '当前流程ID',
                             `description` varchar(500) DEFAULT NULL COMMENT '描述',
                             `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                             `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `uk_robot_code` (`robot_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RPA机器人表';