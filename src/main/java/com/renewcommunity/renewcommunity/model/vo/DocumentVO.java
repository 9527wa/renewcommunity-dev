package com.renewcommunity.renewcommunity.model.vo;

import java.time.LocalDateTime;

/**
 * 资料台账列表项。
 */
public record DocumentVO(
        Long id,
        Long projectId,
        String projectName,
        String category,
        String title,
        String fileName,
        Long fileSize,
        String contentType,
        String uploaderName,
        Integer status,
        LocalDateTime createdAt
) {
}
