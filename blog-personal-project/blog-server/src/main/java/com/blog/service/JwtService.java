package com.blog.service;

import jakarta.servlet.http.HttpServletResponse;

public interface JwtService {
    // 生成token
    String createToken(Long userId,String username);
    // 生成refreshtoken，remember 为 true 时使用更长有效期
    String createRefreshToken(Long userId, boolean remember);
    // 获取用户ID
    Long getUserId(String token);
    //从refreshToken拿userId
    Long getUserIdFromRefreshToken(String token);
    // 获取用户名
    String getUsername(String token);
    // 校验token
    boolean validateToken(String token);
    //把refreshToken写入HttpOnly Cookie，remember 为 true 时写入持久化Cookie
    void setAdminRefreshCookie(String refreshToken, boolean remember, HttpServletResponse response);
    //把refreshToken清空
    void clearAdminRefreshCookie(HttpServletResponse response);
    //把refreshToken写入HttpOnly Cookie，remember 为 true 时写入持久化Cookie
    void setUserRefreshCookie(String refreshToken, boolean remember, HttpServletResponse response);
    //把refreshToken清空
    void clearUserRefreshCookie(HttpServletResponse response);
}
