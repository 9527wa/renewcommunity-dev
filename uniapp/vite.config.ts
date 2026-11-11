import { defineConfig } from 'vite';
import uni from '@dcloudio/vite-plugin-uni';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [uni()],
  server: {
    port: 5173,
    proxy: {
      // H5 开发环境代理到后端，小程序端直接访问 BASE_URL
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
});
