package com.example.rpa.vo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserListItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String realName;

    private String email;

    private String phone;

    private Integer status;

    private String statusDesc;

    private LocalDateTime createTime;

    private List<RoleInfo> roles;

    @Data
    public static class RoleInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private Long id;
        private String roleName;
        private String roleCode;
    }
}
