package com.blog.service.impl;

import com.blog.properties.JwtProperties;
import com.blog.service.JwtService;
import com.blog.utils.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JwtServiceImpl implements JwtService {

    /**
     * 未勾选"记住我"时刷新令牌的有效期：12 小时
     * （Cookie 本身是会话级，关闭浏览器即失效，这里再收窄一层令牌有效期）
     */
    private static final long SESSION_REFRESH_TTL_MILLIS = 12 * 60 * 60 * 1000L;

    @Autowired
    private JwtProperties jwtProperties;

    // 生成token
    @Override
    public String createToken(Long userId,String username) {
        return JwtUtil.generateToken(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                userId,username);
    }

    @Override
    public String createRefreshToken(Long userId, boolean remember) {
        long ttl = remember ? jwtProperties.getUserRefreshTtl() : SESSION_REFRESH_TTL_MILLIS;
        return JwtUtil.createRefreshToken(
                jwtProperties.getUserSecretKey(),
                ttl,
                userId);
    }

    // 获取用户ID
    @Override
    public Long getUserId(String token) {
        return JwtUtil.getUserIdByToken(jwtProperties.getUserSecretKey(),token);
    }

    // 从refreshToken获取用户ID
    @Override
    public Long getUserIdFromRefreshToken(String token) {
        return JwtUtil.getUserIdFromRefreshToken(jwtProperties.getUserSecretKey(),token);
    }

    //获取用户名
    @Override
    public String getUsername(String token) {
        return JwtUtil.getUsernameByToken(jwtProperties.getUserSecretKey(),token);
    }

    // 校验token
    @Override
    public boolean validateToken(String token) {
        return JwtUtil.validateToken(jwtProperties.getUserSecretKey(),token);
    }

    @Override
    public void setAdminRefreshCookie(String refreshToken, boolean remember, HttpServletResponse response) {
        JwtUtil.setAdminRefreshCookie(refreshToken, cookieMaxAgeSeconds(remember), response);
    }

    @Override
    public void clearAdminRefreshCookie(HttpServletResponse response) {
        JwtUtil.clearAdminRefreshCookie(response);
    }

    @Override
    public void setUserRefreshCookie(String refreshToken, boolean remember, HttpServletResponse response) {
        JwtUtil.setUserRefreshCookie(refreshToken, cookieMaxAgeSeconds(remember), response);
    }

    @Override
    public void clearUserRefreshCookie(HttpServletResponse response) {
        JwtUtil.clearUserRefreshCookie(response);
    }

    /**
     * 勾选"记住我"返回 Cookie 的 Max-Age（秒），与刷新令牌有效期保持一致；
     * 未勾选返回 0，此时不写 Max-Age，浏览器按会话Cookie处理
     *
     * @param remember 是否勾选记住我
     * @return Cookie 有效期（秒）
     */
    private long cookieMaxAgeSeconds(boolean remember) {
        return remember ? jwtProperties.getUserRefreshTtl() / 1000 : 0L;
    }
}
