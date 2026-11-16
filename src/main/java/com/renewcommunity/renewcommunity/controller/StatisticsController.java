package com.renewcommunity.renewcommunity.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.renewcommunity.renewcommunity.common.api.Result;
import com.renewcommunity.renewcommunity.model.vo.StatisticsVO;
import com.renewcommunity.renewcommunity.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计分析接口。
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/overview")
    @SaCheckPermission("statistics:view")
    public Result<StatisticsVO> overview() {
        return Result.ok(statisticsService.getOverview());
    }
}
