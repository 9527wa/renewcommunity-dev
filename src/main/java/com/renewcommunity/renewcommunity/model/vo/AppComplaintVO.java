package com.renewcommunity.renewcommunity.model.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * C 端我的投诉列表项。
 */
public record AppComplaintVO(
        Long id,
        String title,
        String content,
        String type,
        String kind,
        String community,
        Integer status,
        Long projectId,
        String projectName,
        String reply,
        LocalDateTime createdAt,
        LocalDateTime handledAt,
        Boolean rated,
        Integer score,
        List<FileRefVO> images,
        String location,
        Integer remindCount,
        String orderNo
) {
}
