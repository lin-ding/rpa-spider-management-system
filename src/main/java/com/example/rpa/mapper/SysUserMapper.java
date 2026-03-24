package com.example.rpa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.rpa.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统用户 Mapper 接口
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户
     */
    SysUser selectByUsername(@Param("username") String username);

    SysUser selectByUsernameIncludeDeleted(@Param("username") String username);

    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的权限列表
     */
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);
}
