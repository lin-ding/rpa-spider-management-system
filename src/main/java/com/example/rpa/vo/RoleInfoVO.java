package com.example.rpa.vo;

import lombok.Data;

/**
 * 角色信息 VO
 */
@Data
public class RoleInfoVO {

    /**
     * 角色 ID
     */
    private Long id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色描述
     */
    private String description;
}
