package com.renewcommunity.renewcommunity.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 新增/修改菜单请求。
 */
public record MenuSaveRequest(
        Long parentId,

        @NotBlank(message = "菜单名称不能为空")
        @Size(max = 50, message = "菜单名称长度不能超过 50")
        String name,

        @NotBlank(message = "菜单类型不能为空")
        String type,

        @Size(max = 200, message = "路由路径长度不能超过 200")
        String path,

        @Size(max = 200, message = "组件路径长度不能超过 200")
        String component,

        @Size(max = 100, message = "权限标识长度不能超过 100")
        String permission,

        @Size(max = 50, message = "图标长度不能超过 50")
        String icon,

        Integer sort,

        Integer status
) {
}
