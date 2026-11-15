package com.renewcommunity.renewcommunity.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 新增 / 修改小区请求。
 */
public record CommunitySaveRequest(
        @NotBlank(message = "小区名称不能为空")
        @Size(max = 100, message = "小区名称长度不能超过 100")
        String name,

        @Size(max = 200, message = "小区地址长度不能超过 200")
        String address,

        @Min(value = 0, message = "建成年份不合法")
        @Max(value = 9999, message = "建成年份不合法")
        Integer builtYear,

        @Min(value = 0, message = "楼栋数不能小于 0")
        Integer buildingCount,

        @Min(value = 0, message = "户数不能小于 0")
        Integer householdCount,

        @Size(max = 50, message = "负责人长度不能超过 50")
        String manager,

        @Size(max = 20, message = "负责人电话长度不能超过 20")
        String managerMobile,

        @Min(value = 0, message = "状态不合法")
        @Max(value = 1, message = "状态不合法")
        Integer status,

        @Size(max = 500, message = "备注长度不能超过 500")
        String remark
) {
}
