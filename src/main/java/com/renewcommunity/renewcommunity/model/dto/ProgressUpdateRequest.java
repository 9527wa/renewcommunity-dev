package com.renewcommunity.renewcommunity.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * 修改工程进度请求。
 */
public record ProgressUpdateRequest(
        @NotBlank(message = "施工阶段不能为空")
        @Size(max = 20, message = "施工阶段长度不能超过 20")
        String stage,

        @NotBlank(message = "进度标题不能为空")
        @Size(max = 200, message = "进度标题长度不能超过 200")
        String title,

        @Size(max = 1000, message = "进度说明长度不能超过 1000")
        String content,

        @Min(value = 0, message = "进度不能小于 0")
        @Max(value = 100, message = "进度不能超过 100")
        Integer progress,

        @NotNull(message = "上报日期不能为空")
        LocalDate reportDate
) {
}
