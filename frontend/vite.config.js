import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        // Backend Spring Boot is running on port 8090 in this environment
        target: 'http://localhost:8090',
        changeOrigin: true,
        secure: false,
      }
    }
  }
});
