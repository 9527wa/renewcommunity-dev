package com.renewcommunity.renewcommunity.model.vo;

import java.util.List;

/**
 * 角色列表项。
 */
public record RoleVO(
        Long id,
        String code,
        String name,
        Integer sort,
        Integer status,
        String remark,
        List<Long> menuIds
) {
}
