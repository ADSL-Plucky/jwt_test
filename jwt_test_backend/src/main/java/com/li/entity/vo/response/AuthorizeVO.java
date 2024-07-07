package com.li.entity.vo.response;


import lombok.Data;

import java.util.Date;

/**
 * 登录验证成功的用户信息响应
 */
@Data
public class AuthorizeVO {
    // 用户名
    String username;
    // 用户权限
    String role;
    // token
    String token;
    // 过期时间
    Date expire;
}
