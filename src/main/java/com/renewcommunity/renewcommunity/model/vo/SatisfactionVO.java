package com.renewcommunity.renewcommunity.model.vo;

import java.time.LocalDateTime;

/**
 * 满意度评价列表项。
 */
public record SatisfactionVO(
        Long id,
        Long complaintId,
        String complaintTitle,
        String userName,
        Integer score,
        String comment,
        LocalDateTime createdAt
) {
}
