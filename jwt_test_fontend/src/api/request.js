import axios from 'axios'
import {ElMessage} from "element-plus";
import {takeAccessToken} from "@/utils/accessTokenUtils.js";
const request  = axios.create({
    baseURL: import.meta.env.VITE_APP_BASE_API,
    timeout: 3000000,
    // 跨域时候允许携带凭证
    withCredentials: true,
})
// 定义需要排除的 URL 数组
const excludedUrls = [
    '/api/auth/login',
    '/api/auth/register',
    '/api/auth/forgot-password'
];
//添加请求和响应拦截器
// 添加请求拦截器
request.interceptors.request.use(
     (config)  =>{
        // 在发送请求之前做些什么
         if (!excludedUrls.includes(config.url) && takeAccessToken()) {
             console.log(excludedUrls.includes(config.url))
             config.headers.Authorization = `Bearer ${takeAccessToken ()}`;
         }
        return config;
    },
     (error) =>{
        // 对请求错误做些什么
        return Promise.reject(error);
    }
);

// 添加响应拦截器
request.interceptors.response.use(
     (response) =>{
        // 对响应数据做点什么
        return response.data;
    },
     (error) =>{
         // 对响应错误做些什么
         console.error('Request failed:', {
             url: error.config.url,
             status: error.response,
             data: error.config.data,
         });

         // Show error notification
         ElMessage.error('请求出错，请联系管理员');
         return Promise.reject(error)
    }
);



export default request
