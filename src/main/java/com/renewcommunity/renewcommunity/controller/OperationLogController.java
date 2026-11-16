package com.renewcommunity.renewcommunity.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.Result;
import com.renewcommunity.renewcommunity.model.vo.OperationLogVO;
import com.renewcommunity.renewcommunity.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志接口。
 */
@RestController
@RequestMapping("/api/system/log")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    @GetMapping("/list")
    @SaCheckPermission("log:list")
    public Result<PageResult<OperationLogVO>> list(@RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) Integer success,
                                                   @RequestParam(defaultValue = "1") int pageNum,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(operationLogService.listLogs(keyword, success, pageNum, pageSize));
    }
}
