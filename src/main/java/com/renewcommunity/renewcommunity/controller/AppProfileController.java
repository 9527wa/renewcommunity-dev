package com.renewcommunity.renewcommunity.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.renewcommunity.renewcommunity.common.api.Result;
import com.renewcommunity.renewcommunity.model.dto.AppCommunityBindRequest;
import com.renewcommunity.renewcommunity.model.dto.AppProfileUpdateRequest;
import com.renewcommunity.renewcommunity.model.vo.AppProfileVO;
import com.renewcommunity.renewcommunity.service.AppProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端个人资料接口。
 */
@RestController
@RequestMapping("/api/app/profile")
@RequiredArgsConstructor
public class AppProfileController {

    private final AppProfileService appProfileService;

    @GetMapping
    public Result<AppProfileVO> profile() {
        return Result.ok(appProfileService.getProfile(StpUtil.getLoginIdAsLong()));
    }

    @PutMapping
    public Result<Void> updateProfile(@Valid @RequestBody AppProfileUpdateRequest request) {
        appProfileService.updateProfile(StpUtil.getLoginIdAsLong(), request);
        return Result.ok();
    }

    @PutMapping("/community")
    public Result<Void> bindCommunity(@Valid @RequestBody AppCommunityBindRequest request) {
        appProfileService.bindCommunity(StpUtil.getLoginIdAsLong(), request.communityId());
        return Result.ok();
    }
}
