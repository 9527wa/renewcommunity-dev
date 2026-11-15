package com.renewcommunity.renewcommunity.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.renewcommunity.renewcommunity.common.api.Result;
import com.renewcommunity.renewcommunity.model.dto.AssignMenusRequest;
import com.renewcommunity.renewcommunity.model.dto.RoleSaveRequest;
import com.renewcommunity.renewcommunity.model.vo.RoleVO;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色管理接口。
 */
@RestController
@RequestMapping("/api/system/role")
@RequiredArgsConstructor
public class SystemRoleController {

    private final RbacService rbacService;

    @GetMapping("/list")
    @SaCheckPermission("system:role:list")
    public Result<List<RoleVO>> list() {
        return Result.ok(rbacService.listRoles());
    }

    @PostMapping
    @SaCheckPermission("system:role:add")
    public Result<Long> create(@Valid @RequestBody RoleSaveRequest request) {
        return Result.ok(rbacService.createRole(request));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:role:edit")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RoleSaveRequest request) {
        rbacService.updateRole(id, request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:role:delete")
    public Result<Void> delete(@PathVariable Long id) {
        rbacService.deleteRole(id);
        return Result.ok();
    }

    @GetMapping("/{id}/menus")
    @SaCheckPermission("system:role:list")
    public Result<List<Long>> getMenus(@PathVariable Long id) {
        return Result.ok(rbacService.getRoleMenuIds(id));
    }

    @PutMapping("/{id}/menus")
    @SaCheckPermission("system:role:assign")
    public Result<Void> assignMenus(@PathVariable Long id, @RequestBody AssignMenusRequest request) {
        rbacService.assignRoleMenus(id, request.menuIds());
        return Result.ok();
    }
}
