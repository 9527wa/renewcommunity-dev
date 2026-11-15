package com.renewcommunity.renewcommunity.model.vo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 资金台账列表项。
 */
public record FundVO(
        Long id,
        Long projectId,
        String projectName,
        String name,
        Integer type,
        BigDecimal amount,
        LocalDate occurredAt,
        String remark
) {
}
