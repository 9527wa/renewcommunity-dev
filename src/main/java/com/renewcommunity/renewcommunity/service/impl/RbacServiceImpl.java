package com.renewcommunity.renewcommunity.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.ResultCode;
import com.renewcommunity.renewcommunity.common.exception.BusinessException;
import com.renewcommunity.renewcommunity.entity.SysMenu;
import com.renewcommunity.renewcommunity.entity.SysRole;
import com.renewcommunity.renewcommunity.entity.SysRoleMenu;
import com.renewcommunity.renewcommunity.entity.SysUser;
import com.renewcommunity.renewcommunity.entity.SysUserRole;
import com.renewcommunity.renewcommunity.mapper.SysMenuMapper;
import com.renewcommunity.renewcommunity.mapper.SysRoleMapper;
import com.renewcommunity.renewcommunity.mapper.SysRoleMenuMapper;
import com.renewcommunity.renewcommunity.mapper.SysUserMapper;
import com.renewcommunity.renewcommunity.mapper.SysUserRoleMapper;
import com.renewcommunity.renewcommunity.model.dto.MenuSaveRequest;
import com.renewcommunity.renewcommunity.model.dto.PasswordResetRequest;
import com.renewcommunity.renewcommunity.model.dto.RoleSaveRequest;
import com.renewcommunity.renewcommunity.model.dto.UserSaveRequest;
import com.renewcommunity.renewcommunity.model.dto.UserUpdateRequest;
import com.renewcommunity.renewcommunity.model.vo.MenuVO;
import com.renewcommunity.renewcommunity.model.vo.RoleVO;
import com.renewcommunity.renewcommunity.model.vo.UserVO;
import com.renewcommunity.renewcommunity.service.RbacService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * RBAC 权限管理实现。
 */
@Service
@RequiredArgsConstructor
public class RbacServiceImpl implements RbacService {

    private static final long BUILTIN_ADMIN_ID = 1L;

    private static final long BUILTIN_SUPER_ROLE_ID = 1L;

    private final SysUserMapper userMapper;

    private final SysRoleMapper roleMapper;

    private final SysMenuMapper menuMapper;

    private final SysUserRoleMapper userRoleMapper;

    private final SysRoleMenuMapper roleMenuMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<UserVO> listUsers(String keyword, int pageNum, int pageSize) {
        Page<SysUser> page = new Page<>(Math.max(pageNum, 1), Math.min(Math.max(pageSize, 1), 200));
        var query = Wrappers.<SysUser>lambdaQuery();

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper.like(SysUser::getUsername, keyword)
                    .or()
                    .like(SysUser::getNickname, keyword));
        }

        query.orderByAsc(SysUser::getId);
        Page<SysUser> result = userMapper.selectPage(page, query);

        List<UserVO> list = result.getRecords().stream().map(this::toUserVO).toList();

        return new PageResult<>(result.getTotal(), list);
    }

    @Override
    @Transactional
    public Long createUser(UserSaveRequest request) {
        long count = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, request.username()));
        if (count > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setNickname(StringUtils.hasText(request.nickname()) ? request.nickname() : request.username());
        user.setMobile(request.mobile());
        user.setEmail(request.email());
        user.setStatus(normalizeStatus(request.status()));
        userMapper.insert(user);

        saveUserRoles(user.getId(), request.roleIds());

        return user.getId();
    }

    @Override
    @Transactional
    public void updateUser(Long id, UserUpdateRequest request) {
        requireUser(id);

        if (id == BUILTIN_ADMIN_ID && Objects.equals(request.status(), 0)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "内置管理员不能停用");
        }

        SysUser user = new SysUser();
        user.setId(id);
        user.setNickname(request.nickname());
        user.setMobile(request.mobile());
        user.setEmail(request.email());
        user.setStatus(request.status() == null ? null : normalizeStatus(request.status()));
        userMapper.updateById(user);

        if (request.roleIds() != null) {
            userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, id));
            saveUserRoles(id, request.roleIds());
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (id == BUILTIN_ADMIN_ID) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "内置管理员不能删除");
        }

        if (Objects.equals(id, StpUtil.getLoginIdAsLong())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不能删除当前登录用户");
        }

        requireUser(id);
        userMapper.deleteById(id);
        userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, id));
    }

    @Override
    public void resetPassword(Long id, PasswordResetRequest request) {
        requireUser(id);

        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(passwordEncoder.encode(request.password()));
        userMapper.updateById(user);
    }

    @Override
    public List<RoleVO> listRoles() {
        List<SysRole> roles = roleMapper.selectList(Wrappers.<SysRole>lambdaQuery()
                .orderByAsc(SysRole::getSort)
                .orderByAsc(SysRole::getId));

        return roles.stream().map(this::toRoleVO).toList();
    }

    @Override
    @Transactional
    public Long createRole(RoleSaveRequest request) {
        long count = roleMapper.selectCount(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getCode, request.code()));
        if (count > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "角色标识已存在");
        }

        SysRole role = new SysRole();
        role.setCode(request.code());
        role.setName(request.name());
        role.setSort(request.sort() == null ? 0 : request.sort());
        role.setStatus(normalizeStatus(request.status()));
        role.setRemark(request.remark());
        roleMapper.insert(role);

        saveRoleMenus(role.getId(), request.menuIds());

        return role.getId();
    }

    @Override
    @Transactional
    public void updateRole(Long id, RoleSaveRequest request) {
        requireRole(id);

        long count = roleMapper.selectCount(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getCode, request.code())
                .ne(SysRole::getId, id));
        if (count > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "角色标识已存在");
        }

        SysRole role = new SysRole();
        role.setId(id);
        role.setCode(request.code());
        role.setName(request.name());
        role.setSort(request.sort());
        role.setStatus(request.status() == null ? null : normalizeStatus(request.status()));
        role.setRemark(request.remark());
        roleMapper.updateById(role);

        if (request.menuIds() != null) {
            roleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, id));
            saveRoleMenus(id, request.menuIds());
        }
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        if (id == BUILTIN_SUPER_ROLE_ID) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "内置超级管理员角色不能删除");
        }

        requireRole(id);

        long userCount = userRoleMapper.selectCount(Wrappers.<SysUserRole>lambdaQuery()
                .eq(SysUserRole::getRoleId, id));
        if (userCount > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该角色已分配给用户，无法删除");
        }

        roleMapper.deleteById(id);
        roleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, id));
    }

    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        requireRole(roleId);

        return roleMenuMapper.selectList(Wrappers.<SysRoleMenu>lambdaQuery()
                        .eq(SysRoleMenu::getRoleId, roleId))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .toList();
    }

    @Override
    @Transactional
    public void assignRoleMenus(Long roleId, List<Long> menuIds) {
        requireRole(roleId);

        roleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, roleId));
        saveRoleMenus(roleId, menuIds);
    }

    @Override
    public List<MenuVO> getMenuTree() {
        List<SysMenu> menus = menuMapper.selectList(Wrappers.<SysMenu>lambdaQuery()
                .orderByAsc(SysMenu::getSort)
                .orderByAsc(SysMenu::getId));

        Map<Long, MenuVO> nodes = new LinkedHashMap<>();
        menus.forEach(menu -> nodes.put(menu.getId(), new MenuVO(menu)));

        List<MenuVO> roots = new ArrayList<>();
        nodes.values().forEach(node -> {
            MenuVO parent = nodes.get(node.getParentId());

            if (node.getParentId() == null || node.getParentId() == 0 || parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        });

        return roots;
    }

    @Override
    public Long createMenu(MenuSaveRequest request) {
        validateMenuType(request.type());
        validateParent(request.parentId(), null);

        SysMenu menu = new SysMenu();
        applyMenu(menu, request);
        menuMapper.insert(menu);

        return menu.getId();
    }

    @Override
    public void updateMenu(Long id, MenuSaveRequest request) {
        requireMenu(id);
        validateMenuType(request.type());
        validateParent(request.parentId(), id);

        SysMenu menu = new SysMenu();
        menu.setId(id);
        applyMenu(menu, request);
        menuMapper.updateById(menu);
    }

    @Override
    @Transactional
    public void deleteMenu(Long id) {
        requireMenu(id);

        long children = menuMapper.selectCount(Wrappers.<SysMenu>lambdaQuery().eq(SysMenu::getParentId, id));
        if (children > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "存在子菜单，无法删除");
        }

        menuMapper.deleteById(id);
        roleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getMenuId, id));
    }

    private UserVO toUserVO(SysUser user) {
        List<Long> roleIds = userRoleMapper.selectList(Wrappers.<SysUserRole>lambdaQuery()
                        .eq(SysUserRole::getUserId, user.getId()))
                .stream()
                .map(SysUserRole::getRoleId)
                .toList();

        return new UserVO(user.getId(), user.getUsername(), user.getNickname(), user.getMobile(),
                user.getEmail(), user.getStatus(), roleIds);
    }

    private RoleVO toRoleVO(SysRole role) {
        List<Long> menuIds = roleMenuMapper.selectList(Wrappers.<SysRoleMenu>lambdaQuery()
                        .eq(SysRoleMenu::getRoleId, role.getId()))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .toList();

        return new RoleVO(role.getId(), role.getCode(), role.getName(), role.getSort(), role.getStatus(),
                role.getRemark(), menuIds);
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }

        roleIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .forEach(roleId -> userRoleMapper.insert(new SysUserRole(userId, roleId)));
    }

    private void saveRoleMenus(Long roleId, List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }

        menuIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .forEach(menuId -> roleMenuMapper.insert(new SysRoleMenu(roleId, menuId)));
    }

    private void applyMenu(SysMenu menu, MenuSaveRequest request) {
        menu.setParentId(request.parentId() == null ? 0L : request.parentId());
        menu.setName(request.name());
        menu.setType(request.type());
        menu.setPath(request.path() == null ? "" : request.path());
        menu.setComponent(request.component() == null ? "" : request.component());
        menu.setPermission(request.permission() == null ? "" : request.permission());
        menu.setIcon(request.icon() == null ? "" : request.icon());
        menu.setSort(request.sort() == null ? 0 : request.sort());
        menu.setStatus(normalizeStatus(request.status()));
    }

    private void validateMenuType(String type) {
        if (!List.of("M", "C", "B").contains(type)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "菜单类型只能是 M、C、B");
        }
    }

    private void validateParent(Long parentId, Long selfId) {
        if (parentId == null || parentId == 0) {
            return;
        }

        if (Objects.equals(parentId, selfId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上级菜单不能是自己");
        }

        SysMenu parent = menuMapper.selectById(parentId);
        if (parent == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上级菜单不存在");
        }

        if ("B".equals(parent.getType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "按钮不能作为上级菜单");
        }
    }

    private void requireUser(Long id) {
        if (id == null || userMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
    }

    private void requireRole(Long id) {
        if (id == null || roleMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "角色不存在");
        }
    }

    private void requireMenu(Long id) {
        if (id == null || menuMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "菜单不存在");
        }
    }

    private Integer normalizeStatus(Integer status) {
        return Integer.valueOf(0).equals(status) ? 0 : 1;
    }
}
