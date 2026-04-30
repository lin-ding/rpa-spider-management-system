package com.example.rpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("data_collection_result")
public class DataCollectionResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private String taskCode;

    private String taskName;

    private Long executionId;

    private String executionNo;

    private Long processId;

    private String processCode;

    private String processName;

    private Long robotId;

    private String robotCode;

    private String robotName;

    private String dataSource;

    private String dataType;

    private String businessKey;

    private String contentHash;

    private String rawContent;

    private String dataContent;

    private String processedContent;

    private String dataStatus;

    private String errorMessage;

    private String filePath;

    private LocalDateTime sourceTime;

    private LocalDateTime collectionTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Integer deleted;

    @TableField(exist = false)
    private LocalDateTime collectionTimeFrom;

    @TableField(exist = false)
    private LocalDateTime collectionTimeTo;

    @TableField(exist = false)
    private Boolean usableOnly;
}
