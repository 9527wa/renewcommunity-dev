package com.renewcommunity.renewcommunity.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 新增 / 修改项目请求。
 */
public record ProjectSaveRequest(
        @NotBlank(message = "项目名称不能为空")
        @Size(max = 100, message = "项目名称长度不能超过 100")
        String name,

        @Size(max = 100, message = "所属小区长度不能超过 100")
        String community,

        @Size(max = 50, message = "改造类别长度不能超过 50")
        String category,

        @Min(value = 0, message = "项目状态不合法")
        @Max(value = 2, message = "项目状态不合法")
        Integer status,

        @Min(value = 0, message = "进度不能小于 0")
        @Max(value = 100, message = "进度不能超过 100")
        Integer progress,

        BigDecimal budget,

        LocalDate startDate,

        LocalDate endDate,

        @Size(max = 50, message = "负责人长度不能超过 50")
        String manager,

        @Size(max = 100, message = "施工单位长度不能超过 100")
        String constructionUnit,

        @Size(max = 500, message = "项目简介长度不能超过 500")
        String description
) {
}
