package com.renewcommunity.renewcommunity.model.vo;

import java.time.LocalDateTime;

/**
 * C 端我的投诉列表项。
 */
public record AppComplaintVO(
        Long id,
        String title,
        String content,
        String type,
        Integer status,
        Long projectId,
        String projectName,
        String reply,
        LocalDateTime createdAt,
        LocalDateTime handledAt
) {
}
