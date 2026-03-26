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

    @Select("SELECT * FROM sys_resource WHERE parent_id IS NULL AND deleted = 0 ORDER BY sort ASC")
    List<SysResource> selectParentResources();
}
