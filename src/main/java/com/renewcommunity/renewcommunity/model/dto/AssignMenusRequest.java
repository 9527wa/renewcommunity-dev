package com.renewcommunity.renewcommunity.model.dto;

import java.util.List;

/**
 * 分配角色菜单请求。
 */
public record AssignMenusRequest(List<Long> menuIds) {
}
