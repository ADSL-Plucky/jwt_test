package com.li.config;


import com.li.entity.RestBean;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;


import java.io.IOException;

@Configuration
public class SecurityConfiguration {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                // 请求验证配置
                .authorizeHttpRequests(conf -> conf
                        // 只允许下列接口进入
                        .requestMatchers("/api/auth/**").permitAll()
                        // 其余的都需要验证之后才可以放行
                        .anyRequest().authenticated()
                )
                // 登录验证配置
                .formLogin(conf -> conf
                        // 登录url
                        .loginProcessingUrl("/api/auth/login")
                        // 登录失败返回设置
                        .failureHandler(this::onAuthenticationFailure)
                        // 登录成功返回设置
                        .successHandler(this::onAuthenticationSuccess)
                )
                // 登出配置
                .logout(conf -> conf
                        // 登出url
                        .logoutUrl("/api/auth/logout")
                        // 登出成功返回设置
                        .logoutSuccessHandler(this::onLogoutSuccess)
                )
                // 跨域配置 禁用 CSRF 保护
                .csrf(AbstractHttpConfigurer::disable)
                // session管理
                .sessionManagement(conf ->conf
                        // 无状态
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                )
                .build();
    }

    // 登录成功返回的设置方法
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(RestBean.success().asJsonString());
    }

    // 登录失败返回的验证方法
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(RestBean.failure(401, exception.getMessage()).asJsonString());
    }

    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {

    }
}
