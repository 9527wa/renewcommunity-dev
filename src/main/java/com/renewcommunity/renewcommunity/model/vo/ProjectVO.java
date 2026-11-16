package com.renewcommunity.renewcommunity.model.vo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 项目列表项。
 */
public record ProjectVO(
        Long id,
        String name,
        String community,
        String category,
        Integer status,
        Integer progress,
        BigDecimal budget,
        LocalDate startDate,
        LocalDate endDate,
        String manager,
        String constructionUnit,
        String description
) {
}
