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

    private String taskName;

    private String dataSource;

    private String dataContent;

    private String filePath;

    private LocalDateTime collectionTime;

    private String category;

    private String dataStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
