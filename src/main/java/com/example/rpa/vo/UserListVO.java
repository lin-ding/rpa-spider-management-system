package com.example.rpa.vo;

import lombok.Data;

import java.util.List;

/**
 * 用户列表响应 VO
 */
@Data
public class UserListVO<T> {

    /**
     * 用户列表
     */
    private List<T> list;

    /**
     * 总数
     */
    private Long total;

    /**
     * 当前页
     */
    private Long current;

    /**
     * 每页大小
     */
    private Long size;

    public UserListVO() {
    }

    public UserListVO(List<T> list, Long total, Long current, Long size) {
        this.list = list;
        this.total = total;
        this.current = current;
        this.size = size;
    }
}
