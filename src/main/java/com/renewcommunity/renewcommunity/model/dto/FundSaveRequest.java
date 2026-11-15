package com.renewcommunity.renewcommunity.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 新增 / 修改资金台账请求。
 */
public record FundSaveRequest(
        Long projectId,

        @NotBlank(message = "资金名称不能为空")
        @Size(max = 100, message = "资金名称长度不能超过 100")
        String name,

        @NotNull(message = "资金类型不能为空")
        @Min(value = 1, message = "资金类型只能是拨款或支出")
        @Max(value = 2, message = "资金类型只能是拨款或支出")
        Integer type,

        @NotNull(message = "金额不能为空")
        @DecimalMin(value = "0.01", message = "金额必须大于 0")
        BigDecimal amount,

        @NotNull(message = "发生日期不能为空")
        LocalDate occurredAt,

        @Size(max = 200, message = "备注长度不能超过 200")
        String remark
) {
}
