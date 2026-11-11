package com.renewcommunity.renewcommunity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 跨域配置，前端独立部署时按环境覆盖 {@code app.cors.allowed-origins}。
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {

    public CorsProperties {
        allowedOrigins = allowedOrigins == null || allowedOrigins.isEmpty()
                ? List.of("http://localhost:9527")
                : List.copyOf(allowedOrigins);
    }
}
