package com.renewcommunity.renewcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 改造项目。
 */
@Data
@TableName("sys_project")
public class SysProject {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String community;

    private String category;

    /** 状态：0 规划中，1 施工中，2 已完成 */
    private Integer status;

    /** 进度百分比 0-100 */
    private Integer progress;

    /** 预算金额（元） */
    private BigDecimal budget;

    private LocalDate startDate;

    private LocalDate endDate;

    private String manager;

    /** 施工单位 */
    private String constructionUnit;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
