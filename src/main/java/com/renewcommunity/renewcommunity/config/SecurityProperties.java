package com.renewcommunity.renewcommunity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 引导管理员账号配置。
 *
 * <p>当前项目尚未接入用户表，登录校验使用该配置；接入数据库后应删除本配置，
 * 改为查询用户表并使用 BCrypt 校验密码。</p>
 *
 * @param username 用户名
 * @param password 密码
 * @param nickname 昵称
 * @param roles    角色列表，需与前端 {@code VITE_STATIC_SUPER_ROLE} 对齐
 */
@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(String username, String password, String nickname, List<String> roles) {

    public SecurityProperties {
        roles = roles == null ? List.of() : List.copyOf(roles);
    }
}
