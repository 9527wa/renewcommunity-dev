package com.renewcommunity.renewcommunity.common.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 统一业务响应码。
 *
 * <p>前端以 {@code code == 0} 判定成功，其余按业务异常展示 {@code msg}。</p>
 */
@Getter
@RequiredArgsConstructor
public enum ResultCode {

    SUCCESS(0, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "系统内部错误"),
    LOGIN_FAILED(1001, "用户名或密码错误");

    private final int code;
    private final String message;
}
