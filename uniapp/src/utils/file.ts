import { getToken } from './auth';
import { BASE_URL } from './request';

/**
 * 通过带鉴权的 downloadFile 获取图片临时路径（H5 / 小程序通用）。
 */
export function downloadAuthImage(fileId: number): Promise<string> {
  return new Promise((resolve, reject) => {
    uni.downloadFile({
      url: `${BASE_URL}/app/file/${fileId}`,
      header: { satoken: getToken() },
      success: res => {
        if (res.statusCode === 200) {
          resolve(res.tempFilePath);
          return;
        }

        reject(new Error(`HTTP ${res.statusCode}`));
      },
      fail: reject
    });
  });
}
