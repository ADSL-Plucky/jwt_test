package com.li.config;


import com.auth0.jwt.JWT;
import com.li.entity.RestBean;
import com.li.entity.dto.Account;
import com.li.entity.vo.response.AuthorizeVO;
import com.li.filter.JwtAuthenticationFilter;
import com.li.service.AccountService;
import com.li.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfiguration {

    @Resource
    JwtUtils jwtUtils;

    @Resource
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Resource
    AccountService accountService;



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                // 请求验证配置
                .authorizeHttpRequests(conf -> conf
                        // 以下路径允许所有人都访问：登录api，404，接口文档
                        .requestMatchers("/api/auth/**", "/error").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        // 其余的都需要验证角色是否是user，是就放行，不是就报403
                        .anyRequest().authenticated()
                )
                // 登录验证配置
                .formLogin(conf -> conf
                        // 登录url
                        .loginProcessingUrl("/api/auth/login")
                        // 登录失败返回设置
                        .failureHandler(this::handleProcess)
                        // 登录成功返回设置
                        .successHandler(this::handleProcess)
                )
                // 登出配置
                .logout(conf -> conf
                        // 登出url
                        .logoutUrl("/api/auth/logout")
                        // 登出成功返回设置
                        .logoutSuccessHandler(this::onLogoutSuccess)
                )
                // 验证异常配置
                .exceptionHandling(conf->conf
                        // 权限为空返回设置
                        .authenticationEntryPoint(this::handleProcess)
                        // 没有权限设置
                        .accessDeniedHandler(this::handleProcess)
                )
                // 跨域配置 禁用 CSRF 保护
                .csrf(AbstractHttpConfigurer::disable)
                // session管理
                .sessionManagement(conf ->conf
                        // 无状态
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 将自定义的JWT验证过滤添加到框架里面的用户名及密码验证过滤器之前
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 构建安全配置
                .build();
    }

    /**
     * 将多种类型的Handler整合到同一个方法中，包含：
     * - 登录成功
     * - 登录失败
     * - 未登录拦截/无权限拦截
     * @param request 请求
     * @param response 响应
     * @param exceptionOrAuthentication 异常或是验证实体
     * @throws IOException 可能的异常
     */
    private void handleProcess(HttpServletRequest request,
                               HttpServletResponse response,
                               Object exceptionOrAuthentication) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        if(exceptionOrAuthentication instanceof AccessDeniedException exception) {
            writer.write(RestBean
                    .forbidden(exception.getMessage()).asJsonString());
        } else if(exceptionOrAuthentication instanceof Exception exception) {
            writer.write(RestBean
                    .unauthorized(exception.getMessage()).asJsonString());
        } else if(exceptionOrAuthentication instanceof Authentication authentication){
            /*
            * */
            User user = (User) authentication.getPrincipal();
            Account account = accountService.findAccountByNameOrEmail(user.getUsername());
            String token = jwtUtils.createJwt(user, account.getId(), account.getUsername());
            if(token == null) {
                writer.write(RestBean.forbidden("登录验证频繁，请稍后再试").asJsonString());
            } else {
                // 从将实体类里面的同名字段转化到Vo中
                AuthorizeVO vo = account.asViewObject(AuthorizeVO.class, v ->{
                    v.setToken(token);
                    v.setExpire(JWT.decode(token).getExpiresAt());
                });
                writer.write(RestBean.success(vo).asJsonString());
            }
        }
    }

    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        // 获取验证信息
        String authorization = request.getHeader("Authorization");
        // 将该令牌失效
        if(jwtUtils.invalidateJwt(authorization)) {
            // 如果失效成功
            writer.write(RestBean.success("退出登录成功").asJsonString());
            return;
        }
        // 如果失效失败
        writer.write(RestBean.failure(400, "退出登录失败").asJsonString());
    }
}
