package com.renewcommunity.renewcommunity.model.vo;

import java.time.LocalDateTime;

/**
 * 文件管理列表项。
 */
public record FileVO(
        Long id,
        String originalName,
        String contentType,
        Long size,
        String uploaderName,
        LocalDateTime createdAt
) {
}
