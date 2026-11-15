package com.renewcommunity.renewcommunity.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 投诉处理请求。
 */
public record ComplaintHandleRequest(
        @NotNull(message = "处理状态不能为空")
        @Min(value = 1, message = "处理状态只能是处理中或已办结")
        @Max(value = 2, message = "处理状态只能是处理中或已办结")
        Integer status,

        @Size(max = 500, message = "处理回复长度不能超过 500")
        String reply
) {
}
