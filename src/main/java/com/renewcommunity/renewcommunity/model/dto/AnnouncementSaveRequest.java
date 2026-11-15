package com.renewcommunity.renewcommunity.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 新增 / 修改公告请求。
 */
public record AnnouncementSaveRequest(
        @NotBlank(message = "公告标题不能为空")
        @Size(max = 200, message = "公告标题长度不能超过 200")
        String title,

        @NotBlank(message = "公告内容不能为空")
        String content,

        @Size(max = 30, message = "公告类型长度不能超过 30")
        String type
) {
}
