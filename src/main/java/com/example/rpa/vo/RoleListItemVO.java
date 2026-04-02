package com.example.rpa.vo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class RoleListItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String roleCode;

    private String roleName;

    private String description;

    private Integer status;

    private String statusDesc;

    private LocalDateTime createTime;

    private Long userCount;
}
