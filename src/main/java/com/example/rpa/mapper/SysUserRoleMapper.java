package com.example.rpa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.rpa.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);

    @Select("SELECT IFNULL(MAX(id), 0) FROM sys_user_role")
    Long selectMaxId();
}
