package com.renewcommunity.renewcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 投诉建议。
 */
@Data
@TableName("sys_complaint")
public class SysComplaint {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private String community;

    private String type;

    /** 状态：0 待处理，1 处理中，2 已办结 */
    private Integer status;

    private Long projectId;

    /** 提交人ID（C 端用户） */
    private Long submitterId;

    private String contactName;

    private String contactMobile;

    private String reply;

    private LocalDateTime handledAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
