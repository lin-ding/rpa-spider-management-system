package com.example.rpa.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class QueryResourceRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer current = 1;

    private Integer size = 10;

    private String resourceName;

    private String resourceCode;

    private Integer status;
}
