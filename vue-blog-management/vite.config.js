import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
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
      // 通知WebSocket代理：手机/局域网访问时由开发服务器转发到后端
      '/ws': {
        target: 'http://localhost:8080',
        ws: true,
        changeOrigin: true,
      }
    }
  },
  optimizeDeps: {
    exclude: ['@vue/compiler-sfc']
  }
})
