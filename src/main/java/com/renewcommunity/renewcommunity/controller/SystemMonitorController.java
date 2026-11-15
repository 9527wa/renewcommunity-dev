package com.renewcommunity.renewcommunity.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.renewcommunity.renewcommunity.common.api.Result;
import com.renewcommunity.renewcommunity.model.vo.ServerStatusVO;
import com.renewcommunity.renewcommunity.service.SystemMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务器监控接口。
 */
@RestController
@RequestMapping("/api/system/monitor")
@RequiredArgsConstructor
public class SystemMonitorController {

    private final SystemMonitorService systemMonitorService;

    /**
     * 获取服务器运行状态与内存状态。
     */
    @GetMapping
    @SaCheckPermission("system:monitor:view")
    public Result<ServerStatusVO> status() {
        return Result.ok(systemMonitorService.getServerStatus());
    }
}
