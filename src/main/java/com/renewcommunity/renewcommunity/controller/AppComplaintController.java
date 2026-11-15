package com.renewcommunity.renewcommunity.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.Result;
import com.renewcommunity.renewcommunity.model.dto.AppComplaintSubmitRequest;
import com.renewcommunity.renewcommunity.model.vo.AppComplaintVO;
import com.renewcommunity.renewcommunity.service.AppComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端投诉接口。
 */
@RestController
@RequestMapping("/api/app/complaint")
@RequiredArgsConstructor
public class AppComplaintController {

    private final AppComplaintService appComplaintService;

    @PostMapping
    public Result<Long> submit(@Valid @RequestBody AppComplaintSubmitRequest request) {
        return Result.ok(appComplaintService.submitComplaint(StpUtil.getLoginIdAsLong(), request));
    }

    @GetMapping("/my")
    public Result<PageResult<AppComplaintVO>> myComplaints(@RequestParam(defaultValue = "1") int pageNum,
                                                           @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(appComplaintService.listMyComplaints(StpUtil.getLoginIdAsLong(), pageNum, pageSize));
    }
}
