package com.renewcommunity.renewcommunity.model.vo;

import java.time.LocalDateTime;

/**
 * 居民小区绑定记录。
 */
public record CommunityBindingVO(
        Long id,
        Long communityId,
        String communityName,
        String operatorName,
        LocalDateTime createdAt
) {
}
