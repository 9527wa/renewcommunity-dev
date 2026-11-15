package com.renewcommunity.renewcommunity.model.vo;

import java.time.LocalDateTime;

/**
 * C 端公告详情。
 */
public record AppAnnouncementDetailVO(
        Long id,
        String title,
        String content,
        String type,
        LocalDateTime publishedAt
) {
}
