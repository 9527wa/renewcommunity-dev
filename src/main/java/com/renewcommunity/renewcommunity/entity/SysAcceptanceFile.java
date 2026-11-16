package com.renewcommunity.renewcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 项目验收附件。
 */
@Data
@TableName("sys_acceptance_file")
public class SysAcceptanceFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long acceptanceId;

    private Long fileId;

    private Integer sort;
}
