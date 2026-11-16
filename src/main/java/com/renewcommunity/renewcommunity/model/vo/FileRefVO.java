package com.renewcommunity.renewcommunity.model.vo;

/**
 * 通用文件引用（图片/附件）。
 */
public record FileRefVO(
        Long fileId,
        String fileName,
        String contentType
) {
}
