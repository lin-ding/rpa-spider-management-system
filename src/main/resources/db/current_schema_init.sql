-- 当前系统数据库初始化脚本
-- 适用范围：
-- 1. 当前仍在使用的 15 张表
-- 2. 包含基础建表语句
-- 3. 包含基础测试数据，方便新环境直接初始化
--
-- 主要模块：
-- - 流程管理：rpa_process
-- - 机器人管理：rpa_robot
-- - 任务管理：rpa_task / rpa_task_execution / rpa_task_stage_log
-- - 数据结果：data_collection_result
-- - 用户权限：sys_user / sys_role / sys_user_role / sys_resource / sys_role_resource
-- - 系统管理：sys_department / sys_menu / sys_role_menu / sys_operation_log

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------
-- 一、清理旧表
-- -----------------------------------------------------

DROP TABLE IF EXISTS `data_collection_result`;
DROP TABLE IF EXISTS `rpa_task_stage_log`;
DROP TABLE IF EXISTS `rpa_task_execution`;
DROP TABLE IF EXISTS `rpa_task`;
DROP TABLE IF EXISTS `rpa_robot`;
DROP TABLE IF EXISTS `rpa_process`;
DROP TABLE IF EXISTS `sys_role_resource`;
DROP TABLE IF EXISTS `sys_role_menu`;
DROP TABLE IF EXISTS `sys_user_role`;
DROP TABLE IF EXISTS `sys_resource`;
DROP TABLE IF EXISTS `sys_menu`;
DROP TABLE IF EXISTS `sys_operation_log`;
DROP TABLE IF EXISTS `sys_role`;
DROP TABLE IF EXISTS `sys_user`;
DROP TABLE IF EXISTS `sys_department`;

-- -----------------------------------------------------
-- 二、流程管理
-- -----------------------------------------------------

-- 表：rpa_process
-- 作用：流程模板主表，保存流程基础信息和流程设计数据（阶段、脚本、脚本语言等）
CREATE TABLE `rpa_process` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '流程ID',
  `process_name` VARCHAR(100) NOT NULL COMMENT '流程名称',
  `process_code` VARCHAR(50) NOT NULL COMMENT '流程编码',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '流程描述',
  `process_type` TINYINT(1) DEFAULT '1' COMMENT '流程类型：1-数据采集，2-数据处理，3-自动化任务',
  `script_content` TEXT COMMENT '旧版脚本内容字段，当前主要保留兼容',
  `process_data` LONGTEXT COMMENT '流程设计数据(JSON)，包含阶段及脚本内容',
  `status` TINYINT(1) DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_by` BIGINT(20) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_process_code` (`process_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RPA流程表';

-- -----------------------------------------------------
-- 三、机器人管理
-- -----------------------------------------------------

-- 表：rpa_robot
-- 作用：机器人资源表，保存机器人状态、心跳、当前任务占用信息
CREATE TABLE `rpa_robot` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '机器人ID',
  `robot_name` VARCHAR(100) NOT NULL COMMENT '机器人名称',
  `robot_code` VARCHAR(64) NOT NULL COMMENT '机器人编码',
  `robot_type` TINYINT(1) NOT NULL DEFAULT '1' COMMENT '机器人类型：1-无人值守，2-有人值守，3-爬虫节点',
  `status` TINYINT(1) NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-空闲，2-工作中，3-离线，4-异常',
  `host_name` VARCHAR(100) DEFAULT NULL COMMENT '主机名称',
  `host_ip` VARCHAR(64) DEFAULT NULL COMMENT '主机IP',
  `port` INT(11) DEFAULT NULL COMMENT '服务端口',
  `client_version` VARCHAR(32) DEFAULT NULL COMMENT '客户端版本',
  `last_heartbeat_time` DATETIME DEFAULT NULL COMMENT '最后心跳时间',
  `last_online_time` DATETIME DEFAULT NULL COMMENT '最后在线时间',
  `current_task_id` BIGINT(20) DEFAULT NULL COMMENT '当前任务ID',
  `current_process_id` BIGINT(20) DEFAULT NULL COMMENT '当前流程ID',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
  `create_by` BIGINT(20) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_robot_code` (`robot_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RPA机器人表';

-- -----------------------------------------------------
-- 四、任务管理
-- -----------------------------------------------------

-- 表：rpa_task
-- 作用：任务配置主表，绑定流程和机器人，并维护任务当前状态和最近执行结果快照
CREATE TABLE `rpa_task` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `task_code` VARCHAR(64) NOT NULL COMMENT '任务编码',
  `task_name` VARCHAR(128) NOT NULL COMMENT '任务名称',
  `process_id` BIGINT(20) NOT NULL COMMENT '关联流程ID',
  `robot_id` BIGINT(20) NOT NULL COMMENT '关联机器人ID',
  `taxpayer_id` VARCHAR(64) DEFAULT NULL COMMENT '纳税人识别号',
  `enterprise_name` VARCHAR(128) DEFAULT NULL COMMENT '企业名称',
  `category` VARCHAR(64) DEFAULT NULL COMMENT '任务分类',
  `priority` TINYINT(1) NOT NULL DEFAULT '2' COMMENT '优先级：1-低，2-中，3-高',
  `config_status` TINYINT(1) NOT NULL DEFAULT '1' COMMENT '配置状态：0-禁用，1-启用',
  `execution_status` VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT '当前状态：pending/queued/running/success/failed',
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

-- 表：rpa_task_execution
-- 作用：任务执行记录表，保存每次执行实例和执行时快照信息
CREATE TABLE `rpa_task_execution` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '执行记录ID',
  `execution_no` VARCHAR(64) NOT NULL COMMENT '执行单号',
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
  `priority` TINYINT(1) NOT NULL DEFAULT '2' COMMENT '优先级快照',
  `trigger_type` VARCHAR(16) NOT NULL DEFAULT 'manual' COMMENT '触发方式：manual/schedule/retry',
  `status` VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT '执行状态：pending/queued/running/success/failed',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `duration_ms` BIGINT(20) DEFAULT NULL COMMENT '耗时(毫秒)',
  `error_message` TEXT COMMENT '错误信息',
  `log_detail` LONGTEXT COMMENT '详细日志',
  `create_by` BIGINT(20) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rpa_task_execution_no` (`execution_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RPA任务执行记录表';

-- 表：rpa_task_stage_log
-- 作用：阶段日志表，保存单次执行中的每一个阶段结果
CREATE TABLE `rpa_task_stage_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '阶段日志ID',
  `execution_id` BIGINT(20) NOT NULL COMMENT '执行记录ID',
  `task_id` BIGINT(20) NOT NULL COMMENT '任务ID',
  `stage_order` INT(11) NOT NULL COMMENT '阶段顺序',
  `stage_name` VARCHAR(128) NOT NULL COMMENT '阶段名称',
  `status` VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT '阶段状态：pending/running/success/failed',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `duration_ms` BIGINT(20) DEFAULT NULL COMMENT '耗时(毫秒)',
  `error_message` TEXT COMMENT '错误信息',
  `log_detail` LONGTEXT COMMENT '详细日志',
  `stage_result` TEXT COMMENT '阶段结果(JSON摘要)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RPA任务阶段日志表';

-- -----------------------------------------------------
-- 五、数据管理
-- -----------------------------------------------------

-- 表：data_collection_result
-- 作用：数据采集结果表，用于保存结构化采集结果或导出内容
CREATE TABLE `data_collection_result` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '数据ID',
  `task_id` BIGINT(20) NOT NULL COMMENT '任务ID',
  `task_name` VARCHAR(100) DEFAULT NULL COMMENT '任务名称',
  `data_source` VARCHAR(200) DEFAULT NULL COMMENT '数据来源',
  `data_content` JSON DEFAULT NULL COMMENT '采集结果(JSON)',
  `file_path` VARCHAR(500) DEFAULT NULL COMMENT '附件路径',
  `collection_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '采集时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_collection_time` (`collection_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据采集结果表';

-- -----------------------------------------------------
-- 六、系统管理
-- -----------------------------------------------------

-- 表：sys_department
-- 作用：部门表，用户可绑定部门
CREATE TABLE `sys_department` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `parent_id` BIGINT(20) DEFAULT '0' COMMENT '父部门ID',
  `dept_name` VARCHAR(50) NOT NULL COMMENT '部门名称',
  `dept_code` VARCHAR(50) DEFAULT NULL COMMENT '部门编码',
  `sort_order` INT(4) DEFAULT '0' COMMENT '排序号',
  `leader` VARCHAR(50) DEFAULT NULL COMMENT '负责人',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `status` TINYINT(1) DEFAULT '1' COMMENT '状态：0-禁用，1-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统部门表';

-- 表：sys_user
-- 作用：系统用户表，登录、用户管理的核心表
CREATE TABLE `sys_user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `status` TINYINT(1) DEFAULT '1' COMMENT '状态：0-禁用，1-正常',
  `dept_id` BIGINT(20) DEFAULT NULL COMMENT '部门ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 表：sys_role
-- 作用：系统角色表
CREATE TABLE `sys_role` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
  `status` TINYINT(1) DEFAULT '1' COMMENT '状态：0-禁用，1-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- 表：sys_user_role
-- 作用：用户角色关联表
CREATE TABLE `sys_user_role` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `role_id` BIGINT(20) NOT NULL COMMENT '角色ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 表：sys_menu
-- 作用：菜单表，当前主要服务于系统管理与菜单权限配置
CREATE TABLE `sys_menu` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` BIGINT(20) DEFAULT '0' COMMENT '父菜单ID',
  `menu_name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
  `menu_type` TINYINT(1) DEFAULT '1' COMMENT '菜单类型：1-目录，2-菜单，3-按钮',
  `path` VARCHAR(200) DEFAULT NULL COMMENT '路由地址',
  `component` VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
  `permission` VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
  `icon` VARCHAR(50) DEFAULT NULL COMMENT '菜单图标',
  `sort_order` INT(4) DEFAULT '0' COMMENT '排序号',
  `status` TINYINT(1) DEFAULT '1' COMMENT '状态：0-禁用，1-正常',
  `visible` TINYINT(1) DEFAULT '1' COMMENT '是否可见：0-隐藏，1-显示',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单表';

-- 表：sys_role_menu
-- 作用：角色菜单关联表
CREATE TABLE `sys_role_menu` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` BIGINT(20) NOT NULL COMMENT '角色ID',
  `menu_id` BIGINT(20) NOT NULL COMMENT '菜单ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 表：sys_resource
-- 作用：资源表，用于资源级权限控制
CREATE TABLE `sys_resource` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `parent_id` BIGINT(20) DEFAULT NULL COMMENT '父资源ID',
  `resource_code` VARCHAR(100) NOT NULL COMMENT '资源编码',
  `resource_name` VARCHAR(100) NOT NULL COMMENT '资源名称',
  `resource_type` VARCHAR(20) NOT NULL DEFAULT 'menu' COMMENT '资源类型：menu/button/api',
  `url` VARCHAR(200) DEFAULT NULL COMMENT '资源路径',
  `icon` VARCHAR(100) DEFAULT NULL COMMENT '图标',
  `sort` INT(11) DEFAULT '0' COMMENT '排序',
  `status` TINYINT(1) DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resource_code` (`resource_code`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统资源表';

-- 表：sys_role_resource
-- 作用：角色资源关联表
CREATE TABLE `sys_role_resource` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` BIGINT(20) NOT NULL COMMENT '角色ID',
  `resource_id` BIGINT(20) NOT NULL COMMENT '资源ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_resource` (`role_id`, `resource_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_resource_id` (`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色资源关联表';

-- 表：sys_operation_log
-- 作用：操作日志表，用于后续审计扩展
CREATE TABLE `sys_operation_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `module` VARCHAR(50) DEFAULT NULL COMMENT '操作模块',
  `business_type` VARCHAR(50) DEFAULT NULL COMMENT '业务类型',
  `method` VARCHAR(100) DEFAULT NULL COMMENT '请求方法',
  `operator` VARCHAR(50) DEFAULT NULL COMMENT '操作人员',
  `dept_name` VARCHAR(50) DEFAULT NULL COMMENT '部门名称',
  `operation_url` VARCHAR(255) DEFAULT NULL COMMENT '请求URL',
  `operation_ip` VARCHAR(50) DEFAULT NULL COMMENT '主机地址',
  `operation_location` VARCHAR(255) DEFAULT NULL COMMENT '操作地点',
  `request_params` VARCHAR(2000) DEFAULT NULL COMMENT '请求参数',
  `operation_status` TINYINT(1) DEFAULT '0' COMMENT '操作状态：0-失败，1-成功',
  `error_message` VARCHAR(2000) DEFAULT NULL COMMENT '错误消息',
  `operation_time` DATETIME DEFAULT NULL COMMENT '操作时间',
  `cost_time` BIGINT(20) DEFAULT NULL COMMENT '耗时(毫秒)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_operator` (`operator`),
  KEY `idx_operation_time` (`operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志表';

-- -----------------------------------------------------
-- 七、基础初始化数据
-- -----------------------------------------------------

-- 部门
INSERT INTO `sys_department` (`id`, `parent_id`, `dept_name`, `dept_code`, `sort_order`, `leader`, `phone`, `email`, `status`) VALUES
(1, 0, '总公司', 'headquarters', 1, '系统管理员', '13800138000', 'admin@rpa.com', 1),
(2, 1, '技术研发部', 'tech_dept', 2, '张三', '13800138010', 'tech@rpa.com', 1),
(3, 1, '数据采集部', 'data_dept', 3, '李四', '13800138011', 'data@rpa.com', 1);

-- 用户
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `email`, `phone`, `status`, `dept_id`) VALUES
(1, 'admin', '$2a$10$VtDPGV6XfFZ.a.eqSoa3oO9UNqMhsMGJTpaMI6cXYBtujS4msTELO', '系统管理员', 'admin@rpa.com', '13800138000', 1, 1),
(2, 'operator', '$2a$10$VtDPGV6XfFZ.a.eqSoa3oO9UNqMhsMGJTpaMI6cXYBtujS4msTELO', '操作员', 'operator@rpa.com', '13900139000', 1, 2),
(3, 'auditor', '$2a$10$VtDPGV6XfFZ.a.eqSoa3oO9UNqMhsMGJTpaMI6cXYBtujS4msTELO', '审计员', 'auditor@rpa.com', '13800138007', 1, 3);

-- 角色
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`, `status`) VALUES
(1, '超级管理员', 'super_admin', '拥有系统全部权限', 1),
(2, '普通用户', 'common_user', '普通功能使用权限', 1),
(3, '审计员', 'auditor', '可查看执行和日志信息', 1);

-- 用户角色
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`) VALUES
(1, 1, 1),
(2, 2, 2),
(3, 3, 3);

-- 菜单
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort_order`, `status`, `visible`) VALUES
(1, 0, '任务管理', 1, '/task', 'Layout', NULL, 'List', 1, 1, 1),
(2, 1, '任务列表', 2, '/task/list', 'task/TaskList', 'task:list', 'Document', 1, 1, 1),
(3, 1, '执行监控', 2, '/task/execution', 'execution/ExecutionList', 'execution:list', 'VideoPlay', 2, 1, 1),
(4, 0, '流程管理', 1, '/process', 'Layout', NULL, 'Operation', 2, 1, 1),
(5, 4, '流程列表', 2, '/process/list', 'process/ProcessList', 'process:list', 'Document', 1, 1, 1),
(6, 0, '机器人管理', 1, '/robot', 'Layout', NULL, 'Monitor', 3, 1, 1),
(7, 6, '机器人列表', 2, '/robot/list', 'robot/RobotList', 'robot:list', 'Document', 1, 1, 1),
(8, 0, '系统管理', 1, '/system', 'Layout', NULL, 'Setting', 4, 1, 1),
(9, 8, '用户管理', 2, '/system/user', 'user/UserList', 'system:user:list', 'User', 1, 1, 1),
(10, 8, '角色管理', 2, '/system/role', 'user/RoleList', 'system:role:list', 'UserFilled', 2, 1, 1),
(11, 8, '资源管理', 2, '/system/resource', 'user/ResourceList', 'system:resource:list', 'Folder', 3, 1, 1);

-- 角色菜单
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`) VALUES
(1, 1, 1), (2, 1, 2), (3, 1, 3), (4, 1, 4), (5, 1, 5), (6, 1, 6), (7, 1, 7), (8, 1, 8), (9, 1, 9), (10, 1, 10), (11, 1, 11),
(12, 2, 1), (13, 2, 2), (14, 2, 3), (15, 2, 4), (16, 2, 5), (17, 2, 6), (18, 2, 7),
(19, 3, 1), (20, 3, 3);

-- 资源
INSERT INTO `sys_resource` (`id`, `parent_id`, `resource_code`, `resource_name`, `resource_type`, `url`, `icon`, `sort`, `status`) VALUES
(1, NULL, 'system', '系统管理', 'menu', '/system', 'Setting', 1, 1),
(2, 1, 'user', '用户管理', 'menu', '/system/user', 'User', 1, 1),
(3, 1, 'role', '角色管理', 'menu', '/system/role', 'UserFilled', 2, 1),
(4, 1, 'resource', '资源管理', 'menu', '/system/resource', 'Folder', 3, 1),
(5, 2, 'user:add', '新增用户', 'button', NULL, NULL, 1, 1),
(6, 2, 'user:edit', '编辑用户', 'button', NULL, NULL, 2, 1),
(7, 2, 'user:delete', '删除用户', 'button', NULL, NULL, 3, 1),
(8, 3, 'role:add', '新增角色', 'button', NULL, NULL, 1, 1),
(9, 3, 'role:edit', '编辑角色', 'button', NULL, NULL, 2, 1),
(10, 3, 'role:delete', '删除角色', 'button', NULL, NULL, 3, 1);

-- 角色资源
INSERT INTO `sys_role_resource` (`id`, `role_id`, `resource_id`) VALUES
(1, 1, 1), (2, 1, 2), (3, 1, 3), (4, 1, 4), (5, 1, 5), (6, 1, 6), (7, 1, 7), (8, 1, 8), (9, 1, 9), (10, 1, 10),
(11, 2, 1), (12, 2, 2), (13, 2, 3),
(14, 3, 1);

-- 流程
INSERT INTO `rpa_process` (`id`, `process_name`, `process_code`, `description`, `process_type`, `script_content`, `process_data`, `status`, `create_by`) VALUES
(1, '网页标题采集测试流程', 'web_title_test', '用于测试网页采集和标题解析', 1, NULL,
'{"stages":[{"id":"stage_collect","name":"采集阶段","description":"请求网页HTML","scriptLanguage":"groovy","script":"def targetUrl = ''https://example.com''\\ndef html = new URL(targetUrl).getText(''UTF-8'')\\nresult.put(''targetUrl'', targetUrl)\\nresult.put(''html'', html)\\nreturn [crawlSuccess:true,targetUrl:targetUrl,html:html]"},{"id":"stage_parse","name":"解析阶段","description":"提取标题","scriptLanguage":"groovy","script":"def html = context.html ?: ''''\\ndef matcher = (html =~ /<title>(.*?)<\\\\/title>/)\\nif (!matcher.find()) { throw new RuntimeException(''未找到title标签'') }\\ndef pageTitle = matcher.group(1)?.trim()\\nresult.put(''pageTitle'', pageTitle)\\nreturn [parseSuccess:true,pageTitle:pageTitle]"}]}',
1, 1),
(2, 'Python示例流程', 'python_demo', '用于测试 Python 阶段脚本', 1, NULL,
'{"stages":[{"id":"stage_py","name":"Python阶段","description":"Python脚本示例","scriptLanguage":"python","script":"name = context.get(''enterpriseName'') or ''demo''\\nresult[''message''] = f''hello {name}''\\nresult[''stageSuccess''] = True"}]}',
1, 1);

-- 机器人
INSERT INTO `rpa_robot` (`id`, `robot_name`, `robot_code`, `robot_type`, `status`, `host_name`, `host_ip`, `port`, `client_version`, `last_heartbeat_time`, `last_online_time`, `description`, `create_by`) VALUES
(1, '测试机器人1', 'robot_test_001', 1, 1, 'HOST-001', '127.0.0.1', 9001, '1.0.0', NOW(), NOW(), '用于任务调试', 1),
(2, '测试机器人2', 'robot_test_002', 1, 1, 'HOST-002', '127.0.0.1', 9002, '1.0.0', NOW(), NOW(), '用于并行测试', 1),
(3, 'Python节点', 'robot_python_001', 3, 1, 'HOST-003', '127.0.0.1', 9003, '1.0.0', NOW(), NOW(), '用于Python流程测试', 1);

-- 任务
INSERT INTO `rpa_task` (`id`, `task_code`, `task_name`, `process_id`, `robot_id`, `taxpayer_id`, `enterprise_name`, `category`, `priority`, `config_status`, `execution_status`, `remark`, `create_by`) VALUES
(1, 'task_web_title_001', '网页标题采集测试', 1, 1, '91310000TEST0001', '示例科技有限公司', '网页采集', 2, 1, 'pending', '测试网页标题采集流程', 1),
(2, 'task_python_demo_001', 'Python流程测试任务', 2, 3, '91310000TEST0002', 'Python测试企业', '脚本测试', 2, 1, 'pending', '测试Python阶段脚本执行', 1);

-- 任务执行记录
INSERT INTO `rpa_task_execution` (`id`, `execution_no`, `task_id`, `task_code`, `task_name`, `process_id`, `process_name`, `robot_id`, `robot_name`, `taxpayer_id`, `enterprise_name`, `category`, `priority`, `trigger_type`, `status`, `start_time`, `end_time`, `duration_ms`, `error_message`, `log_detail`, `create_by`) VALUES
(1, 'EXEC-DEMO-0001', 1, 'task_web_title_001', '网页标题采集测试', 1, '网页标题采集测试流程', 1, '测试机器人1', '91310000TEST0001', '示例科技有限公司', '网页采集', 2, 'manual', 'success', '2026-04-16 10:00:00', '2026-04-16 10:00:03', 3000, NULL, '流程执行成功', 1),
(2, 'EXEC-DEMO-0002', 2, 'task_python_demo_001', 'Python流程测试任务', 2, 'Python示例流程', 3, 'Python节点', '91310000TEST0002', 'Python测试企业', '脚本测试', 2, 'manual', 'failed', '2026-04-16 10:05:00', '2026-04-16 10:05:01', 1000, '示例失败信息', '流程执行失败', 1);

-- 阶段日志
INSERT INTO `rpa_task_stage_log` (`id`, `execution_id`, `task_id`, `stage_order`, `stage_name`, `status`, `start_time`, `end_time`, `duration_ms`, `error_message`, `log_detail`, `stage_result`) VALUES
(1, 1, 1, 1, '采集阶段', 'success', '2026-04-16 10:00:00', '2026-04-16 10:00:02', 2000, NULL, '阶段执行成功', '{"crawlSuccess":true,"targetUrl":"https://example.com"}'),
(2, 1, 1, 2, '解析阶段', 'success', '2026-04-16 10:00:02', '2026-04-16 10:00:03', 1000, NULL, '阶段执行成功', '{"parseSuccess":true,"pageTitle":"Example Domain"}'),
(3, 2, 2, 1, 'Python阶段', 'failed', '2026-04-16 10:05:00', '2026-04-16 10:05:01', 1000, '示例失败信息', '阶段执行失败', '{"stageSuccess":false}');

-- 数据结果
INSERT INTO `data_collection_result` (`id`, `task_id`, `task_name`, `data_source`, `data_content`, `file_path`, `collection_time`) VALUES
(1, 1, '网页标题采集测试', 'https://example.com', JSON_OBJECT('pageTitle', 'Example Domain', 'targetUrl', 'https://example.com'), NULL, '2026-04-16 10:00:03'),
(2, 2, 'Python流程测试任务', 'Python阶段', JSON_OBJECT('message', 'hello Python测试企业'), NULL, '2026-04-16 10:05:01');

SET FOREIGN_KEY_CHECKS = 1;

