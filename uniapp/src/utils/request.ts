import { clearToken, getToken } from './auth';

/** 后端统一响应体 */
export interface ApiResult<T> {
  code: number;
  msg: string;
  data: T;
}

export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE';

export interface RequestOptions {
  /** 接口路径，例如 /auth/login */
  url: string;
  method?: HttpMethod;
  data?: Record<string, any>;
  /** 是否携带 token，默认 true */
  auth?: boolean;
}

/** 与后端 Sa-Token 的 token-name 保持一致 */
const TOKEN_HEADER = 'satoken';

const platform = uni.getSystemInfoSync().uniPlatform;

/** H5 走 Vite 代理，小程序直接访问后端地址 */
const DEFAULT_BASE_URL = platform === 'web' ? '/api' : 'http://localhost:8080/api';

/** 可通过 .env 中的 VITE_API_BASE_URL 覆盖 */
export const BASE_URL = import.meta.env.VITE_API_BASE_URL || DEFAULT_BASE_URL;

/**
 * 统一请求封装：自动携带 token、处理业务错误码与登录失效跳转。
 */
export function request<T>(options: RequestOptions): Promise<T> {
  return new Promise((resolve, reject) => {
    const token = getToken();
    const header: Record<string, string> = {};

    if (token && options.auth !== false) {
      header[TOKEN_HEADER] = token;
    }

    uni.request({
      url: `${BASE_URL}${options.url}`,
      method: options.method || 'GET',
      data: options.data,
      header,
      success: res => {
        const body = res.data as ApiResult<T>;

        if (res.statusCode !== 200 || !body) {
          uni.showToast({ title: `网络异常(${res.statusCode})`, icon: 'none' });
          reject(new Error(`HTTP ${res.statusCode}`));
          return;
        }

        if (body.code === 0) {
          resolve(body.data);
          return;
        }

        if (body.code === 401) {
          clearToken();
          uni.reLaunch({ url: '/pages/login/login' });
        }

        uni.showToast({ title: body.msg || '请求失败', icon: 'none' });
        reject(new Error(body.msg || '请求失败'));
      },
      fail: err => {
        uni.showToast({ title: '网络连接失败', icon: 'none' });
        reject(err);
      }
    });
  });
}
