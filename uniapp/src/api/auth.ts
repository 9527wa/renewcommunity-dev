import { request } from '@/utils/request';

export interface LoginParams {
  userName: string;
  password: string;
}

export interface LoginResult {
  token: string;
}

export interface UserInfo {
  userId: string;
  userName: string;
  roles: string[];
  buttons: string[];
}

/** 登录 */
export function login(data: LoginParams) {
  return request<LoginResult>({
    url: '/auth/login',
    method: 'POST',
    data,
    auth: false
  });
}

/** 获取当前登录用户信息 */
export function getUserInfo() {
  return request<UserInfo>({ url: '/auth/getUserInfo' });
}

/** 退出登录 */
export function logout() {
  return request<void>({ url: '/auth/logout', method: 'POST' });
}
