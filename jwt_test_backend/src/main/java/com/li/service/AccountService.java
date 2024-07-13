package com.li.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.li.entity.dto.Account;
import com.li.entity.vo.request.ConfirmResetVO;
import com.li.entity.vo.request.EmailRegisterVO;
import com.li.entity.vo.request.EmailResetVO;
import org.springframework.security.core.userdetails.UserDetailsService;



/*
*  UserDetailsService用户详细信息服务：提供从数据库或其他存储中加载用户信息的服务。
* */
public interface AccountService extends IService<Account>, UserDetailsService {
    // 根据用户名/邮箱账号获取用户
    Account findAccountByNameOrEmail(String text);
    // 发送邮件验证码
    String registerEmailVerifyCode(String type, String email, String address);
    // 注册邮件账户
    String registerEmailAccount(EmailRegisterVO info);

    String resetEmailAccountPassword(EmailResetVO info);
    String resetConfirm(ConfirmResetVO info);

}
