package com.renewcommunity.renewcommunity.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.Result;
import com.renewcommunity.renewcommunity.model.dto.ResidentBindRequest;
import com.renewcommunity.renewcommunity.model.vo.CommunityBindingVO;
import com.renewcommunity.renewcommunity.model.vo.ResidentVO;
import com.renewcommunity.renewcommunity.service.ResidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 居民用户管理接口。
 */
@RestController
@RequestMapping("/api/resident")
@RequiredArgsConstructor
public class ResidentController {

    private final ResidentService residentService;

    @GetMapping("/list")
    @SaCheckPermission("resident:list")
    public Result<PageResult<ResidentVO>> list(@RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) Long communityId,
                                               @RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(residentService.listResidents(keyword, communityId, pageNum, pageSize));
    }

    @PutMapping("/{id}/community")
    @SaCheckPermission("resident:bind")
    public Result<Void> bindCommunity(@PathVariable Long id, @Valid @RequestBody ResidentBindRequest request) {
        residentService.bindCommunity(id, request.communityId());
        return Result.ok();
    }

    @GetMapping("/{id}/bindings")
    @SaCheckPermission("resident:list")
    public Result<List<CommunityBindingVO>> bindings(@PathVariable Long id) {
        return Result.ok(residentService.listBindings(id));
    }
}
