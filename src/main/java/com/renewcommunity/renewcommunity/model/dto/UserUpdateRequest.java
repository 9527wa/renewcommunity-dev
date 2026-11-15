package com.renewcommunity.renewcommunity.model.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 修改用户请求。
 */
public record UserUpdateRequest(
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
