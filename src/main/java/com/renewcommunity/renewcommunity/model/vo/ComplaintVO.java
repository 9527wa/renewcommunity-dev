package com.renewcommunity.renewcommunity.model.vo;

import java.time.LocalDateTime;

/**
 * 投诉列表项。
 */
public record ComplaintVO(
        Long id,
        String title,
        String content,
        String community,
        String type,
        Integer status,
        Long projectId,
        String projectName,
        String contactName,
        String contactMobile,
        String reply,
        LocalDateTime handledAt,
        LocalDateTime createdAt
) {
}
