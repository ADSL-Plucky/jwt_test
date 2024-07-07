package com.li.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用于处理Jwt令牌的工具类
 */
@Component
public class JwtUtils {

    //用于给Jwt令牌签名校验的秘钥
    @Value("${spring.security.jwt.privateKey}")
    private String privateKey;

    //令牌的过期时间，以分钟为单位
    @Value("${spring.security.jwt.expire}")
    private int expire;

    @Resource
    StringRedisTemplate template;
    /**
     * 让指定Jwt令牌失效
     * @param headerToken 请求头中携带的令牌
     * @return 是否操作成功
     */
    public boolean invalidateJwt(String headerToken){
        String token = this.convertToken(headerToken);
        Algorithm algorithm = Algorithm.HMAC256(privateKey);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            DecodedJWT verify = jwtVerifier.verify(token);
            return deleteToken(verify.getId(), verify.getExpiresAt());
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * 根据UserDetails生成对应的Jwt令牌
     * @param user 用户信息
     * @return 令牌
     */
    public String createJwt(UserDetails user,int userId,String username){
        // 使用HMAC256算法和密钥进行签名
        Algorithm algorithm = Algorithm.HMAC256(privateKey);
        Date expire = this.expireTime();
        return JWT.create()
                // 添加jwtID，用于登出后区别哪个jwt已被废弃
                .withJWTId(UUID.randomUUID().toString())
                // 添加自定义的声明，需要时在应用程序中进行访问和验证
                .withClaim("id",userId)
                .withClaim("name",username)
                .withClaim("authorities",user.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority).toList())
                // 设置过期时间
                .withExpiresAt(expire)
                // 设置签发时间
                .withIssuedAt(new Date())
                // 设置算法
                .sign(algorithm);
    }


    /**
     * 解析Jwt令牌
     * @param headerToken 请求头中携带的令牌
     * @return DecodedJWT
     */
    public DecodedJWT resolveJwt(String headerToken){
        String token = this.convertToken(headerToken);
        if(token == null) return null;
        Algorithm algorithm = Algorithm.HMAC256(privateKey);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            // 验证当前token是否合法，如果不合法会抛出异常
            DecodedJWT verify = jwtVerifier.verify(token);
            //  验证令牌是否失效
            if(this.isInvalidToken(verify.getId())) return null;
            // 验证是否过期
            Date expiresAt = verify.getExpiresAt();
//            Map<String, Claim> claims = verify.getClaims();
            return new Date().after(expiresAt) ? null : verify;
        } catch (JWTVerificationException e) {
            // 验证失败
            return null;
        }
    }

    /**
     * 将jwt对象中的内容封装为UserDetails
     * @param decodedJWT 已解析的Jwt对象
     * @return UserDetails
     */
    public UserDetails toUser(DecodedJWT decodedJWT) {
        // 获取jwt中的声明，键值对和creatJwt中withClaim()的一致
        Map<String, Claim> claims = decodedJWT.getClaims();
        return User
                .withUsername(claims.get("name").asString())
                .password("******")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
    }

    /**
     * 将jwt对象中的用户ID提取出来
     * @param jwt 已解析的Jwt对象
     * @return 用户ID
     */
    public Integer toId(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims();
        return claims.get("id").asInt();
    }

    /**
     * 根据配置快速计算过期时间
     * @return 过期时间
     */
    private Date expireTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE ,expire);
        return calendar.getTime();
    }

    /**
     * 校验并转换请求头中的Token令牌
     * @param headerToken 请求头中的Token
     * @return 转换后的令牌
     */
    private String convertToken(String headerToken){
        // 判断token是否合法
        if(headerToken == null || !headerToken.startsWith("Bearer "))
            return null;
        return headerToken.substring(7);
    }

    /**
     * 将Token列入Redis黑名单中
     * @param uuid 令牌ID
     * @param time 过期时间
     * @return 是否操作成功
     */
    private boolean deleteToken(String uuid, Date time){
        // 验证是否失效
        if(this.isInvalidToken(uuid))
            return false;
        Date now = new Date();
        // 设置存储过期时间，即在黑名单中存储时间为该令牌剩余时间
        long expire = Math.max(time.getTime() - now.getTime(), 0);
        // 结果存入黑名单中，不管是否过期
        template.opsForValue().set(Const.JWT_BLACK_LIST + uuid, "", expire, TimeUnit.MILLISECONDS);
        return true;
    }

    /**
     * 验证Token是否被列入Redis黑名单
     * @param uuid 令牌ID
     * @return 是否操作成功
     */
    private boolean isInvalidToken(String uuid){
        return Boolean.TRUE.equals(template.hasKey(Const.JWT_BLACK_LIST + uuid));
    }
}