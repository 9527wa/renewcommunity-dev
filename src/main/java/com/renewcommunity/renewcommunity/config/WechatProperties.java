package com.renewcommunity.renewcommunity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * 微信小程序配置，用于 code 换取 openid。
 *
 * <p>未配置时微信登录接口会返回明确的业务错误，账号密码登录与注册不受影响。</p>
 */
@ConfigurationProperties(prefix = "app.wechat")
public record WechatProperties(String appId, String secret) {

    public boolean isConfigured() {
        return StringUtils.hasText(appId) && StringUtils.hasText(secret);
    }
}
