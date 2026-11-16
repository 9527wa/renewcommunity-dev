package com.renewcommunity.renewcommunity.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.Result;
import com.renewcommunity.renewcommunity.model.vo.SatisfactionStatsVO;
import com.renewcommunity.renewcommunity.model.vo.SatisfactionVO;
import com.renewcommunity.renewcommunity.service.SatisfactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 满意度管理接口。
 */
@RestController
@RequestMapping("/api/satisfaction")
@RequiredArgsConstructor
public class SatisfactionController {

    private final SatisfactionService satisfactionService;

    @GetMapping("/list")
    @SaCheckPermission("satisfaction:list")
    public Result<PageResult<SatisfactionVO>> list(@RequestParam(required = false) Integer score,
                                                   @RequestParam(defaultValue = "1") int pageNum,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(satisfactionService.listSatisfactions(score, pageNum, pageSize));
    }

    @GetMapping("/stats")
    @SaCheckPermission("satisfaction:list")
    public Result<SatisfactionStatsVO> stats() {
        return Result.ok(satisfactionService.getStats());
    }
}
