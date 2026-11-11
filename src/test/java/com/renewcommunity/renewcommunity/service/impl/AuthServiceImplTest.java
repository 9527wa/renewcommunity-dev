package com.renewcommunity.renewcommunity.service.impl;

import com.renewcommunity.renewcommunity.common.api.ResultCode;
import com.renewcommunity.renewcommunity.common.exception.BusinessException;
import com.renewcommunity.renewcommunity.config.SecurityProperties;
import com.renewcommunity.renewcommunity.model.dto.LoginRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link AuthServiceImpl} 单元测试。登录失败分支不依赖 Sa-Token 与 Redis。
 */
class AuthServiceImplTest {

    private final SecurityProperties securityProperties =
            new SecurityProperties("admin", "123456", "管理员", List.of("R_SUPER"));

    private final AuthServiceImpl authService = new AuthServiceImpl(securityProperties);

    @Test
    void loginShouldThrowWhenPasswordIncorrect() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.login(new LoginRequest("admin", "wrong-password")));

        assertEquals(ResultCode.LOGIN_FAILED.getCode(), exception.getCode());
        assertEquals(ResultCode.LOGIN_FAILED.getMessage(), exception.getMessage());
    }

    @Test
    void loginShouldThrowWhenUserNameIncorrect() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.login(new LoginRequest("nobody", "123456")));

        assertEquals(ResultCode.LOGIN_FAILED.getCode(), exception.getCode());
    }
}
