package com.renewcommunity.renewcommunity.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 资料归档状态请求。
 */
public record DocumentStatusRequest(
        @NotNull(message = "资料状态不能为空")
        @Min(value = 0, message = "资料状态不合法")
        @Max(value = 1, message = "资料状态不合法")
        Integer status
) {
}
