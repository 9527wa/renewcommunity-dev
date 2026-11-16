package com.renewcommunity.renewcommunity.model.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 工程进度列表项。
 */
public record ProgressVO(
        Long id,
        Long projectId,
        String projectName,
        String stage,
        String title,
        String content,
        Integer progress,
        LocalDate reportDate,
        String creatorName,
        LocalDateTime createdAt,
        List<ProgressImageVO> images
) {
}
