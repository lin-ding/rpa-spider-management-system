# Host: localhost  (Version 5.7.44)
# Date: 2026-03-26 21:41:44
# Generator: MySQL-Front 6.1  (Build 1.26)


#
# Structure for table "data_collection_result"
#

DROP TABLE IF EXISTS `data_collection_result`;
CREATE TABLE `data_collection_result` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '数据 ID',
  `task_id` bigint(20) NOT NULL COMMENT '任务 ID',
  `task_name` varchar(100) DEFAULT NULL COMMENT '任务名称',
  `data_source` varchar(200) DEFAULT NULL COMMENT '数据来源',
  `data_content` json DEFAULT NULL COMMENT '采集的数据内容 (JSON 格式)',
  `file_path` varchar(500) DEFAULT NULL COMMENT '附件路径',
  `collection_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '采集时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_collection_time` (`collection_time`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COMMENT='数据采集结果表';

#
# Data for table "data_collection_result"
#

INSERT INTO `data_collection_result` VALUES (1,1001,'股票行情采集','东方财富网',X'7B22636F6465223A2022303030303031222C20227072696365223A2031352E36382C202273746F636B223A2022E5B9B3E5AE89E993B6E8A18C222C20226368616E6765223A20302E35327D',NULL,'2026-03-25 09:00:15','2026-03-25 12:10:09'),(2,1001,'股票行情采集','东方财富网',X'7B22636F6465223A2022363030303336222C20227072696365223A2033382E39322C202273746F636B223A2022E68B9BE59586E993B6E8A18C222C20226368616E6765223A202D302E32387D',NULL,'2026-03-25 09:00:15','2026-03-25 12:10:09'),(3,1002,'基金净值采集','天天基金网',X'7B226E6176223A20322E3335362C2022636F6465223A2022303030303031222C202264617465223A2022323032362D30332D3234222C202266756E64223A2022E58D8EE5A48FE68890E995BF227D',NULL,'2026-03-24 18:30:25','2026-03-25 12:10:09'),(4,1003,'汇率数据采集','中国银行',X'7B2264617465223A2022323032362D30332D3235222C202272617465223A20372E323435362C202263757272656E6379223A2022555344227D',NULL,'2026-03-25 08:00:08','2026-03-25 12:10:09'),(5,1004,'财经新闻采集','新浪财经',X'7B2264617465223A2022323032362D30332D3235222C20227469746C65223A2022E5A4AEE8A18CE9998DE58786E9878AE694BEE6B581E58AA8E680A7227D',NULL,'2026-03-25 09:00:20','2026-03-25 12:10:09'),(6,1001,'股票行情采集','东方财富网',X'7B22636F6465223A2022363030353139222C20227072696365223A20313835362C202273746F636B223A2022E8B4B5E5B79EE88C85E58FB0222C20226368616E6765223A2031322E357D',NULL,'2026-03-24 15:00:12','2026-03-25 12:10:09'),(7,1005,'债券数据采集','中国债券信息网',X'7B22626F6E64223A2022E59BBDE580BA32333031222C202264617465223A2022323032362D30332D3234222C20227969656C64223A20322E38357D',NULL,'2026-03-24 17:00:30','2026-03-25 12:10:09'),(8,1003,'汇率数据采集','中国银行',X'7B2264617465223A2022323032362D30332D3235222C202272617465223A20372E383532332C202263757272656E6379223A2022455552227D',NULL,'2026-03-25 06:00:07','2026-03-25 12:10:09'),(9,1002,'基金净值采集','天天基金网',X'7B226E6176223A20312E3839322C2022636F6465223A2022303035383237222C202264617465223A2022323032362D30332D3233222C202266756E64223A2022E69893E696B9E8BEBEE8939DE7ADB9227D',NULL,'2026-03-23 18:30:22','2026-03-25 12:10:09'),(10,1004,'财经新闻采集','新浪财经',X'7B2264617465223A2022323032362D30332D3234222C20227469746C65223A202241E882A1E5B882E59CBAE99C87E88DA1E4B88AE8A18C227D',NULL,'2026-03-24 09:00:18','2026-03-25 12:10:09');

#
# Structure for table "rpa_process"
#

DROP TABLE IF EXISTS `rpa_process`;
CREATE TABLE `rpa_process` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '流程 ID',
  `process_name` varchar(100) NOT NULL COMMENT '流程名称',
  `process_code` varchar(50) NOT NULL COMMENT '流程编码',
  `description` varchar(500) DEFAULT NULL COMMENT '流程描述',
  `process_type` tinyint(1) DEFAULT '1' COMMENT '流程类型：1-数据采集，2-数据处理，3-自动化任务',
  `script_content` text COMMENT '流程脚本内容',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-停用，1-启用',
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_process_code` (`process_code`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COMMENT='RPA流程管理表';

#
# Structure for table "rpa_robot"
#

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

#
# Data for table "rpa_process"
#

INSERT INTO `rpa_process` VALUES (1,'股票数据采集流程','stock_collection','采集股票市场数据并存储到数据库',1,NULL,1,1,'2026-03-25 11:56:17','2026-03-25 11:56:17',0),(2,'基金净值处理流程','fund_processing','处理基金净值数据并生成报告',2,NULL,1,1,'2026-03-25 11:56:17','2026-03-25 11:56:17',0),(3,'日报自动生成流程','daily_report','自动生成每日数据采集报告',3,NULL,1,1,'2026-03-25 11:56:17','2026-03-25 11:56:17',0),(4,'汇率数据同步流程','exchange_sync','同步汇率数据到本地数据库',1,NULL,0,1,'2026-03-25 11:56:17','2026-03-25 11:56:17',0),(5,'财务报表处理流程','finance_report','处理财务报表数据',2,NULL,1,1,'2026-03-25 11:56:17','2026-03-25 11:56:17',0);

#
# Structure for table "spider_task"
#

DROP TABLE IF EXISTS `spider_task`;
CREATE TABLE `spider_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务 ID',
  `task_name` varchar(100) NOT NULL COMMENT '任务名称',
  `task_code` varchar(50) NOT NULL COMMENT '任务编码',
  `task_type` tinyint(1) DEFAULT '1' COMMENT '任务类型：1-定时任务，2-手动任务',
  `url` varchar(500) DEFAULT NULL COMMENT '目标 URL',
  `script_content` text COMMENT '爬虫脚本内容 (Groovy)',
  `cron_expression` varchar(50) DEFAULT NULL COMMENT 'Cron 表达式',
  `status` tinyint(1) DEFAULT '0' COMMENT '状态：0-停用，1-启用',
  `last_run_time` datetime DEFAULT NULL COMMENT '上次运行时间',
  `next_run_time` datetime DEFAULT NULL COMMENT '下次运行时间',
  `run_count` int(11) DEFAULT '0' COMMENT '运行次数',
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_code` (`task_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1007 DEFAULT CHARSET=utf8mb4 COMMENT='爬虫任务表';

#
# Data for table "spider_task"
#

INSERT INTO `spider_task` VALUES (1001,'股票行情采集','stock_quote',1,'https://api.example.com/stock','// 股票数据采集脚本\ndef crawl() {\n    def url = \"https://api.example.com/stock\"\n    def data = httpGet(url)\n    saveToDatabase(data)\n    return \"采集成功\"\n}','0 0 9,15 * * ?',1,'2026-03-25 09:00:00','2026-03-25 15:00:00',156,1,'2026-03-25 10:21:35','2026-03-25 10:21:35',0),(1002,'基金净值采集','fund_nav',1,'https://api.example.com/fund','// 基金净值采集脚本\ndef crawl() {\n    def url = \"https://api.example.com/fund\"\n    def data = httpGet(url)\n    processData(data)\n    return \"采集成功\"\n}','0 30 18 * * ?',1,'2026-03-24 18:30:00','2026-03-25 18:30:00',89,1,'2026-03-25 10:21:35','2026-03-25 10:21:35',0),(1003,'汇率数据采集','exchange_rate',1,'https://api.example.com/exchange','// 汇率数据采集脚本\ndef crawl() {\n    def url = \"https://api.example.com/exchange\"\n    def data = httpGet(url)\n    saveExchangeRate(data)\n    return \"采集成功\"\n}','0 0 */2 * * ?',1,'2026-03-25 08:00:00','2026-03-25 10:00:00',234,1,'2026-03-25 10:21:35','2026-03-25 10:21:35',0),(1004,'财经新闻采集','finance_news',1,'https://api.example.com/news','// 财经新闻采集脚本\ndef crawl() {\n    def url = \"https://api.example.com/news\"\n    def data = httpGet(url)\n    parseNews(data)\n    return \"采集成功\"\n}','0 0 */1 * * ?',1,'2026-03-25 09:00:00','2026-03-25 10:00:00',567,1,'2026-03-25 10:21:35','2026-03-25 10:21:35',0),(1005,'债券数据采集','bond_data',1,'https://api.example.com/bond','// 债券数据采集脚本\ndef crawl() {\n    def url = \"https://api.example.com/bond\"\n    def data = httpGet(url)\n    processBondData(data)\n    return \"采集成功\"\n}','0 0 17 * * ?',1,'2026-03-24 17:00:00','2026-03-25 17:00:00',78,1,'2026-03-25 10:21:35','2026-03-25 10:21:35',0),(1006,'银行利率采集','bank_rate',2,'https://api.example.com/bank-rate','// 银行利率采集脚本\ndef crawl() {\n    def url = \"https://api.example.com/bank-rate\"\n    def data = httpGet(url)\n    saveBankRate(data)\n    return \"采集成功\"\n}',NULL,1,'2026-03-23 14:30:00',NULL,12,1,'2026-03-25 10:21:35','2026-03-25 10:21:35',0);

#
# Structure for table "sys_department"
#

DROP TABLE IF EXISTS `sys_department`;
CREATE TABLE `sys_department` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '部门 ID',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父部门 ID',
  `dept_name` varchar(50) NOT NULL COMMENT '部门名称',
  `dept_code` varchar(50) DEFAULT NULL COMMENT '部门编码',
  `sort_order` int(4) DEFAULT '0' COMMENT '排序号',
  `leader` varchar(50) DEFAULT NULL COMMENT '部门负责人',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-禁用，1-正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识：0-未删除，1-已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COMMENT='系统部门表';

#
# Data for table "sys_department"
#

INSERT INTO `sys_department` VALUES (1,0,'总公司','headquarters',1,NULL,NULL,NULL,1,'2026-03-20 08:33:15','2026-03-20 08:33:15',0),(2,1,'技术研发部','tech_dept',1,'张三','13800138010','tech@rpa.com',1,'2026-03-25 10:21:35','2026-03-25 10:21:35',0),(3,1,'数据采集部','data_dept',2,'李四','13800138011','data@rpa.com',1,'2026-03-25 10:21:35','2026-03-25 10:21:35',0),(4,1,'运营管理部','operation_dept',3,'王五','13800138012','operation@rpa.com',1,'2026-03-25 10:21:35','2026-03-25 10:21:35',0),(5,1,'财务部','finance_dept',4,'赵六','13800138013','finance@rpa.com',1,'2026-03-25 10:21:35','2026-03-25 10:21:35',0);

#
# Structure for table "sys_menu"
#

DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '菜单 ID',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父菜单 ID',
  `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
  `menu_type` tinyint(1) DEFAULT '1' COMMENT '菜单类型：1-目录，2-菜单，3-按钮',
  `path` varchar(200) DEFAULT NULL COMMENT '路由地址',
  `component` varchar(200) DEFAULT NULL COMMENT '组件路径',
  `permission` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(50) DEFAULT NULL COMMENT '菜单图标',
  `sort_order` int(4) DEFAULT '0' COMMENT '排序号',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-禁用，1-正常',
  `visible` tinyint(1) DEFAULT '1' COMMENT '是否可见：0-隐藏，1-显示',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识：0-未删除，1-已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单表';

#
# Data for table "sys_menu"
#

INSERT INTO `sys_menu` VALUES (1,0,'系统管理',1,'/system','Layout',NULL,'setting',1,1,1,'2026-03-20 08:33:15','2026-03-20 08:33:15',0),(2,1,'用户管理',2,'/system/user','system/user/index','system:user:list','user',1,1,1,'2026-03-20 08:33:15','2026-03-20 08:33:15',0),(3,1,'角色管理',2,'/system/role','system/role/index','system:role:list','peoples',2,1,1,'2026-03-20 08:33:15','2026-03-20 08:33:15',0),(4,1,'菜单管理',2,'/system/menu','system/menu/index','system:menu:list','tree-table',3,1,1,'2026-03-20 08:33:15','2026-03-20 08:33:15',0),(5,1,'部门管理',2,'/system/dept','system/dept/index','system:dept:list','tree',4,1,1,'2026-03-20 08:33:15','2026-03-20 08:33:15',0),(6,0,'任务管理',1,'/task','Layout',NULL,'schedule',2,1,1,'2026-03-20 08:33:15','2026-03-20 08:33:15',0),(7,6,'任务配置',2,'/task/config','task/config/index','task:config:list','edit',1,1,1,'2026-03-20 08:33:15','2026-03-20 08:33:15',0),(8,6,'执行日志',2,'/task/log','task/log/index','task:log:list','log',2,1,1,'2026-03-20 08:33:15','2026-03-20 08:33:15',0),(9,0,'数据管理',1,'/data','Layout',NULL,'data',3,1,1,'2026-03-20 08:33:15','2026-03-20 08:33:15',0),(10,9,'采集结果',2,'/data/result','data/result/index','data:result:list','document',1,1,1,'2026-03-20 08:33:15','2026-03-20 08:33:15',0);

#
# Structure for table "sys_operation_log"
#

DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志 ID',
  `module` varchar(50) DEFAULT NULL COMMENT '操作模块',
  `business_type` varchar(50) DEFAULT NULL COMMENT '业务类型',
  `method` varchar(100) DEFAULT NULL COMMENT '请求方法',
  `operator` varchar(50) DEFAULT NULL COMMENT '操作人员',
  `dept_name` varchar(50) DEFAULT NULL COMMENT '部门名称',
  `operation_url` varchar(255) DEFAULT NULL COMMENT '请求 URL',
  `operation_ip` varchar(50) DEFAULT NULL COMMENT '主机地址',
  `operation_location` varchar(255) DEFAULT NULL COMMENT '操作地点',
  `request_params` varchar(2000) DEFAULT NULL COMMENT '请求参数',
  `operation_status` tinyint(1) DEFAULT '0' COMMENT '操作状态：0-失败，1-成功',
  `error_message` varchar(2000) DEFAULT NULL COMMENT '错误消息',
  `operation_time` datetime DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint(20) DEFAULT NULL COMMENT '消耗时间 (毫秒)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_operator` (`operator`),
  KEY `idx_operation_time` (`operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志表';

#
# Data for table "sys_operation_log"
#


#
# Structure for table "sys_role"
#

DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色 ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `description` varchar(255) DEFAULT NULL COMMENT '角色描述',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-禁用，1-正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB AUTO_INCREMENT=2037110918791073797 DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

#
# Data for table "sys_role"
#

INSERT INTO `sys_role` VALUES (1,'超级管理员','super_admin','拥有系统所有权限，可管理所有功能模块',1,'2026-03-20 08:33:15','2026-03-20 08:33:15',0),(2,'普通用户','common_user','普通用户权限，可查看和使用基本功能',1,'2026-03-20 08:33:15','2026-03-20 08:33:15',0),(3,'数据分析师','data_analyst','负责数据分析工作，可访问数据采集结果',1,'2026-03-21 09:00:00','2026-03-21 09:00:00',0),(4,'任务管理员','task_manager','负责任务配置和监控，可管理爬虫任务。',1,'2026-03-22 10:00:00','2026-03-26 18:12:32',0),(5,'审计员','auditor','负责系统审计，可查看操作日志',1,'2026-03-23 11:00:00','2026-03-26 18:12:24',0),(6,'dscsdc','dcsdc','asdac',1,'2026-03-26 20:54:42','2026-03-26 20:54:42',0);

#
# Structure for table "sys_role_menu"
#

DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色 ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单 ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`,`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

#
# Data for table "sys_role_menu"
#

INSERT INTO `sys_role_menu` VALUES (1,1,1,'2026-03-20 08:33:15'),(2,1,2,'2026-03-20 08:33:15'),(3,1,3,'2026-03-20 08:33:15'),(4,1,4,'2026-03-20 08:33:15'),(5,1,5,'2026-03-20 08:33:15'),(6,1,6,'2026-03-20 08:33:15'),(7,1,7,'2026-03-20 08:33:15'),(8,1,8,'2026-03-20 08:33:15'),(9,1,9,'2026-03-20 08:33:15'),(10,1,10,'2026-03-20 08:33:15'),(11,2,6,'2026-03-20 08:33:15'),(12,2,7,'2026-03-20 08:33:15'),(13,2,8,'2026-03-20 08:33:15'),(14,2,9,'2026-03-20 08:33:15'),(15,2,10,'2026-03-20 08:33:15'),(16,3,6,'2026-03-21 09:00:00'),(17,3,8,'2026-03-21 09:00:00'),(18,3,9,'2026-03-21 09:00:00'),(19,3,10,'2026-03-21 09:00:00'),(20,4,6,'2026-03-22 10:00:00'),(21,4,7,'2026-03-22 10:00:00'),(22,4,8,'2026-03-22 10:00:00'),(23,5,8,'2026-03-23 11:00:00');

#
# Structure for table "sys_user"
#

DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户 ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像 URL',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-禁用，1-正常',
  `dept_id` bigint(20) DEFAULT NULL COMMENT '部门 ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

#
# Data for table "sys_user"
#

INSERT INTO `sys_user` VALUES (1,'admin','$2a$10$VtDPGV6XfFZ.a.eqSoa3oO9UNqMhsMGJTpaMI6cXYBtujS4msTELO','系统管理员','admin@rpa.com','13800138000',NULL,1,1,'2026-03-20 08:33:15','2026-03-26 11:37:15',0),(2,'operator','$2a$10$VtDPGV6XfFZ.a.eqSoa3oO9UNqMhsMGJTpaMI6cXYBtujS4msTELO','操作员张伟','operator@rpa.com','13900139000',NULL,1,2,'2026-03-21 09:00:00','2026-03-26 11:19:53',0),(3,'finance','$2a$10$VtDPGV6XfFZ.a.eqSoa3oO9UNqMhsMGJTpaMI6cXYBtujS4msTELO','财务管理员李娜','finance@rpa.com','13800138002',NULL,1,5,'2026-03-22 10:00:00','2026-03-26 11:19:53',0),(4,'tech','$2a$10$VtDPGV6XfFZ.a.eqSoa3oO9UNqMhsMGJTpaMI6cXYBtujS4msTELO','技术支持王强','tech@rpa.com','13800138003',NULL,1,2,'2026-03-23 11:00:00','2026-03-26 11:19:53',0),(5,'guest','$2a$10$VtDPGV6XfFZ.a.eqSoa3oO9UNqMhsMGJTpaMI6cXYBtujS4msTELO','访客用户赵敏','guest@rpa.com','13800138004',NULL,1,3,'2026-03-24 12:00:00','2026-03-26 11:19:53',0),(6,'analyst','$2a$10$VtDPGV6XfFZ.a.eqSoa3oO9UNqMhsMGJTpaMI6cXYBtujS4msTELO','数据分析师孙丽','analyst@rpa.com','13800138005',NULL,1,3,'2026-03-25 13:00:00','2026-03-26 11:19:53',0),(7,'taskmgr','$2a$10$VtDPGV6XfFZ.a.eqSoa3oO9UNqMhsMGJTpaMI6cXYBtujS4msTELO','任务管理员周杰','taskmgr@rpa.com','13800138006',NULL,1,2,'2026-03-25 14:00:00','2026-03-26 11:19:53',0),(8,'auditor','$2a$10$VtDPGV6XfFZ.a.eqSoa3oO9UNqMhsMGJTpaMI6cXYBtujS4msTELO','审计员吴芳','auditor@rpa.com','13800138007',NULL,1,4,'2026-03-25 15:00:00','2026-03-26 11:19:53',0),(11,'yqewduqwyfegd','$2a$10$vttM4vwdghNXtgN2GVI3BuZQQdg0eAVrUDk0HhBEXiTvEd6G.f7wa','ewjhdfwqoef',NULL,NULL,NULL,1,NULL,'2026-03-26 21:34:17','2026-03-26 21:34:17',0);

#
# Structure for table "sys_user_role"
#

DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户 ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色 ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

#
# Data for table "sys_user_role"
#

INSERT INTO `sys_user_role` VALUES (1,1,1,'2026-03-20 08:33:15'),(2,2,2,'2026-03-21 09:00:00'),(3,3,2,'2026-03-22 10:00:00'),(4,4,2,'2026-03-23 11:00:00'),(5,5,2,'2026-03-24 12:00:00'),(6,6,3,'2026-03-25 13:00:00'),(7,7,4,'2026-03-25 14:00:00'),(8,8,5,'2026-03-25 15:00:00'),(9,11,5,'2026-03-26 21:34:17');

#
# Structure for table "task_execution_log"
#

DROP TABLE IF EXISTS `task_execution_log`;
CREATE TABLE `task_execution_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志 ID',
  `task_id` bigint(20) NOT NULL COMMENT '任务 ID',
  `task_name` varchar(100) DEFAULT NULL COMMENT '任务名称',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `duration` bigint(20) DEFAULT NULL COMMENT '执行时长 (毫秒)',
  `status` tinyint(1) DEFAULT '0' COMMENT '执行状态：0-失败，1-成功',
  `error_message` text COMMENT '错误信息',
  `log_detail` text COMMENT '详细日志',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务执行日志表';

#
# Data for table "task_execution_log"
#

