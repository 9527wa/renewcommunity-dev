package com.renewcommunity.renewcommunity.model.vo;

/**
 * 工程进度现场图片。
 */
public record ProgressImageVO(
        Long id,
        Long fileId,
        String fileName,
        Long fileSize,
        String contentType
) {
}
