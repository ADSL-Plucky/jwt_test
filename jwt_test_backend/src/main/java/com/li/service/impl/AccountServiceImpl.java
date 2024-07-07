package com.li.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.li.entity.dto.Account;
import com.li.mapper.AccountMapper;
import com.li.service.AccountService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 账户信息处理相关服务
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {


    @Override
    public Account findAccountByNameOrEmail(String text) {
        return this.query()
                .eq("username", text).or()
                .eq("email", text)
                .one();
    }

    /*
    *   这个方法重载了用户详细信息服务获取用户信息的途径，并将获取到的用户信息返回
    *   给予身份验证过滤器(UsernamePasswordAuthenticationFilter)中的身份验证管理器（AuthenticationManager）进行验证
    * */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.findAccountByNameOrEmail(username);
        if(account == null)
            throw new UsernameNotFoundException("用户名或密码错误");
        return User
                .withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }
}
