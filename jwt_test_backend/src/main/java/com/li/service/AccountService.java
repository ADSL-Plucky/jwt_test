package com.li.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.li.entity.dto.Account;
import org.springframework.security.core.userdetails.UserDetailsService;



/*
*  UserDetailsService用户详细信息服务：提供从数据库或其他存储中加载用户信息的服务。
* */
public interface AccountService extends IService<Account>, UserDetailsService {
    Account findAccountByNameOrEmail(String text);
}
