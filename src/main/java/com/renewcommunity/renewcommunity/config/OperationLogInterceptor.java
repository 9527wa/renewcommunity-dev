package com.renewcommunity.renewcommunity.config;

import cn.dev33.satoken.stp.StpUtil;
import com.renewcommunity.renewcommunity.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 记录 /api 下的写操作（POST/PUT/PATCH/DELETE）到操作日志。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperationLogInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTR = "operation-log-start-time";

    private final OperationLogService operationLogService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();

        if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)
                && request.getRequestURI().startsWith("/api/")) {
            request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        Object start = request.getAttribute(START_TIME_ATTR);

        if (!(start instanceof Long startTime)) {
            return;
        }

        long duration = System.currentTimeMillis() - startTime;
        Long userId = null;

        try {
            if (StpUtil.isLogin()) {
                userId = StpUtil.getLoginIdAsLong();
            }
        } catch (Exception ignored) {
            // 未登录或上下文不可用时按匿名记录
        }

        String businessError = (String) request.getAttribute("operation-log-error");
        boolean success = ex == null && businessError == null;
        String errorMsg = businessError != null
                ? businessError
                : (ex == null ? "" : String.valueOf(ex.getMessage()));

        try {
            operationLogService.record(userId, request.getMethod(), request.getRequestURI(), resolveIp(request),
                    duration, success, errorMsg);
        } catch (Exception e) {
            log.warn("记录操作日志失败: {} {}", request.getMethod(), request.getRequestURI(), e);
        }
    }

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");

        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
