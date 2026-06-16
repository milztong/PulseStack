import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
  ],
  define: {
    global: 'globalThis',
  },
  build: {
    rollupOptions: {
      output: {
        // recharts + d3-* müssen als EIN Chunk gebündelt werden — wenn Rolldown sie
        // über mehrere Chunks verteilt (z.B. weil zwei lazy-loaded Components beide
        // recharts importieren), bricht die Modul-Initialisierungsreihenfolge auf
        // Linux-Builds (TDZ/Hoisting-Bug in d3-internals → "t is not a function").
        manualChunks(id) {
          if (id.includes('node_modules/recharts') || id.includes('node_modules/d3-')) {
            return 'charts';
          }
        },
      },
    },
  },
})
