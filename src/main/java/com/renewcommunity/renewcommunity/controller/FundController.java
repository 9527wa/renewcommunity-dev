package com.renewcommunity.renewcommunity.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.Result;
import com.renewcommunity.renewcommunity.model.dto.FundSaveRequest;
import com.renewcommunity.renewcommunity.model.vo.FundVO;
import com.renewcommunity.renewcommunity.service.FundService;
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
 * 资金台账接口。
 */
@RestController
@RequestMapping("/api/fund")
@RequiredArgsConstructor
public class FundController {

    private final FundService fundService;

    @GetMapping("/list")
    @SaCheckPermission("fund:list")
    public Result<PageResult<FundVO>> list(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Integer type,
                                           @RequestParam(required = false) Long projectId,
                                           @RequestParam(defaultValue = "1") int pageNum,
                                           @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(fundService.listFunds(keyword, type, projectId, pageNum, pageSize));
    }

    @PostMapping
    @SaCheckPermission("fund:add")
    public Result<Long> create(@Valid @RequestBody FundSaveRequest request) {
        return Result.ok(fundService.createFund(request));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("fund:edit")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody FundSaveRequest request) {
        fundService.updateFund(id, request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("fund:delete")
    public Result<Void> delete(@PathVariable Long id) {
        fundService.deleteFund(id);
        return Result.ok();
    }
}
