-- RPA 运营管理系统数据库初始化脚本
-- Database: rpa_system

CREATE DATABASE IF NOT EXISTS rpa_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE rpa_system;

-- ----------------------------
-- 1. 用户表
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT(20) NOT NULL COMMENT '用户 ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像 URL',
    status TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    dept_id BIGINT(20) DEFAULT NULL COMMENT '部门 ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ----------------------------
-- 2. 角色表
-- ----------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT(20) NOT NULL COMMENT '角色 ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    description VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    status TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- ----------------------------
-- 3. 菜单表
-- ----------------------------
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id BIGINT(20) NOT NULL COMMENT '菜单 ID',
    parent_id BIGINT(20) DEFAULT 0 COMMENT '父菜单 ID',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    menu_type TINYINT(1) DEFAULT 1 COMMENT '菜单类型：1-目录，2-菜单，3-按钮',
    path VARCHAR(200) DEFAULT NULL COMMENT '路由地址',
    component VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
    permission VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
    icon VARCHAR(50) DEFAULT NULL COMMENT '菜单图标',
    sort_order INT(4) DEFAULT 0 COMMENT '排序号',
    status TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    visible TINYINT(1) DEFAULT 1 COMMENT '是否可见：0-隐藏，1-显示',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单表';

-- ----------------------------
-- 4. 部门表
-- ----------------------------
DROP TABLE IF EXISTS sys_department;
CREATE TABLE sys_department (
    id BIGINT(20) NOT NULL COMMENT '部门 ID',
    parent_id BIGINT(20) DEFAULT 0 COMMENT '父部门 ID',
    dept_name VARCHAR(50) NOT NULL COMMENT '部门名称',
    dept_code VARCHAR(50) DEFAULT NULL COMMENT '部门编码',
    sort_order INT(4) DEFAULT 0 COMMENT '排序号',
    leader VARCHAR(50) DEFAULT NULL COMMENT '部门负责人',
    phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    status TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统部门表';

-- ----------------------------
-- 5. 用户角色关联表
-- ----------------------------
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id BIGINT(20) NOT NULL COMMENT '主键 ID',
    user_id BIGINT(20) NOT NULL COMMENT '用户 ID',
    role_id BIGINT(20) NOT NULL COMMENT '角色 ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ----------------------------
-- 6. 角色菜单关联表
-- ----------------------------
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id BIGINT(20) NOT NULL COMMENT '主键 ID',
    role_id BIGINT(20) NOT NULL COMMENT '角色 ID',
    menu_id BIGINT(20) NOT NULL COMMENT '菜单 ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ----------------------------
-- 7. 爬虫任务表
-- ----------------------------
DROP TABLE IF EXISTS spider_task;
CREATE TABLE spider_task (
    id BIGINT(20) NOT NULL COMMENT '任务 ID',
    task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    task_code VARCHAR(50) NOT NULL COMMENT '任务编码',
    task_type TINYINT(1) DEFAULT 1 COMMENT '任务类型：1-定时任务，2-手动任务',
    url VARCHAR(500) DEFAULT NULL COMMENT '目标 URL',
    script_content TEXT COMMENT '爬虫脚本内容 (Groovy)',
    cron_expression VARCHAR(50) DEFAULT NULL COMMENT 'Cron 表达式',
    status TINYINT(1) DEFAULT 0 COMMENT '状态：0-停用，1-启用',
    last_run_time DATETIME DEFAULT NULL COMMENT '上次运行时间',
    next_run_time DATETIME DEFAULT NULL COMMENT '下次运行时间',
    run_count INT(11) DEFAULT 0 COMMENT '运行次数',
    create_by BIGINT(20) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_task_code (task_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='爬虫任务表';

-- ----------------------------
-- 8. 任务执行日志表
-- ----------------------------
DROP TABLE IF EXISTS task_execution_log;
CREATE TABLE task_execution_log (
    id BIGINT(20) NOT NULL COMMENT '日志 ID',
    task_id BIGINT(20) NOT NULL COMMENT '任务 ID',
    task_name VARCHAR(100) DEFAULT NULL COMMENT '任务名称',
    start_time DATETIME DEFAULT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    duration BIGINT(20) DEFAULT NULL COMMENT '执行时长 (毫秒)',
    status TINYINT(1) DEFAULT 0 COMMENT '执行状态：0-失败，1-成功',
    error_message TEXT COMMENT '错误信息',
    log_detail TEXT COMMENT '详细日志',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_task_id (task_id),
    KEY idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务执行日志表';

-- ----------------------------
-- 9. 数据采集结果表
-- ----------------------------
DROP TABLE IF EXISTS data_collection_result;
CREATE TABLE data_collection_result (
    id BIGINT(20) NOT NULL COMMENT '数据 ID',
    task_id BIGINT(20) NOT NULL COMMENT '任务 ID',
    task_name VARCHAR(100) DEFAULT NULL COMMENT '任务名称',
    data_source VARCHAR(200) DEFAULT NULL COMMENT '数据来源',
    data_content JSON COMMENT '采集的数据内容 (JSON 格式)',
    file_path VARCHAR(500) DEFAULT NULL COMMENT '附件路径',
    collection_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '采集时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_task_id (task_id),
    KEY idx_collection_time (collection_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据采集结果表';

-- ----------------------------
-- 10. 操作日志表
-- ----------------------------
DROP TABLE IF EXISTS sys_operation_log;
CREATE TABLE sys_operation_log (
    id BIGINT(20) NOT NULL COMMENT '日志 ID',
    module VARCHAR(50) DEFAULT NULL COMMENT '操作模块',
    business_type VARCHAR(50) DEFAULT NULL COMMENT '业务类型',
    method VARCHAR(100) DEFAULT NULL COMMENT '请求方法',
    operator VARCHAR(50) DEFAULT NULL COMMENT '操作人员',
    dept_name VARCHAR(50) DEFAULT NULL COMMENT '部门名称',
    operation_url VARCHAR(255) DEFAULT NULL COMMENT '请求 URL',
    operation_ip VARCHAR(50) DEFAULT NULL COMMENT '主机地址',
    operation_location VARCHAR(255) DEFAULT NULL COMMENT '操作地点',
    request_params VARCHAR(2000) DEFAULT NULL COMMENT '请求参数',
    operation_status TINYINT(1) DEFAULT 0 COMMENT '操作状态：0-失败，1-成功',
    error_message VARCHAR(2000) DEFAULT NULL COMMENT '错误消息',
    operation_time DATETIME DEFAULT NULL COMMENT '操作时间',
    cost_time BIGINT(20) DEFAULT NULL COMMENT '消耗时间 (毫秒)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_operator (operator),
    KEY idx_operation_time (operation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志表';

-- ----------------------------
-- 初始化数据
-- ----------------------------

-- 默认管理员账号 (密码：admin123)
INSERT INTO sys_user (id, username, password, real_name, email, phone, status) VALUES 
(1, 'admin', '$2a$10$vaITHwKEUroecnHBYCUh2.Vb7V3DS2yJV9Vd/s5EjnP9cP6qCjeti', '超级管理员', 'admin@rpa.com', '13800138000', 1);

-- 默认角色
INSERT INTO sys_role (id, role_name, role_code, description, status) VALUES 
(1, '超级管理员', 'super_admin', '拥有系统所有权限', 1),
(2, '普通用户', 'common_user', '普通用户权限', 1);

-- 默认菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission, icon, sort_order, status, visible) VALUES 
(1, 0, '系统管理', 1, '/system', 'Layout', NULL, 'setting', 1, 1, 1),
(2, 1, '用户管理', 2, '/system/user', 'system/user/index', 'system:user:list', 'user', 1, 1, 1),
(3, 1, '角色管理', 2, '/system/role', 'system/role/index', 'system:role:list', 'peoples', 2, 1, 1),
(4, 1, '菜单管理', 2, '/system/menu', 'system/menu/index', 'system:menu:list', 'tree-table', 3, 1, 1),
(5, 1, '部门管理', 2, '/system/dept', 'system/dept/index', 'system:dept:list', 'tree', 4, 1, 1),
(6, 0, '任务管理', 1, '/task', 'Layout', NULL, 'schedule', 2, 1, 1),
(7, 6, '任务配置', 2, '/task/config', 'task/config/index', 'task:config:list', 'edit', 1, 1, 1),
(8, 6, '执行日志', 2, '/task/log', 'task/log/index', 'task:log:list', 'log', 2, 1, 1),
(9, 0, '数据管理', 1, '/data', 'Layout', NULL, 'data', 3, 1, 1),
(10, 9, '采集结果', 2, '/data/result', 'data/result/index', 'data:result:list', 'document', 1, 1, 1);

-- 分配角色给用户
INSERT INTO sys_user_role (id, user_id, role_id) VALUES 
(1, 1, 1);

-- 分配菜单给角色
INSERT INTO sys_role_menu (id, role_id, menu_id) VALUES 
(1, 1, 1), (2, 1, 2), (3, 1, 3), (4, 1, 4), (5, 1, 5),
(6, 1, 6), (7, 1, 7), (8, 1, 8),
(9, 1, 9), (10, 1, 10);

-- 默认部门
INSERT INTO sys_department (id, parent_id, dept_name, dept_code, sort_order, status) VALUES 
(1, 0, '总公司', 'headquarters', 1, 1);
