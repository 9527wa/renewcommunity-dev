package com.renewcommunity.renewcommunity.service;

import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.model.dto.MenuSaveRequest;
import com.renewcommunity.renewcommunity.model.dto.PasswordResetRequest;
import com.renewcommunity.renewcommunity.model.dto.RoleSaveRequest;
import com.renewcommunity.renewcommunity.model.dto.UserSaveRequest;
import com.renewcommunity.renewcommunity.model.dto.UserUpdateRequest;
import com.renewcommunity.renewcommunity.model.vo.MenuVO;
import com.renewcommunity.renewcommunity.model.vo.RoleVO;
import com.renewcommunity.renewcommunity.model.vo.UserVO;

import java.util.List;

/**
 * RBAC 权限管理服务。
 */
public interface RbacService {

    PageResult<UserVO> listUsers(String keyword, int pageNum, int pageSize);

    Long createUser(UserSaveRequest request);

    void updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);

    void resetPassword(Long id, PasswordResetRequest request);

    List<RoleVO> listRoles();

    Long createRole(RoleSaveRequest request);

    void updateRole(Long id, RoleSaveRequest request);

    void deleteRole(Long id);

    List<Long> getRoleMenuIds(Long roleId);

    void assignRoleMenus(Long roleId, List<Long> menuIds);

    List<MenuVO> getMenuTree();

    Long createMenu(MenuSaveRequest request);

    void updateMenu(Long id, MenuSaveRequest request);

    void deleteMenu(Long id);
}
