package com.example.rpa.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 机器人实体
 */
@Data
@TableName("rpa_robot")
public class RpaRobot implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String robotName;

    private String robotCode;

    private Integer robotType;

    private Integer status;

    private String hostName;

    private String hostIp;

    private Integer port;

    private String clientVersion;

    private LocalDateTime lastHeartbeatTime;

    private LocalDateTime lastOnlineTime;

    private Long currentTaskId;

    private Long currentProcessId;

    private String description;

    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
