package com.renewcommunity.renewcommunity.model.vo;

import java.time.LocalDateTime;

/**
 * 居民用户列表项。
 */
public record ResidentVO(
        Long id,
        String username,
        String nickname,
        String mobile,
        String email,
        Integer status,
        Long communityId,
        String communityName,
        LocalDateTime createdAt
) {
}
