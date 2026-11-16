package com.renewcommunity.renewcommunity.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.renewcommunity.renewcommunity.common.api.Result;
import com.renewcommunity.renewcommunity.common.api.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理，保证所有接口返回统一的 {@link Result} 结构。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        markFailure(request, e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e,
                                                              HttpServletRequest request) {
        String message = resolveFieldErrorMessage(e.getBindingResult().getFieldError());
        markFailure(request, message);
        return Result.fail(ResultCode.BAD_REQUEST, message);
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e, HttpServletRequest request) {
        String message = resolveFieldErrorMessage(e.getBindingResult().getFieldError());
        markFailure(request, message);
        return Result.fail(ResultCode.BAD_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e,
                                                           HttpServletRequest request) {
        markFailure(request, e.getMessage());
        return Result.fail(ResultCode.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        markFailure(request, "请求体格式不正确");
        return Result.fail(ResultCode.BAD_REQUEST, "请求体格式不正确");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParameterException(MissingServletRequestParameterException e,
                                                        HttpServletRequest request) {
        String message = "缺少必要参数：" + e.getParameterName();
        markFailure(request, message);
        return Result.fail(ResultCode.BAD_REQUEST, message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatchException(MethodArgumentTypeMismatchException e,
                                                    HttpServletRequest request) {
        String message = "参数格式不正确：" + e.getName();
        markFailure(request, message);
        return Result.fail(ResultCode.BAD_REQUEST, message);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUploadSizeException(MaxUploadSizeExceededException e,
                                                     HttpServletRequest request) {
        markFailure(request, "上传文件大小超过限制");
        return Result.fail(ResultCode.BAD_REQUEST, "上传文件大小超过限制");
    }

    @ExceptionHandler(MultipartException.class)
    public Result<Void> handleMultipartException(MultipartException e, HttpServletRequest request) {
        markFailure(request, "文件上传失败");
        return Result.fail(ResultCode.BAD_REQUEST, "文件上传失败");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNoResourceFoundException(NoResourceFoundException e, HttpServletRequest request) {
        return Result.fail(ResultCode.NOT_FOUND, "资源不存在");
    }

    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleNotLoginException(NotLoginException e, HttpServletRequest request) {
        log.warn("未登录访问: {}", e.getMessage());
        markFailure(request, ResultCode.UNAUTHORIZED.getMessage());
        return Result.fail(ResultCode.UNAUTHORIZED);
    }

    @ExceptionHandler({NotPermissionException.class, NotRoleException.class})
    public Result<Void> handleNotPermissionException(Exception e, HttpServletRequest request) {
        log.warn("无权限访问: {}", e.getMessage());
        markFailure(request, ResultCode.FORBIDDEN.getMessage());
        return Result.fail(ResultCode.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常", e);
        markFailure(request, ResultCode.INTERNAL_ERROR.getMessage());
        return Result.fail(ResultCode.INTERNAL_ERROR);
    }

    private void markFailure(HttpServletRequest request, String message) {
        request.setAttribute("operation-log-error", message == null ? "操作失败" : message);
    }

    private String resolveFieldErrorMessage(FieldError fieldError) {
        return fieldError == null ? ResultCode.BAD_REQUEST.getMessage() : fieldError.getDefaultMessage();
    }
}
