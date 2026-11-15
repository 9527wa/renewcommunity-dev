import { request } from '../request';

/** get user list */
export function fetchGetUserList(params: Api.SystemManage.UserSearchParams) {
  return request<Api.SystemManage.UserList>({
    url: '/api/system/user/list',
    method: 'get',
    params
  });
}

/** create user */
export function fetchCreateUser(data: Api.SystemManage.UserSaveParams) {
  return request<number>({ url: '/api/system/user', method: 'post', data });
}

/** update user */
export function fetchUpdateUser(id: number, data: Api.SystemManage.UserUpdateParams) {
  return request<void>({ url: `/api/system/user/${id}`, method: 'put', data });
}

/** delete user */
export function fetchDeleteUser(id: number) {
  return request<void>({ url: `/api/system/user/${id}`, method: 'delete' });
}

/** reset user password */
export function fetchResetUserPassword(id: number, password: string) {
  return request<void>({
    url: `/api/system/user/${id}/password`,
    method: 'put',
    data: { password }
  });
}

/** get role list */
export function fetchGetRoleList() {
  return request<Api.SystemManage.Role[]>({ url: '/api/system/role/list' });
}

/** create role */
export function fetchCreateRole(data: Api.SystemManage.RoleSaveParams) {
  return request<number>({ url: '/api/system/role', method: 'post', data });
}

/** update role */
export function fetchUpdateRole(id: number, data: Api.SystemManage.RoleSaveParams) {
  return request<void>({ url: `/api/system/role/${id}`, method: 'put', data });
}

/** delete role */
export function fetchDeleteRole(id: number) {
  return request<void>({ url: `/api/system/role/${id}`, method: 'delete' });
}

/** get menu ids of role */
export function fetchGetRoleMenuIds(id: number) {
  return request<number[]>({ url: `/api/system/role/${id}/menus` });
}

/** assign menus to role */
export function fetchAssignRoleMenus(id: number, menuIds: number[]) {
  return request<void>({
    url: `/api/system/role/${id}/menus`,
    method: 'put',
    data: { menuIds }
  });
}

/** get menu tree */
export function fetchGetMenuTree() {
  return request<Api.SystemManage.Menu[]>({ url: '/api/system/menu/tree' });
}

/** create menu */
export function fetchCreateMenu(data: Api.SystemManage.MenuSaveParams) {
  return request<number>({ url: '/api/system/menu', method: 'post', data });
}

/** update menu */
export function fetchUpdateMenu(id: number, data: Api.SystemManage.MenuSaveParams) {
  return request<void>({ url: `/api/system/menu/${id}`, method: 'put', data });
}

/** delete menu */
export function fetchDeleteMenu(id: number) {
  return request<void>({ url: `/api/system/menu/${id}`, method: 'delete' });
}

/** get server runtime and memory status */
export function fetchGetServerStatus() {
  return request<Api.SystemManage.ServerStatus>({ url: '/api/system/monitor', method: 'get' });
}
