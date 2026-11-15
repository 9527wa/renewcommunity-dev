package com.renewcommunity.renewcommunity.model.vo;

/**
 * 小区列表项。
 */
public record CommunityVO(
        Long id,
        String name,
        String address,
        Integer builtYear,
        Integer buildingCount,
        Integer householdCount,
        String manager,
        String managerMobile,
        Integer status,
        String remark
) {
}
