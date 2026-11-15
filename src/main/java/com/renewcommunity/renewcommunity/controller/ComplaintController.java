package com.renewcommunity.renewcommunity.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.Result;
import com.renewcommunity.renewcommunity.model.dto.ComplaintHandleRequest;
import com.renewcommunity.renewcommunity.model.dto.ComplaintSaveRequest;
import com.renewcommunity.renewcommunity.model.vo.ComplaintVO;
import com.renewcommunity.renewcommunity.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 投诉管理接口。
 */
@RestController
@RequestMapping("/api/complaint")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @GetMapping("/list")
    @SaCheckPermission("complaint:list")
    public Result<PageResult<ComplaintVO>> list(@RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) Integer status,
                                                @RequestParam(defaultValue = "1") int pageNum,
                                                @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(complaintService.listComplaints(keyword, status, pageNum, pageSize));
    }

    @PostMapping
    @SaCheckPermission("complaint:add")
    public Result<Long> create(@Valid @RequestBody ComplaintSaveRequest request) {
        return Result.ok(complaintService.createComplaint(request));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("complaint:edit")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ComplaintSaveRequest request) {
        complaintService.updateComplaint(id, request);
        return Result.ok();
    }

    @PutMapping("/{id}/handle")
    @SaCheckPermission("complaint:handle")
    public Result<Void> handle(@PathVariable Long id, @Valid @RequestBody ComplaintHandleRequest request) {
        complaintService.handleComplaint(id, request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("complaint:delete")
    public Result<Void> delete(@PathVariable Long id) {
        complaintService.deleteComplaint(id);
        return Result.ok();
    }
}
