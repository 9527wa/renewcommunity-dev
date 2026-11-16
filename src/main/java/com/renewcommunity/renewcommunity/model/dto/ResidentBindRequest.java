package com.renewcommunity.renewcommunity.model.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 居民绑定小区请求。
 */
public record ResidentBindRequest(
        @NotNull(message = "小区不能为空")
        Long communityId
) {
}
