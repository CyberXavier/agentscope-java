import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
  },
  server: {
    proxy: {
      '/api': {
        // 代理到 Spring Boot 后端实际端口（见 application.yml 的 server.port）
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
    },
  },
});
