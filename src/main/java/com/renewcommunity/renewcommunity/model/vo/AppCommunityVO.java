package com.renewcommunity.renewcommunity.model.vo;

/**
 * C 端小区选项。
 */
public record AppCommunityVO(
        Long id,
        String name,
        String address,
        Integer buildingCount,
        Integer householdCount
) {
}
