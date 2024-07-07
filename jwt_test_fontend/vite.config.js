import path from 'path'
import {defineConfig} from 'vite'
import Vue from '@vitejs/plugin-vue'
// Element 图标自动导入
import Icons from 'unplugin-icons/vite'
import IconsResolver from 'unplugin-icons/resolver'
// Element 组件自动导入
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import {ElementPlusResolver} from 'unplugin-vue-components/resolvers'

const pathSrc = path.resolve(__dirname, 'src')

export default defineConfig({
    resolve: {
        alias: {
            '@': pathSrc,
        },
    },
    plugins: [
        Vue(),
        AutoImport({
            // Auto import functions from Vue, e.g. ref, reactive, toRef...
            // 自动导入 Vue 相关函数，如：ref, reactive, toRef 等
            imports: ['vue'],

            // Auto import functions from Element Plus, e.g. ElMessage, ElMessageBox... (with style)
            // 自动导入 Element Plus 相关函数，如：ElMessage, ElMessageBox... (带样式)
            resolvers: [
                ElementPlusResolver(),

                // Auto import icon components
                // 自动导入图标组件
                IconsResolver({
                    prefix: 'Icon',
                }),
            ],
        }),

        Components({
            resolvers: [
                // Auto register icon components
                // 自动注册图标组件
                IconsResolver(
                    {
                        // 修改Icon组件前缀，不设置则默认为i,禁用则设置为false
                        prefix: false,
                        // 指定collection，即指定为elementplus图标集ep
                        enabledCollections: ['ep']
                    }
                ),
                // Auto register Element Plus components
                // 自动导入 Element Plus 组件
                ElementPlusResolver(),
            ],
        }),

        Icons({
            autoInstall: true,
        }),
    ],
    server: {
        host: 'localhost',//ip地址
        port: 5173, // 设置服务启动端口号
        proxy: {
            // 配置跨域代理
            '/api': {
                target: 'http://localhost:8080', // 要访问的后端服务地址
                changeOrigin: true,
            },
        },
    }
})