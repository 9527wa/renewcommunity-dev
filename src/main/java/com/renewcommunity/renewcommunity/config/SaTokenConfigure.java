package com.renewcommunity.renewcommunity.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 路由拦截配置：{@code /api/**} 默认需要登录，登录 / 注册 / 微信登录接口除外。
 */
@Configuration
@RequiredArgsConstructor
public class SaTokenConfigure implements WebMvcConfigurer {

    private final OperationLogInterceptor operationLogInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/register", "/api/auth/wechat-login");

        registry.addInterceptor(operationLogInterceptor)
                .addPathPatterns("/api/**");
    }
}
