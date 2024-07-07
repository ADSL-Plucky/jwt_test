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
