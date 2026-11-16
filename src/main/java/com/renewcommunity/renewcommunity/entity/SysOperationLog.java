package com.renewcommunity.renewcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志。
 */
@Data
@TableName("sys_operation_log")
public class SysOperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String userName;

    private String method;

    private String path;

    private String ip;

    private Long durationMs;

    /** 是否成功：1 成功，0 失败 */
    private Integer success;

    private String errorMsg;

    private LocalDateTime createdAt;
}
