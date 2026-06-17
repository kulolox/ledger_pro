import { defineConfig } from "vite";
import uni from "@dcloudio/vite-plugin-uni";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [uni()],
  optimizeDeps: {
    include: ["echarts"],
  },
  server: {
    proxy: {
      "/api": {
        // target: "http://127.0.0.1", // prod
        target: "http://localhost:8080", // dev
        changeOrigin: true,
      },
    },
  },
  css: {
    preprocessorOptions: {
      scss: {
        api: "modern-compiler", // 启用现代编译器 API
        silenceDeprecations: ["legacy-js-api"], // 可选：强制静默该弃用警告
      },
      // 若项目里有纯 .sass（缩进语法），可同样配一份：
      // sass: { api: "modern" },
    },
  },
});
