package com.renewcommunity.renewcommunity.model.vo;

/**
 * 项目验收附件。
 */
public record AcceptanceFileVO(
        Long id,
        Long fileId,
        String fileName,
        Long fileSize,
        String contentType
) {
}
