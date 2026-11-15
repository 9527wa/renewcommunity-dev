package com.renewcommunity.renewcommunity.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.renewcommunity.renewcommunity.common.api.ResultCode;
import com.renewcommunity.renewcommunity.common.exception.BusinessException;
import com.renewcommunity.renewcommunity.config.WechatProperties;
import com.renewcommunity.renewcommunity.entity.SysRole;
import com.renewcommunity.renewcommunity.entity.SysUser;
import com.renewcommunity.renewcommunity.entity.SysUserRole;
import com.renewcommunity.renewcommunity.mapper.SysMenuMapper;
import com.renewcommunity.renewcommunity.mapper.SysRoleMapper;
import com.renewcommunity.renewcommunity.mapper.SysUserMapper;
import com.renewcommunity.renewcommunity.mapper.SysUserRoleMapper;
import com.renewcommunity.renewcommunity.model.dto.LoginRequest;
import com.renewcommunity.renewcommunity.model.dto.RegisterRequest;
import com.renewcommunity.renewcommunity.model.dto.WechatLoginRequest;
import com.renewcommunity.renewcommunity.model.vo.LoginTokenVO;
import com.renewcommunity.renewcommunity.model.vo.UserInfoVO;
import com.renewcommunity.renewcommunity.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * 认证服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /** 小程序注册用户的默认角色 */
    private static final String DEFAULT_ROLE_CODE = "R_USER";

    private static final String WECHAT_SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    private final SysUserMapper userMapper;

    private final SysRoleMapper roleMapper;

    private final SysMenuMapper menuMapper;

    private final SysUserRoleMapper userRoleMapper;

    private final PasswordEncoder passwordEncoder;

    private final WechatProperties wechatProperties;

    private final RestClient.Builder restClientBuilder;

    @Override
    public LoginTokenVO login(LoginRequest request) {
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, request.userName()));

        if (user == null
                || !Integer.valueOf(1).equals(user.getStatus())
                || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        StpUtil.login(user.getId());
        log.info("用户登录成功: {}", user.getUsername());

        return new LoginTokenVO(StpUtil.getTokenValue());
    }

    @Override
    @Transactional
    public LoginTokenVO register(RegisterRequest request) {
        long count = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, request.username()));
        if (count > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setNickname(StringUtils.hasText(request.nickname()) ? request.nickname() : request.username());
        user.setMobile(request.mobile());
        user.setStatus(1);
        userMapper.insert(user);

        assignDefaultRole(user);

        StpUtil.login(user.getId());
        log.info("小程序用户注册成功: {}", user.getUsername());

        return new LoginTokenVO(StpUtil.getTokenValue());
    }

    @Override
    @Transactional
    public LoginTokenVO wechatLogin(WechatLoginRequest request) {
        if (!wechatProperties.isConfigured()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "微信登录未配置，请联系管理员设置 AppID 与 Secret");
        }

        WechatSessionResponse session = fetchWechatSession(request.code());

        String username = "wx_" + session.openid();
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, username));

        if (user == null) {
            user = new SysUser();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setNickname(StringUtils.hasText(request.nickname()) ? request.nickname() : "微信用户");
            user.setStatus(1);
            userMapper.insert(user);

            assignDefaultRole(user);
            log.info("微信用户首次登录，已创建账号: {}", username);
        }

        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(ResultCode.LOGIN_FAILED, "账号已被停用");
        }

        StpUtil.login(user.getId());
        log.info("微信用户登录成功: {}", username);

        return new LoginTokenVO(StpUtil.getTokenValue());
    }

    @Override
    public UserInfoVO getUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        List<String> roles = roleMapper.selectEnabledByUserId(userId).stream()
                .map(SysRole::getCode)
                .toList();
        List<String> buttons = menuMapper.selectPermissionsByUserId(userId);
        String userName = StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();

        return new UserInfoVO(String.valueOf(user.getId()), userName, roles, buttons);
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    /**
     * 为小程序注册 / 微信登录创建的账号授予默认角色。
     */
    private void assignDefaultRole(SysUser user) {
        SysRole defaultRole = roleMapper.selectOne(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getCode, DEFAULT_ROLE_CODE));
        if (defaultRole == null) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "默认角色 " + DEFAULT_ROLE_CODE + " 未配置");
        }

        userRoleMapper.insert(new SysUserRole(user.getId(), defaultRole.getId()));
    }

    private WechatSessionResponse fetchWechatSession(String code) {
        URI uri = UriComponentsBuilder.fromUriString(WECHAT_SESSION_URL)
                .queryParam("appid", wechatProperties.appId())
                .queryParam("secret", wechatProperties.secret())
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .encode()
                .build()
                .toUri();

        try {
            WechatSessionResponse response = restClientBuilder.build()
                    .get()
                    .uri(uri)
                    .retrieve()
                    .body(WechatSessionResponse.class);

            if (response == null || !StringUtils.hasText(response.openid())) {
                String reason = response == null || !StringUtils.hasText(response.errmsg())
                        ? "未知错误"
                        : response.errmsg();
                throw new BusinessException(ResultCode.BAD_REQUEST, "微信登录失败：" + reason);
            }

            return response;
        } catch (RestClientException e) {
            log.error("微信登录请求失败", e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "微信登录服务暂不可用");
        }
    }

    /**
     * 微信 jscode2session 响应。
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WechatSessionResponse(
            String openid,
            @JsonProperty("session_key") String sessionKey,
            Integer errcode,
            String errmsg) {
    }
}
