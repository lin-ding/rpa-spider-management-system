-- 任务管理相关表
-- 说明：
-- 1. rpa_task 用于存储任务配置，负责绑定流程与机器人。
-- 2. rpa_task_execution 用于存储每次任务执行实例。
-- 3. rpa_task_stage_log 用于存储单次执行中的阶段明细日志。

DROP TABLE IF EXISTS `rpa_task_stage_log`;
DROP TABLE IF EXISTS `rpa_task_execution`;
DROP TABLE IF EXISTS `rpa_task`;

CREATE TABLE `rpa_task` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `task_code` VARCHAR(64) NOT NULL COMMENT '任务编码，唯一',
    `task_name` VARCHAR(128) NOT NULL COMMENT '任务名称',
    `process_id` BIGINT(20) NOT NULL COMMENT '关联流程ID',
    `robot_id` BIGINT(20) NOT NULL COMMENT '关联机器人ID',
    `taxpayer_id` VARCHAR(64) DEFAULT NULL COMMENT '纳税人识别号',
    `enterprise_name` VARCHAR(128) DEFAULT NULL COMMENT '企业名称',
    `category` VARCHAR(64) DEFAULT NULL COMMENT '任务分类',
    `priority` TINYINT(1) NOT NULL DEFAULT '2' COMMENT '优先级：1-低，2-中，3-高',
    `config_status` TINYINT(1) NOT NULL DEFAULT '1' COMMENT '配置状态：0-禁用，1-启用',
    `execution_status` VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT '最近执行状态：pending-待执行，running-执行中，success-成功，failed-失败',
    `last_start_time` DATETIME DEFAULT NULL COMMENT '最近一次开始时间',
    `last_end_time` DATETIME DEFAULT NULL COMMENT '最近一次结束时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_by` BIGINT(20) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_rpa_task_code` (`task_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RPA任务表';

CREATE TABLE `rpa_task_execution` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '执行记录ID',
    `execution_no` VARCHAR(64) NOT NULL COMMENT '执行单号，唯一',
    `task_id` BIGINT(20) NOT NULL COMMENT '任务ID',
    `task_code` VARCHAR(64) NOT NULL COMMENT '任务编码快照',
    `task_name` VARCHAR(128) NOT NULL COMMENT '任务名称快照',
    `process_id` BIGINT(20) NOT NULL COMMENT '流程ID快照',
    `process_name` VARCHAR(128) DEFAULT NULL COMMENT '流程名称快照',
    `robot_id` BIGINT(20) NOT NULL COMMENT '机器人ID快照',
    `robot_name` VARCHAR(128) DEFAULT NULL COMMENT '机器人名称快照',
    `taxpayer_id` VARCHAR(64) DEFAULT NULL COMMENT '纳税人识别号快照',
    `enterprise_name` VARCHAR(128) DEFAULT NULL COMMENT '企业名称快照',
    `category` VARCHAR(64) DEFAULT NULL COMMENT '任务分类快照',
    `priority` TINYINT(1) NOT NULL DEFAULT '2' COMMENT '优先级快照：1-低，2-中，3-高',
    `trigger_type` VARCHAR(16) NOT NULL DEFAULT 'manual' COMMENT '触发方式：manual-手动，schedule-调度，retry-重试',
    `status` VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT '执行状态：pending-待执行，running-执行中，success-成功，failed-失败',
    `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    `duration_ms` BIGINT(20) DEFAULT NULL COMMENT '执行耗时（毫秒）',
    `error_message` TEXT COMMENT '错误信息',
    `log_detail` LONGTEXT COMMENT '执行详细日志',
    `create_by` BIGINT(20) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_rpa_task_execution_no` (`execution_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RPA任务执行记录表';

CREATE TABLE `rpa_task_stage_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '阶段日志ID',
    `execution_id` BIGINT(20) NOT NULL COMMENT '执行记录ID',
    `task_id` BIGINT(20) NOT NULL COMMENT '任务ID',
    `stage_order` INT(11) NOT NULL COMMENT '阶段顺序',
    `stage_name` VARCHAR(128) NOT NULL COMMENT '阶段名称',
    `status` VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT '阶段状态：pending-待执行，running-执行中，success-成功，failed-失败',
    `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    `duration_ms` BIGINT(20) DEFAULT NULL COMMENT '执行耗时（毫秒）',
    `error_message` TEXT COMMENT '错误信息',
    `log_detail` LONGTEXT COMMENT '阶段详细日志',
    `stage_result` TEXT COMMENT '阶段执行结果摘要',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RPA任务阶段日志表';
