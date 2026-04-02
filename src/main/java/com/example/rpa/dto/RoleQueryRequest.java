package com.example.rpa.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class RoleQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String roleName;

    private String roleCode;

    private Integer status;

    private Integer current = 1;

    private Integer size = 10;
}
