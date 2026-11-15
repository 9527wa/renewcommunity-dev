package com.renewcommunity.renewcommunity.security;

import cn.dev33.satoken.stp.StpInterface;
import com.renewcommunity.renewcommunity.entity.SysRole;
import com.renewcommunity.renewcommunity.mapper.SysMenuMapper;
import com.renewcommunity.renewcommunity.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限数据来源：按登录用户 ID 查询数据库中的角色与权限标识。
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysRoleMapper roleMapper;

    private final SysMenuMapper menuMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = parseUserId(loginId);

        return userId == null ? List.of() : menuMapper.selectPermissionsByUserId(userId);
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = parseUserId(loginId);

        if (userId == null) {
            return List.of();
        }

        return roleMapper.selectEnabledByUserId(userId).stream()
                .map(SysRole::getCode)
                .toList();
    }

    private Long parseUserId(Object loginId) {
        if (loginId == null) {
            return null;
        }

        if (loginId instanceof Number number) {
            return number.longValue();
        }

        try {
            return Long.valueOf(loginId.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
