import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'


// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
    server: {  // 新增服务器配置
  port: 8080,       // 设置监听端口
      host: '0.0.0.0',  // 允许局域网访问（可选）
      open: true        // 启动时自动打开浏览器（可选）
}
})
