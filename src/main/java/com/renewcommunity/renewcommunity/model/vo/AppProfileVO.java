package com.renewcommunity.renewcommunity.model.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * C 端个人资料与统计。
 */
public record AppProfileVO(
        Long userId,
        String userName,
        String username,
        String mobile,
        String email,
        List<String> roles,
        Long communityId,
        String communityName,
        LocalDateTime createdAt,
        long complaintTotal,
        long complaintPending,
        long complaintProcessing,
        long complaintResolved,
        long satisfactionTotal
) {
}
