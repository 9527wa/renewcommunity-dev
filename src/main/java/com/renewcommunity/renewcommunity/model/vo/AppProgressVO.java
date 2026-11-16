package com.renewcommunity.renewcommunity.model.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * C 端项目进度动态（不含现场图片）。
 */
public record AppProgressVO(
        Long id,
        String stage,
        String title,
        String content,
        Integer progress,
        LocalDate reportDate,
        LocalDateTime createdAt,
        List<FileRefVO> images
) {
}
