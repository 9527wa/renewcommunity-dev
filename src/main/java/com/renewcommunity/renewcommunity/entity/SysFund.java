package com.renewcommunity.renewcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资金台账。
 */
@Data
@TableName("sys_fund")
public class SysFund {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String name;

    /** 类型：1 拨款，2 支出 */
    private Integer type;

    /** 金额（元） */
    private BigDecimal amount;

    private LocalDate occurredAt;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
