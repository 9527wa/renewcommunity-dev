package com.renewcommunity.renewcommunity.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 新增用户请求。
 */
public record UserSaveRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(max = 50, message = "用户名长度不能超过 50")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 32, message = "密码长度需在 6-32 位之间")
        String password,

        @Size(max = 50, message = "昵称长度不能超过 50")
        String nickname,

        @Size(max = 20, message = "手机号长度不能超过 20")
        String mobile,

        @Size(max = 100, message = "邮箱长度不能超过 100")
        String email,

        Integer status,

        List<Long> roleIds
) {
}
