-- 添加测试用户脚本
-- 所有用户密码均为：123456 (BCrypt 加密)
-- 使用 Spring Boot BCryptPasswordEncoder 生成的正确 Hash 值
-- BCrypt Hash: $2a$10$dsRxTcjM11lwiendJnD/iOOQM61nXRRqNlSJHVCcTPLTBoGDR.PnO

USE rpa_system;

-- 首先检查并删除可能存在的测试用户（避免重复插入）
DELETE FROM sys_user_role WHERE user_id IN (2, 3, 4, 5);
DELETE FROM sys_user WHERE id IN (2, 3, 4, 5);

-- 添加测试用户 1: 普通操作员
-- 密码: 123456
-- BCrypt Hash: $2a$10$dsRxTcjM11lwiendJnD/iOOQM61nXRRqNlSJHVCcTPLTBoGDR.PnO
INSERT INTO sys_user (id, username, password, real_name, email, phone, avatar, status, dept_id, create_time, update_time, deleted) VALUES 
(2, 'operator', '$2a$10$dsRxTcjM11lwiendJnD/iOOQM61nXRRqNlSJHVCcTPLTBoGDR.PnO', '操作员', 'operator@rpa.com', '13800138001', NULL, 1, 1, NOW(), NOW(), 0);

-- 添加测试用户 2: 财务人员
-- 密码: 123456
INSERT INTO sys_user (id, username, password, real_name, email, phone, avatar, status, dept_id, create_time, update_time, deleted) VALUES 
(3, 'finance', '$2a$10$dsRxTcjM11lwiendJnD/iOOQM61nXRRqNlSJHVCcTPLTBoGDR.PnO', '财务管理员', 'finance@rpa.com', '13800138002', NULL, 1, 1, NOW(), NOW(), 0);

-- 添加测试用户 3: 技术人员
-- 密码: 123456
INSERT INTO sys_user (id, username, password, real_name, email, phone, avatar, status, dept_id, create_time, update_time, deleted) VALUES 
(4, 'tech', '$2a$10$dsRxTcjM11lwiendJnD/iOOQM61nXRRqNlSJHVCcTPLTBoGDR.PnO', '技术支持', 'tech@rpa.com', '13800138003', NULL, 1, 1, NOW(), NOW(), 0);

-- 添加测试用户 4: 访客用户
-- 密码: 123456
INSERT INTO sys_user (id, username, password, real_name, email, phone, avatar, status, dept_id, create_time, update_time, deleted) VALUES 
(5, 'guest', '$2a$10$dsRxTcjM11lwiendJnD/iOOQM61nXRRqNlSJHVCcTPLTBoGDR.PnO', '访客', 'guest@rpa.com', '13800138004', NULL, 1, 1, NOW(), NOW(), 0);

-- 为新用户分配普通用户角色 (role_id=2 对应 'common_user' 角色)
INSERT INTO sys_user_role (id, user_id, role_id, create_time) VALUES 
(2, 2, 2, NOW()),
(3, 3, 2, NOW()),
(4, 4, 2, NOW()),
(5, 5, 2, NOW());

-- 查询验证插入结果
SELECT 
    u.id,
    u.username,
    u.real_name,
    u.email,
    u.phone,
    u.status,
    r.role_name,
    r.role_code
FROM sys_user u
LEFT JOIN sys_user_role ur ON u.id = ur.user_id
LEFT JOIN sys_role r ON ur.role_id = r.id
WHERE u.id IN (2, 3, 4, 5)
ORDER BY u.id;
