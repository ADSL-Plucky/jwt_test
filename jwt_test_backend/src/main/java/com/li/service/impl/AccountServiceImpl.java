package com.li.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.li.entity.dto.Account;
import com.li.entity.dto.EmailProperties;
import com.li.entity.vo.request.EmailRegisterVO;
import com.li.mapper.AccountMapper;
import com.li.service.AccountService;
import com.li.utils.Const;
import com.li.utils.FlowUtils;
import com.li.utils.MailUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 账户信息处理相关服务
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Value("${spring.web.verify.mail-limit}")
    int verifyLimit;


    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    FlowUtils flow;

    @Resource
    EmailProperties emailProperties;

    @Resource
    PasswordEncoder passwordEncoder;

    /**
     * 移除Redis中存储的邮件验证码
     *
     * @param email 电邮
     */
    private void deleteEmailVerifyCode(String email) {
        String key = Const.VERIFY_EMAIL_DATA + email;
        stringRedisTemplate.delete(key);
    }

    /**
     * 获取Redis中存储的邮件验证码
     *
     * @param email 电邮
     * @return 验证码
     */
    private String getEmailVerifyCode(String email) {
        String key = Const.VERIFY_EMAIL_DATA + email;
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 针对IP地址进行邮件验证码获取限流
     *
     * @param address 地址
     * @return 是否通过验证
     */
    private boolean verifyLimit(String address) {
        String key = Const.VERIFY_EMAIL_LIMIT + address;
        return flow.limitOnceCheck(key, verifyLimit);
    }

    /**
     * 查询指定邮箱的用户是否已经存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    private boolean existsAccountByEmail(String email) {
        return this.baseMapper.exists(Wrappers.<Account>query().eq("email", email));
    }

    /**
     * 查询指定用户名的用户是否已经存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    private boolean existsAccountByUsername(String username) {
        return this.baseMapper.exists(Wrappers.<Account>query().eq("username", username));
    }

    @Override
    public Account findAccountByNameOrEmail(String text) {
        return this.query()
                .eq("username", text).or()
                .eq("email", text)
                .one();
    }

    /**
     * 生成注册验证码存入Redis中，并将邮件发送请求提交到消息队列等待发送
     *
     * @param type    类型
     * @param to      邮件地址
     * @param address 请求IP地址
     * @return 操作结果，null表示正常，否则为错误原因
     */
    @Override
    public String registerEmailVerifyCode(String type, String to, String address) {
        /*
         *
         * 通过 synchronized 块可以确保在同一时刻只有一个线程可以进入该块，从而防止并发修改带来的问题
         * address.intern() 是一个特殊的方法调用，它返回字符串的规范化表示（interned string）。具体作用如下：
         * 字符串常量池:
         * intern() 方法会查找字符串常量池中是否有与当前字符串值相等的字符串。如果有，则返回常量池中的字符串引用；如果没有，则将当前字符串添加到常量池中，并返回这个字符串的引用。
         * 统一对象锁:
         * 通过 address.intern() 返回的字符串引用，可以确保不同线程在同步时使用的是同一个锁对象。这就可以有效地解决多线程同时操作相同逻辑的字符串的问题。
         * */
        synchronized (address.intern()) {
            // 首先验证该地址是否请求过邮箱验证码
            if (!this.verifyLimit(address))
                return "请求频繁，请稍后再试";
            // 生成随机的六位数字当做验证码
            Random random = new Random();
            int code = random.nextInt(899999) + 100000;
            // 将发送的内容放入消息队列
            String title = null;
            String content = null;
            switch (type) {
                // 发送的是注册邮件
                case "register" -> {
                    title = "欢迎注册我们的网站";
                    content = "您的邮件注册验证码为: " + code + "，有效时间3分钟，为了保障您的账户安全，请勿向他人泄露验证码信息。";
                }
                // 发送的是重置密码邮件
                case "reset" -> {
                    title = "您的密码重置邮件";
                    content = "你好，您正在执行重置密码操作，验证码: " + code + "，有效时间3分钟，如非本人操作，请无视。";
                }
                default -> {
                    return "类型错误！";
                }
            }
            ;
            String result = MailUtil.sendMail(emailProperties, to, title, content);
            // 将生成的验证码和对应的邮箱、过期时间存入redis，用于验证，过期时间时间一般是三分钟
            stringRedisTemplate.opsForValue()
                    .set(Const.VERIFY_EMAIL_DATA + to, String.valueOf(code), 3, TimeUnit.MINUTES);
            return result;
        }

    }

    /**
     * 邮件验证码注册账号操作，需要检查验证码是否正确以及邮箱、用户名是否存在重名
     *
     * @param info 注册基本信息
     * @return 操作结果，null表示正常，否则为错误原因
     */
    public String registerEmailAccount(EmailRegisterVO info) {
        // 提交的用户名
        String username = info.getUsername();
        // 提交的邮件名
        String email = info.getEmail();
        // 获取Redis里面的验证码
        String code = this.getEmailVerifyCode(email);
        // 如果没有获取验证码
        if (code == null) return "请先获取验证码";
        // 传入的验证码与Redis获取到的验证码不一致
        if (!code.equals(info.getCode())) return "验证码错误，请重新输入";
        // 邮件已经存在
        if (this.existsAccountByEmail(email)) return "该邮件地址已被注册";
        // 用户名已经存在
        if (this.existsAccountByUsername(username)) return "该用户名已被他人使用，请重新更换";
        // 注册用户
        String password = passwordEncoder.encode(info.getPassword());
        Account account = new Account(null, username,email,
                password,  Const.ROLE_DEFAULT, new Date());
        if (!this.save(account)) {
            return "内部错误，注册失败";
        } else {
            this.deleteEmailVerifyCode(email);
            return null;
        }
    }

    /*
     *   这个方法重载了用户详细信息服务获取用户信息的途径，并将获取到的用户信息返回
     *   给予身份验证过滤器(UsernamePasswordAuthenticationFilter)中的身份验证管理器（AuthenticationManager）进行验证
     * */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.findAccountByNameOrEmail(username);
        if (account == null)
            throw new UsernameNotFoundException("用户名或密码错误");
        return User
                .withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }
}
