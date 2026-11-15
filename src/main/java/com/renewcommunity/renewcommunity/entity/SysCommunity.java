package com.renewcommunity.renewcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小区信息。
 */
@Data
@TableName("sys_community")
public class SysCommunity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String address;

    private Integer builtYear;

    private Integer buildingCount;

    private Integer householdCount;

    private String manager;

    private String managerMobile;

    /** 状态：1 启用，0 停用 */
    private Integer status;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
