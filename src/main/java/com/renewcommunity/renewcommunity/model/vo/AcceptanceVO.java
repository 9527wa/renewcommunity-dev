package com.renewcommunity.renewcommunity.model.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目验收列表项。
 */
public record AcceptanceVO(
        Long id,
        Long projectId,
        String projectName,
        String title,
        Integer result,
        LocalDate acceptDate,
        String conclusion,
        String creatorName,
        LocalDateTime createdAt,
        List<AcceptanceFileVO> attachments
) {
}
