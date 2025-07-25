import { defineConfig } from 'vite';

export default defineConfig({
  // Relative assets work both at localhost and under /Gebeta-Game/ on GitHub Pages.
  base: './',
  publicDir: '../src/main/resources',
  build: {
    outDir: 'dist',
    emptyOutDir: true,
  },
});
