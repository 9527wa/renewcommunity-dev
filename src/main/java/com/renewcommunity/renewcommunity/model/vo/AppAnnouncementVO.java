package com.renewcommunity.renewcommunity.model.vo;

import java.time.LocalDateTime;

/**
 * C 端公告列表项。
 */
public record AppAnnouncementVO(
        Long id,
        String title,
        String type,
        String summary,
        LocalDateTime publishedAt
) {
}
