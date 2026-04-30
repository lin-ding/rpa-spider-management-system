package com.example.rpa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.rpa.entity.SysResource;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysResourceMapper extends BaseMapper<SysResource> {

    @Select("SELECT IFNULL(MAX(id), 0) FROM sys_resource")
    Long selectMaxId();

    @Delete("DELETE FROM sys_resource WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);

    @Select("SELECT * FROM sys_resource WHERE resource_type = 'menu' AND deleted = 0 ORDER BY sort ASC")
    List<SysResource> selectParentResources();

    @Select("""
            SELECT COUNT(1)
            FROM sys_resource r
            INNER JOIN sys_role_resource rr ON r.id = rr.resource_id
            INNER JOIN sys_user_role ur ON rr.role_id = ur.role_id
            INNER JOIN sys_role role ON ur.role_id = role.id
            WHERE ur.user_id = #{userId}
              AND r.resource_code = #{resourceCode}
              AND r.status = 1
              AND r.deleted = 0
              AND role.status = 1
              AND role.deleted = 0
            """)
    Long countPermissionByUserIdAndCode(@Param("userId") Long userId,
                                        @Param("resourceCode") String resourceCode);

    @Select("""
            SELECT DISTINCT r.resource_code
            FROM sys_resource r
            INNER JOIN sys_role_resource rr ON r.id = rr.resource_id
            INNER JOIN sys_user_role ur ON rr.role_id = ur.role_id
            INNER JOIN sys_role role ON ur.role_id = role.id
            WHERE ur.user_id = #{userId}
              AND r.status = 1
              AND r.deleted = 0
              AND role.status = 1
              AND role.deleted = 0
            ORDER BY r.resource_code
            """)
    List<String> selectResourceCodesByUserId(@Param("userId") Long userId);
}
