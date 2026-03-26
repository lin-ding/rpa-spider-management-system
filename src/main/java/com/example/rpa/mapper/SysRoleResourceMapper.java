package com.example.rpa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.rpa.entity.SysRoleResource;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleResourceMapper extends BaseMapper<SysRoleResource> {

    @Select("SELECT IFNULL(MAX(id), 0) FROM sys_role_resource")
    Long selectMaxId();

    @Select("SELECT resource_id FROM sys_role_resource WHERE role_id = #{roleId}")
    List<Long> selectResourceIdsByRoleId(@Param("roleId") Long roleId);

    @Delete("DELETE FROM sys_role_resource WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);
}
