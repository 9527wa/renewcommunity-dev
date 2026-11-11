package com.renewcommunity.renewcommunity.common.exception;

import com.renewcommunity.renewcommunity.common.api.ResultCode;
import lombok.Getter;

/**
 * 可预期的业务异常，由全局异常处理器转换为统一响应体。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }
}
