package com.renewcommunity.renewcommunity.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 小程序账号注册请求。
 */
public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度需在 3-50 位之间")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 32, message = "密码长度需在 6-32 位之间")
        String password,

        @Size(max = 50, message = "昵称长度不能超过 50")
        String nickname,

        @Size(max = 20, message = "手机号长度不能超过 20")
        String mobile
) {
}
