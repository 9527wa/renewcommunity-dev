package com.renewcommunity.renewcommunity.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.renewcommunity.renewcommunity.common.api.Result;
import com.renewcommunity.renewcommunity.model.dto.MenuSaveRequest;
import com.renewcommunity.renewcommunity.model.vo.MenuVO;
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
 * 菜单管理接口。
 */
@RestController
@RequestMapping("/api/system/menu")
@RequiredArgsConstructor
public class SystemMenuController {

    private final RbacService rbacService;

    @GetMapping("/tree")
    @SaCheckPermission("system:menu:list")
    public Result<List<MenuVO>> tree() {
        return Result.ok(rbacService.getMenuTree());
    }

    @PostMapping
    @SaCheckPermission("system:menu:add")
    public Result<Long> create(@Valid @RequestBody MenuSaveRequest request) {
        return Result.ok(rbacService.createMenu(request));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:menu:edit")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody MenuSaveRequest request) {
        rbacService.updateMenu(id, request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:menu:delete")
    public Result<Void> delete(@PathVariable Long id) {
        rbacService.deleteMenu(id);
        return Result.ok();
    }
}
