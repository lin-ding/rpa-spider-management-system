package com.example.rpa.vo;

import lombok.Data;
import lombok.Getter;

import java.util.List;

/**
 * 菜单树形结构 VO
 */
@Data
public class MenuTreeVO {

    /**
     * 菜单 ID
     */
    private Long id;

    /**
     * 父菜单 ID
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    private String label;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 权限标识
     */
    private String permission;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 菜单类型：1-目录，2-菜单，3-按钮
     */
    private Integer type;

    /**
     * 排序号
     */
    @Getter
    private Integer sortOrder;


    /**
     * 子菜单列表
     */
    private List<MenuTreeVO> children;
}
