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

export interface RegisterParams {
  username: string;
  password: string;
  nickname?: string;
  mobile?: string;
}

export interface WechatLoginParams {
  /** wx.login 获取的临时登录凭证 */
  code: string;
  nickname?: string;
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

/** 注册（成功后直接返回登录令牌） */
export function register(data: RegisterParams) {
  return request<LoginResult>({
    url: '/auth/register',
    method: 'POST',
    data,
    auth: false
  });
}

/** 微信小程序登录（未注册的 openid 会自动创建账号） */
export function wechatLogin(data: WechatLoginParams) {
  return request<LoginResult>({
    url: '/auth/wechat-login',
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
