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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @GetMapping("/{id}")
    public Result<AppComplaintVO> detail(@PathVariable Long id) {
        return Result.ok(appComplaintService.getMyComplaint(StpUtil.getLoginIdAsLong(), id));
    }

    @PostMapping("/{id}/images")
    public Result<Void> uploadImages(@PathVariable Long id,
                                     @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        appComplaintService.uploadImages(StpUtil.getLoginIdAsLong(), id, files);
        return Result.ok();
    }

    @PostMapping("/{id}/remind")
    public Result<Integer> remind(@PathVariable Long id) {
        return Result.ok(appComplaintService.remindComplaint(StpUtil.getLoginIdAsLong(), id));
    }

    @GetMapping("/my")
    public Result<PageResult<AppComplaintVO>> myComplaints(@RequestParam(required = false) Integer status,
                                                           @RequestParam(defaultValue = "1") int pageNum,
                                                           @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(appComplaintService.listMyComplaints(StpUtil.getLoginIdAsLong(), status, pageNum, pageSize));
    }
}
