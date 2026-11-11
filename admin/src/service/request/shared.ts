import { localStg } from '@/utils/storage';
import type { RequestInstanceState } from './type';

/** Sa-Token 请求头名称，需与后端 sa-token.token-name 保持一致 */
export const TOKEN_HEADER = 'satoken';

/** 获取本地保存的令牌 */
export function getToken() {
  return localStg.get('token') || '';
}

export function showErrorMsg(state: RequestInstanceState, message: string) {
  if (!state.errMsgStack?.length) {
    state.errMsgStack = [];
  }

  const isExist = state.errMsgStack.includes(message);

  if (!isExist) {
    state.errMsgStack.push(message);

    window.$message?.error(message, {
      onLeave: () => {
        state.errMsgStack = state.errMsgStack.filter(msg => msg !== message);

        setTimeout(() => {
          state.errMsgStack = [];
        }, 5000);
      }
    });
  }
}
