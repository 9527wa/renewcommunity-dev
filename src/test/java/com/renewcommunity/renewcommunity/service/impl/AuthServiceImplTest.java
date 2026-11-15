package com.renewcommunity.renewcommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.renewcommunity.renewcommunity.common.api.ResultCode;
import com.renewcommunity.renewcommunity.common.exception.BusinessException;
import com.renewcommunity.renewcommunity.config.WechatProperties;
import com.renewcommunity.renewcommunity.entity.SysUser;
import com.renewcommunity.renewcommunity.mapper.SysMenuMapper;
import com.renewcommunity.renewcommunity.mapper.SysRoleMapper;
import com.renewcommunity.renewcommunity.mapper.SysUserMapper;
import com.renewcommunity.renewcommunity.mapper.SysUserRoleMapper;
import com.renewcommunity.renewcommunity.model.dto.LoginRequest;
import com.renewcommunity.renewcommunity.model.dto.RegisterRequest;
import com.renewcommunity.renewcommunity.model.dto.WechatLoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link AuthServiceImpl} 单元测试。登录 / 注册失败分支不依赖 Sa-Token 与 Redis。
 */
class AuthServiceImplTest {

    private final SysUserMapper userMapper = mock(SysUserMapper.class);

    private final SysRoleMapper roleMapper = mock(SysRoleMapper.class);

    private final SysUserRoleMapper userRoleMapper = mock(SysUserRoleMapper.class);

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final WechatProperties wechatProperties = new WechatProperties("", "");

    private final AuthServiceImpl authService = new AuthServiceImpl(
            userMapper,
            roleMapper,
            mock(SysMenuMapper.class),
            userRoleMapper,
            passwordEncoder,
            wechatProperties,
            RestClient.builder()
    );

    @Test
    void loginShouldThrowWhenUserNotExists() {
        when(userMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.login(new LoginRequest("admin", "123456")));

        assertEquals(ResultCode.LOGIN_FAILED.getCode(), exception.getCode());
    }

    @Test
    void loginShouldThrowWhenPasswordIncorrect() {
        when(userMapper.selectOne(any(Wrapper.class))).thenReturn(activeUser());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.login(new LoginRequest("admin", "wrong-password")));

        assertEquals(ResultCode.LOGIN_FAILED.getCode(), exception.getCode());
        assertEquals(ResultCode.LOGIN_FAILED.getMessage(), exception.getMessage());
    }

    @Test
    void loginShouldThrowWhenUserDisabled() {
        SysUser user = activeUser();
        user.setStatus(0);
        when(userMapper.selectOne(any(Wrapper.class))).thenReturn(user);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.login(new LoginRequest("admin", "123456")));

        assertEquals(ResultCode.LOGIN_FAILED.getCode(), exception.getCode());
    }

    @Test
    void registerShouldThrowWhenUsernameExists() {
        when(userMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.register(new RegisterRequest("admin", "123456", "管理员", null)));

        assertEquals(ResultCode.BAD_REQUEST.getCode(), exception.getCode());
        assertEquals("用户名已存在", exception.getMessage());
    }

    @Test
    void wechatLoginShouldThrowWhenNotConfigured() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.wechatLogin(new WechatLoginRequest("wx-code", "微信用户")));

        assertEquals(ResultCode.BAD_REQUEST.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("微信登录未配置"));
    }

    private SysUser activeUser() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setStatus(1);

        return user;
    }
}
