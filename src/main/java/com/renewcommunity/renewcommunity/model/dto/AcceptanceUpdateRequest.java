package com.renewcommunity.renewcommunity.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * 修改项目验收请求。
 */
public record AcceptanceUpdateRequest(
        @NotBlank(message = "验收名称不能为空")
        @Size(max = 200, message = "验收名称长度不能超过 200")
        String title,

        @Min(value = 0, message = "验收结果不合法")
        @Max(value = 2, message = "验收结果不合法")
        Integer result,

        LocalDate acceptDate,

        @Size(max = 1000, message = "验收结论长度不能超过 1000")
        String conclusion
) {
}
