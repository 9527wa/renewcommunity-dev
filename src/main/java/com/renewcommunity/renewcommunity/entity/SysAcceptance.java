package com.renewcommunity.renewcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目验收记录。
 */
@Data
@TableName("sys_acceptance")
public class SysAcceptance {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String title;

    /** 结果：0 待验收，1 通过，2 不通过 */
    private Integer result;

    private LocalDate acceptDate;

    private String conclusion;

    private Long creatorId;

    private String creatorName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
