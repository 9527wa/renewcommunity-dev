package com.renewcommunity.renewcommunity.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * C 端个人资料修改请求。
 */
public record AppProfileUpdateRequest(
        @Size(max = 50, message = "昵称长度不能超过 50")
        String nickname,

        @Size(max = 20, message = "手机号长度不能超过 20")
        String mobile,

        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱长度不能超过 100")
        String email
) {
}
