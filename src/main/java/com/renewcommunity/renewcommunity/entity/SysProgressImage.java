package com.renewcommunity.renewcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 工程进度现场图片。
 */
@Data
@TableName("sys_progress_image")
public class SysProgressImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long progressId;

    private Long fileId;

    private Integer sort;
}
