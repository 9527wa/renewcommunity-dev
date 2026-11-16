package com.renewcommunity.renewcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 居民小区绑定记录。
 */
@Data
@TableName("sys_user_community_log")
public class SysUserCommunityLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long communityId;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime createdAt;
}
