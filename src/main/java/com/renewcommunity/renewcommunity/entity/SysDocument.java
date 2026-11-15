package com.renewcommunity.renewcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资料台账。
 */
@Data
@TableName("sys_document")
public class SysDocument {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String category;

    private String title;

    private Long fileId;

    /** 状态：0 正常，1 已归档 */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
