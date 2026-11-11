const TOKEN_KEY = 'renew_token';

/** 获取本地令牌 */
export function getToken(): string {
  return uni.getStorageSync(TOKEN_KEY) || '';
}

/** 保存令牌 */
export function setToken(token: string) {
  uni.setStorageSync(TOKEN_KEY, token);
}

/** 清除令牌 */
export function clearToken() {
  uni.removeStorageSync(TOKEN_KEY);
}

/** 是否已登录 */
export function isLoggedIn(): boolean {
  return Boolean(getToken());
}
