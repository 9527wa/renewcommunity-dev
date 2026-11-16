package com.renewcommunity.renewcommunity.model.dto;

import jakarta.validation.constraints.NotNull;

/**
 * C 端绑定 / 切换小区请求。
 */
public record AppCommunityBindRequest(
        @NotNull(message = "小区不能为空")
        Long communityId
) {
}
