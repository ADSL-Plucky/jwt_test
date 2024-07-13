import request from '../request'

export function loginApi(username, password) {
    return request({
        url: '/api/auth/login',
        method: 'post',
        data: {
            username,
            password
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 退出系统
export function loginOutApi() {
    return request({
        url: '/api/auth/logout'
    })
}

// 注册账号
export function registerApi(username, password, email, code) {
    return request({
        url: '/api/auth/register',
        method: 'post',
        data: {
            username,
            password,
            email,
            code
        }
    })
}

// 获取邮箱验证码
export function askCodeApi(email, type) {
    return request({
        url: '/api/auth/ask-code',
        method: 'get',
        params: {
            email,
            type
        }
    })
}
//
export function verifyAccountApi(email) {
    return request({
        url: '/api/auth/verify-account',
        method: 'get',
        params: {
            email
        }
    })
}
// 提交邮箱验证码
export function resetConfirmApi(email, code) {
    return request({
        url: '/api/auth/reset-confirm',
        method: 'post',
        data: {
            email,
            code,
        }
    })
}

// 重置密码
export function resetPasswordApi(email, code, password) {
    return request({
        url: '/api/auth/reset-password',
        method: 'post',
        data: {
            email,
            code,
            password
        }
    })
}
