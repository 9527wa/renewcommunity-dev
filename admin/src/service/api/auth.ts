import { request } from '../request';

/**
 * 登录
 *
 * @param userName 用户名
 * @param password 密码
 */
export function fetchLogin(userName: string, password: string) {
  return request<Api.Auth.LoginToken>({
    url: '/api/auth/login',
    method: 'post',
    data: {
      userName,
      password
    }
  });
}

/** 获取当前登录用户信息 */
export function fetchGetUserInfo() {
  return request<Api.Auth.UserInfo>({ url: '/api/auth/getUserInfo' });
}

/** 退出登录 */
export function fetchLogout() {
  return request<void>({ url: '/api/auth/logout', method: 'post' });
}
