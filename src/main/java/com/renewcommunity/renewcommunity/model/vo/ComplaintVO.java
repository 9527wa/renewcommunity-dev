package com.renewcommunity.renewcommunity.model.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 投诉列表项。
 */
public record ComplaintVO(
        Long id,
        String title,
        String content,
        String community,
        String type,
        String kind,
        Integer status,
        Long projectId,
        String projectName,
        String contactName,
        String contactMobile,
        String reply,
        LocalDateTime handledAt,
        LocalDateTime createdAt,
        List<FileRefVO> images,
        String location,
        Integer remindCount,
        String orderNo
) {
}
