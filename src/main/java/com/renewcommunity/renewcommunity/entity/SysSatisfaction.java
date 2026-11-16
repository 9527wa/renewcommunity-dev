package com.renewcommunity.renewcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 满意度评价。
 */
@Data
@TableName("sys_satisfaction")
public class SysSatisfaction {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long complaintId;

    private Long userId;

    private String userName;

    private Integer score;

    private String comment;

    private LocalDateTime createdAt;
}
