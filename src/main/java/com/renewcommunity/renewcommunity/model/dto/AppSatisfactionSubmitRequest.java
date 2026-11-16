package com.renewcommunity.renewcommunity.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * C 端满意度评价请求。
 */
public record AppSatisfactionSubmitRequest(
        @NotNull(message = "投诉ID不能为空")
        Long complaintId,

        @NotNull(message = "评分不能为空")
        @Min(value = 1, message = "评分需在 1-5 之间")
        @Max(value = 5, message = "评分需在 1-5 之间")
        Integer score,

        @Size(max = 300, message = "评价内容长度不能超过 300")
        String comment
) {
}
