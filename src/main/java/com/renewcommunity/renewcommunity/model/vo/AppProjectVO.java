package com.renewcommunity.renewcommunity.model.vo;

import java.time.LocalDate;

/**
 * C 端项目信息（不含预算等敏感数据）。
 */
public record AppProjectVO(
        Long id,
        String name,
        String community,
        String category,
        Integer status,
        Integer progress,
        LocalDate startDate,
        LocalDate endDate,
        String description
) {
}
