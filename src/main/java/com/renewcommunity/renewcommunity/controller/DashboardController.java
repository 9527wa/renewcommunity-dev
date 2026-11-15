package com.renewcommunity.renewcommunity.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.renewcommunity.renewcommunity.common.api.Result;
import com.renewcommunity.renewcommunity.model.vo.DashboardVO;
import com.renewcommunity.renewcommunity.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端首页工作台接口。
 */
@RestController
@RequestMapping("/api/system/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 项目 / 投诉 / 资金统计。
     */
    @GetMapping
    @SaCheckPermission(value = {"project:list", "complaint:list", "fund:list"}, mode = SaMode.OR)
    public Result<DashboardVO> dashboard() {
        return Result.ok(dashboardService.getDashboard());
    }
}
