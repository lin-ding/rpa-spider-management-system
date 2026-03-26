package com.example.rpa.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ResourceListItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long parentId;

    private String resourceCode;

    private String resourceName;

    private String resourceType;

    private String url;

    private String icon;

    private Integer sort;

    private Integer status;

    private String parentResourceName;

    private LocalDateTime createTime;

    @Data
    public static class ParentInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long id;
        private String resourceName;
    }
}
