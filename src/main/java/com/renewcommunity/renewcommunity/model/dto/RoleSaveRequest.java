package com.renewcommunity.renewcommunity.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 新增/修改角色请求。
 */
public record RoleSaveRequest(
        @NotBlank(message = "角色标识不能为空")
        @Size(max = 50, message = "角色标识长度不能超过 50")
        String code,

        @NotBlank(message = "角色名称不能为空")
        @Size(max = 50, message = "角色名称长度不能超过 50")
        String name,

        Integer sort,

        Integer status,

        @Size(max = 200, message = "备注长度不能超过 200")
        String remark,

        List<Long> menuIds
) {
}
