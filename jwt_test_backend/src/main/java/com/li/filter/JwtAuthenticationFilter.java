package com.li.filter;


import com.auth0.jwt.interfaces.DecodedJWT;
import com.li.utils.Const;
import com.li.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 用于对请求头中Jwt令牌进行校验的工具，为当前请求添加用户验证信息
 * 并将用户的ID存放在请求对象属性中，方便后续使用
 */
// 继承了一次请求过滤，每一次请求触发一次
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    JwtUtils jwtUtils;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        // 把token进行解码
        DecodedJWT decodedJWT = jwtUtils.resolveJwt(authorization);
        if(decodedJWT != null) {
            // 获取到token里面的用户信息
            UserDetails user = jwtUtils.toUser(decodedJWT);
            // 用户名及密码验证
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            // 设置验证结果
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // 验证结果放入SecurityContextHolder中，表明验证通过
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // 将某信息放入请求以便获取使用
            request.setAttribute(Const.ATTR_USER_ID, jwtUtils.toId(decodedJWT));
        }
        // 如果是空，说明解析token失败，直接往下走让框架拦截
        filterChain.doFilter(request, response);
    }
}
