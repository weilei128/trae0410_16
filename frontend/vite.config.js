import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 10021,
    proxy: {
      '/api': {
        target: 'http://localhost:10011',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist_10021'
  }
})
