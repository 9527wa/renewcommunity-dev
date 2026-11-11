package com.renewcommunity.renewcommunity.model.vo;

import java.util.List;

/**
 * 当前登录用户信息，字段需与前端 {@code Api.Auth.UserInfo} 保持一致。
 *
 * @param userId   用户标识
 * @param userName 用户名称
 * @param roles    角色列表
 * @param buttons  按钮权限列表
 */
public record UserInfoVO(String userId, String userName, List<String> roles, List<String> buttons) {
}
