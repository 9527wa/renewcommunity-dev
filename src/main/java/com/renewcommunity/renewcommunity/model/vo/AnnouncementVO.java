package com.renewcommunity.renewcommunity.model.vo;

import java.time.LocalDateTime;

/**
 * 公告列表项。
 */
public record AnnouncementVO(
        Long id,
        String title,
        String content,
        String type,
        Integer status,
        LocalDateTime publishedAt,
        LocalDateTime createdAt
) {
}
