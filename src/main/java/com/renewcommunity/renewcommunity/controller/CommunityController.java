package com.renewcommunity.renewcommunity.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.Result;
import com.renewcommunity.renewcommunity.model.dto.CommunitySaveRequest;
import com.renewcommunity.renewcommunity.model.vo.CommunityVO;
import com.renewcommunity.renewcommunity.service.CommunityService;
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

import java.util.List;

/**
 * 小区管理接口。
 */
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @GetMapping("/list")
    @SaCheckPermission("community:list")
    public Result<PageResult<CommunityVO>> list(@RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) Integer status,
                                                @RequestParam(defaultValue = "1") int pageNum,
                                                @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(communityService.listCommunities(keyword, status, pageNum, pageSize));
    }

    @GetMapping("/options")
    @SaCheckPermission("community:list")
    public Result<List<CommunityVO>> options() {
        return Result.ok(communityService.listAllCommunities());
    }

    @PostMapping
    @SaCheckPermission("community:add")
    public Result<Long> create(@Valid @RequestBody CommunitySaveRequest request) {
        return Result.ok(communityService.createCommunity(request));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("community:edit")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CommunitySaveRequest request) {
        communityService.updateCommunity(id, request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("community:delete")
    public Result<Void> delete(@PathVariable Long id) {
        communityService.deleteCommunity(id);
        return Result.ok();
    }
}
