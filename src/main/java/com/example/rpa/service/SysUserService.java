package com.example.rpa.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.dto.AddUserRequest;
import com.example.rpa.dto.ResetPasswordRequest;
import com.example.rpa.dto.UpdateUserRequest;
import com.example.rpa.dto.UserQueryRequest;
import com.example.rpa.entity.SysUser;
import com.example.rpa.vo.UserDetailVO;
import com.example.rpa.vo.UserListItemVO;

import java.util.List;

public interface SysUserService {

    Page<SysUser> getUserPage(Integer current, Integer size, SysUser user);

    Page<UserListItemVO> getUserPageWithRoles(UserQueryRequest request);

    SysUser getUserById(Long id);

    UserDetailVO getUserDetailById(Long id);

    void addUser(AddUserRequest request);

    void addUser(SysUser user);

    void updateUser(SysUser user);

    void updateUser(UpdateUserRequest request);

    void deleteUser(Long id);

    void resetPassword(Long userId, String newPassword);

    void resetPassword(ResetPasswordRequest request);

    boolean checkUsernameUnique(String username);

    boolean checkUsernameUnique(SysUser user);

    void toggleUserStatus(Long userId);

    List<SysUser> searchUsersByUsername(String keyword);
}
