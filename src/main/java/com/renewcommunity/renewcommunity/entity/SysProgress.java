package com.renewcommunity.renewcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工程进度记录。
 */
@Data
@TableName("sys_progress")
public class SysProgress {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String stage;

    private String title;

    private String content;

    private Integer progress;

    private LocalDate reportDate;

    private Long creatorId;

    private String creatorName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
