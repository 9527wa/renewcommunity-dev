package com.renewcommunity.renewcommunity.service;

import com.renewcommunity.renewcommunity.model.dto.LoginRequest;
import com.renewcommunity.renewcommunity.model.dto.RegisterRequest;
import com.renewcommunity.renewcommunity.model.dto.WechatLoginRequest;
import com.renewcommunity.renewcommunity.model.vo.LoginTokenVO;
import com.renewcommunity.renewcommunity.model.vo.UserInfoVO;

/**
 * 认证服务。
 */
public interface AuthService {

    /**
     * 登录并签发令牌。
     *
     * @param request 登录请求
     * @return 令牌
     */
    LoginTokenVO login(LoginRequest request);

    /**
     * 小程序账号注册并签发令牌。
     *
     * @param request 注册请求
     * @return 令牌
     */
    LoginTokenVO register(RegisterRequest request);

    /**
     * 微信小程序登录，按 openid 查找或创建账号并签发令牌。
     *
     * @param request 微信登录请求
     * @return 令牌
     */
    LoginTokenVO wechatLogin(WechatLoginRequest request);

    /**
     * 获取当前登录用户信息。
     *
     * @return 用户信息
     */
    UserInfoVO getUserInfo();

    /**
     * 退出登录。
     */
    void logout();
}
