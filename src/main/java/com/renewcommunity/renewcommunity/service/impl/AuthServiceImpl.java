package com.renewcommunity.renewcommunity.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.renewcommunity.renewcommunity.common.api.ResultCode;
import com.renewcommunity.renewcommunity.common.exception.BusinessException;
import com.renewcommunity.renewcommunity.config.SecurityProperties;
import com.renewcommunity.renewcommunity.model.dto.LoginRequest;
import com.renewcommunity.renewcommunity.model.vo.LoginTokenVO;
import com.renewcommunity.renewcommunity.model.vo.UserInfoVO;
import com.renewcommunity.renewcommunity.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 认证服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SecurityProperties securityProperties;

    @Override
    public LoginTokenVO login(LoginRequest request) {
        // TODO 接入用户表后改为数据库查询，并使用 BCrypt 校验密码
        if (!securityProperties.username().equals(request.userName())
                || !securityProperties.password().equals(request.password())) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        StpUtil.login(request.userName());
        log.info("用户登录成功: {}", request.userName());

        return new LoginTokenVO(StpUtil.getTokenValue());
    }

    @Override
    public UserInfoVO getUserInfo() {
        String userName = StpUtil.getLoginIdAsString();

        return new UserInfoVO(userName, securityProperties.nickname(), securityProperties.roles(), List.of());
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }
}
