package com.renewcommunity.renewcommunity.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.Result;
import com.renewcommunity.renewcommunity.model.dto.PasswordResetRequest;
import com.renewcommunity.renewcommunity.model.dto.UserSaveRequest;
import com.renewcommunity.renewcommunity.model.dto.UserUpdateRequest;
import com.renewcommunity.renewcommunity.model.vo.UserVO;
import com.renewcommunity.renewcommunity.service.RbacService;
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
 * 用户管理接口。
 */
@RestController
@RequestMapping("/api/system/user")
@RequiredArgsConstructor
public class SystemUserController {

    private final RbacService rbacService;

    @GetMapping("/list")
    @SaCheckPermission("system:user:list")
    public Result<PageResult<UserVO>> list(@RequestParam(required = false) String keyword,
                                           @RequestParam(defaultValue = "1") int pageNum,
                                           @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(rbacService.listUsers(keyword, pageNum, pageSize));
    }

    @PostMapping
    @SaCheckPermission("system:user:add")
    public Result<Long> create(@Valid @RequestBody UserSaveRequest request) {
        return Result.ok(rbacService.createUser(request));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:user:edit")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        rbacService.updateUser(id, request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:user:delete")
    public Result<Void> delete(@PathVariable Long id) {
        rbacService.deleteUser(id);
        return Result.ok();
    }

    @PutMapping("/{id}/password")
    @SaCheckPermission("system:user:reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody PasswordResetRequest request) {
        rbacService.resetPassword(id, request);
        return Result.ok();
    }
}
