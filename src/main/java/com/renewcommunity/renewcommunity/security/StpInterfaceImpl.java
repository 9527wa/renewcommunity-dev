package com.renewcommunity.renewcommunity.security;

import cn.dev33.satoken.stp.StpInterface;
import com.renewcommunity.renewcommunity.config.SecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限数据来源。
 *
 * <p>当前基于配置返回角色，接入用户/权限表后改为按 loginId 查询数据库。</p>
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SecurityProperties securityProperties;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return List.of();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return securityProperties.roles();
    }
}
