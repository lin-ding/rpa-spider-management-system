package com.example.rpa.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.entity.SysUser;

import java.util.List;

/**
 * 系统用户 Service
 */
public interface SysUserService {

    /**
     * 分页查询用户列表
     */
    Page<SysUser> getUserPage(Integer current, Integer size, SysUser user);

    /**
     * 根据 ID 查询用户
     */
    SysUser getUserById(Long id);

    /**
     * 新增用户
     */
    void addUser(SysUser user);

    /**
     * 修改用户
     */
    void updateUser(SysUser user);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 重置用户密码
     */
    void resetPassword(Long userId, String newPassword);

    /**
     * 检查用户名是否唯一
     */
    boolean checkUsernameUnique(SysUser user);

    /**
     * 切换用户状态（启用/禁用）
     */
    void toggleUserStatus(Long userId);

    /**
     * 根据用户名关键字搜索用户
     */
    List<SysUser> searchUsersByUsername(String keyword);
}
