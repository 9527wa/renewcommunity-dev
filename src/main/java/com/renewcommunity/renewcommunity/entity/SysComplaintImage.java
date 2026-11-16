package com.renewcommunity.renewcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 投诉图片。
 */
@Data
@TableName("sys_complaint_image")
public class SysComplaintImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long complaintId;

    private Long fileId;

    private Integer sort;
}
