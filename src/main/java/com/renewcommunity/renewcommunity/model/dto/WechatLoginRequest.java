package com.renewcommunity.renewcommunity.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 微信小程序登录请求。
 *
 * @param code     wx.login 获取的临时登录凭证
 * @param nickname 微信昵称，可空
 */
public record WechatLoginRequest(
        @NotBlank(message = "微信登录凭证不能为空")
        String code,

        @Size(max = 50, message = "昵称长度不能超过 50")
        String nickname
) {
}
