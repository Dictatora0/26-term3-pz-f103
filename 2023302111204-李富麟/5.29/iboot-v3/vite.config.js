const path = require('path');
import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
// https://github.com/vuejs/jsx-next#syntax
import vueJsx from '@vitejs/plugin-vue-jsx'

// https://vitejs.dev/config/
export default defineConfig((env)=>{
  return {
    plugins: [
      vue(),
      vueJsx(),
    ],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
        '@msn': path.resolve(__dirname, './src/views')
      }
    },
    server: {
      proxy: {
        '^/api/*': {
          changeOrigin: true,
          // 线上测试地址, 只支持浏览数据
          // target: 'http://iot.iteaj.com/api',
          // 本地开发地址, 需要运行对应的java后端[https://gitee.com/iteaj/iboot]
          target: 'http://localhost:8085',
          rewrite: (path) => {
            return path.replace(/^\/api/, '')
          }
        },
      },
      cors: true,
      host: '0.0.0.0'
    },
    optimizeDeps: {
      include: [],
    },
    build: {
      rollupOptions: {
        // external: ['ant-design-vue'],
        // output: {
        //   format: 'module',
        //   paths: {
        //     'ant-design-vue': 'http://localhost'
        //   }
        // }
      },
      sourcemap: false,
      cssCodeSplit: true
    }
  }
})
