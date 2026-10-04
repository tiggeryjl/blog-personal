import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  // 必须用绝对根路径：相对路径('./')会让 /article/xx、/rss/unsubscribe 这类深链接
  // 把资源解析成 /article/src/main.js 从而整页白屏。
  // 如果前端部署在子路径下（例如 https://域名/blog/），这里要改成 '/blog/'。
  base: '/',
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        secure: false,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
      // 用户通知WebSocket代理，支持本机和局域网开发访问
      '/ws': {
        target: 'http://localhost:8080',
        ws: true,
        changeOrigin: true,
      },
    },
  },
  optimizeDeps: {
    exclude: ['@vue/compiler-sfc'],
  },
})
