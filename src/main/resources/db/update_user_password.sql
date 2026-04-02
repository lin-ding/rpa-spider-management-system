-- 更新测试用户密码哈希值
-- 将密码更新为新的BCrypt哈希值

USE rpa_system;

-- 更新所有测试用户的密码哈希值
-- 新的BCrypt Hash: $2a$10$dsRxTcjM11lwiendJnD/iOOQM61nXRRqNlSJHVCcTPLTBoGDR.PnO
UPDATE sys_user 
SET password = '$2a$10$dsRxTcjM11lwiendJnD/iOOQM61nXRRqNlSJHVCcTPLTBoGDR.PnO',
    update_time = NOW()
WHERE id IN (2, 3, 4, 5);

-- 验证更新结果
SELECT 
    id,
    username,
    real_name,
    password,
    update_time
FROM sys_user 
WHERE id IN (2, 3, 4, 5)
ORDER BY id;
