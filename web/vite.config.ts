import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // During development the page runs on :5173 and the Java API on :8080.
    // Vite forwards every /api/... request to Spring Boot, so the browser sees one site (no CORS setup needed).
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
