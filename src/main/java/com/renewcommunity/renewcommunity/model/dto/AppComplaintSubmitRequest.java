package com.renewcommunity.renewcommunity.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * C 端投诉提交请求。
 */
public record AppComplaintSubmitRequest(
        @NotBlank(message = "投诉标题不能为空")
        @Size(max = 100, message = "投诉标题长度不能超过 100")
        String title,

        @NotBlank(message = "投诉内容不能为空")
        @Size(max = 1000, message = "投诉内容长度不能超过 1000")
        String content,

        @Size(max = 30, message = "投诉类型长度不能超过 30")
        String type,

        Long projectId,

        @Size(max = 100, message = "所属小区长度不能超过 100")
        String community,

        @Size(max = 50, message = "联系人长度不能超过 50")
        String contactName,

        @Size(max = 20, message = "联系电话长度不能超过 20")
        String contactMobile
) {
}
