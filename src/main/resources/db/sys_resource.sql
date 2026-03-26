-- 资源管理表
CREATE TABLE IF NOT EXISTS `sys_resource` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `parent_id` BIGINT(20) DEFAULT NULL COMMENT '父资源ID',
  `resource_code` VARCHAR(100) NOT NULL COMMENT '资源编码',
  `resource_name` VARCHAR(100) NOT NULL COMMENT '资源名称',
  `resource_type` VARCHAR(20) NOT NULL DEFAULT 'menu' COMMENT '资源类型：menu-菜单，button-按钮，api-接口',
  `url` VARCHAR(200) DEFAULT NULL COMMENT '路径/URL',
  `icon` VARCHAR(100) DEFAULT NULL COMMENT '图标',
  `sort` INT(11) DEFAULT 0 COMMENT '排序',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '删除标识：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resource_code` (`resource_code`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源管理表';

-- 初始化资源数据
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
