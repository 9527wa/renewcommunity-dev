package com.renewcommunity.renewcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户。
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String mobile;

    private String email;

    /** 绑定小区ID（C 端居民） */
    private Long communityId;

    /** 状态：1 启用，0 停用 */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
