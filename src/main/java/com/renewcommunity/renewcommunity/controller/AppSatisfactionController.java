package com.renewcommunity.renewcommunity.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.Result;
import com.renewcommunity.renewcommunity.model.dto.AppSatisfactionSubmitRequest;
import com.renewcommunity.renewcommunity.model.vo.SatisfactionVO;
import com.renewcommunity.renewcommunity.service.AppSatisfactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端满意度评价接口。
 */
@RestController
@RequestMapping("/api/app/satisfaction")
@RequiredArgsConstructor
public class AppSatisfactionController {

    private final AppSatisfactionService appSatisfactionService;

    @GetMapping("/my")
    public Result<PageResult<SatisfactionVO>> myRatings(@RequestParam(defaultValue = "1") int pageNum,
                                                        @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(appSatisfactionService.listMy(StpUtil.getLoginIdAsLong(), pageNum, pageSize));
    }

    @PostMapping
    public Result<Long> submit(@Valid @RequestBody AppSatisfactionSubmitRequest request) {
        return Result.ok(appSatisfactionService.submit(StpUtil.getLoginIdAsLong(), request));
    }
}
