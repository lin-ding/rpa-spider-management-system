package com.example.rpa.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;

    private String realName;

    private Long roleId;

    private Integer status;

    private Integer current = 1;

    private Integer size = 10;
}
