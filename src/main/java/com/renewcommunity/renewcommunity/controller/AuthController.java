package com.renewcommunity.renewcommunity.controller;

import com.renewcommunity.renewcommunity.common.api.Result;
import com.renewcommunity.renewcommunity.model.dto.LoginRequest;
import com.renewcommunity.renewcommunity.model.vo.LoginTokenVO;
import com.renewcommunity.renewcommunity.model.vo.UserInfoVO;
import com.renewcommunity.renewcommunity.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录。
     */
    @PostMapping("/login")
    public Result<LoginTokenVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    /**
     * 获取当前登录用户信息。
     */
    @GetMapping("/getUserInfo")
    public Result<UserInfoVO> getUserInfo() {
        return Result.ok(authService.getUserInfo());
    }

    /**
     * 退出登录。
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.ok();
    }
}
