import {createRouter, createWebHistory} from 'vue-router'
import {takeAccessToken} from "@/utils/accessTokenUtils.js";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'welcome',
            component: () => import('@/views/WelcomeView.vue'),
            children: [
                {
                    path: '',
                    name: 'welcome-login',
                    component: () => import('@/views/welcome/LoginPage.vue')
                }, {
                    path: 'register',
                    name: 'welcome-register',
                    component: () => import('@/views/welcome/RegisterPage.vue')
                }, {
                    path: 'forget',
                    name: 'welcome-forget',
                    component: () => import('@/views/welcome/ForgetPage.vue')
                }
            ]
        }, {
            path: '/index',
            name: 'index',
            meta: {requiresAuth: true},
            component: () => import('@/views/IndexView.vue'),
        }
    ]
})

router.beforeEach((to, from, next) => {
    const isUnauthorized = !takeAccessToken();
    // 如果用户未登录，且目标路由不是欢迎界面相关的路由，则重定向到欢迎页
    if (isUnauthorized && to.name && !to.name.startsWith('welcome')) {
        next('/');
    }
    // 如果用户已登录，且目标路由是欢迎界面相关的路由，则重定向到主页
    else if (!isUnauthorized  && to.name && to.name.startsWith('welcome')) {
        next('/index');
    }
    // 如果目标路由不存在
    else if ( to.matched.length === 0) {
        if(!isUnauthorized){
            // 用户已登录，则重定向到主页
            next('/index');
        }else{
            // 用户未登录，则重定向到欢迎界面
            next('/');
        }
    }
    // 如果目标路由存在或匹配，则继续导航
    else {
        next();
    }
})

export default router
// 登录:true | false
// 目标界面: welcome-* | index | order
/*
* 1.login = false and router != welcome-* => welcome
* 2.login = false and router == welcome-* => welcome-*   => next()
* 3.login = true and router != index => index
* 4.login = true and router = index => index => next()
* */