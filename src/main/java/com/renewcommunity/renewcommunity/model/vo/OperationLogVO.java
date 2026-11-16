package com.renewcommunity.renewcommunity.model.vo;

import java.time.LocalDateTime;

/**
 * 操作日志列表项。
 */
public record OperationLogVO(
        Long id,
        Long userId,
        String userName,
        String method,
        String path,
        String ip,
        Long durationMs,
        Integer success,
        String errorMsg,
        LocalDateTime createdAt
) {
}
