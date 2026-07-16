package com.blog.service;

import jakarta.servlet.http.HttpServletResponse;

public interface JwtService {
    // 生成token
    String createToken(Long userId,String username);
    // 生成refreshtoken
    String createRefreshToken(Long userId);
    // 获取用户ID
    Long getUserId(String token);
    //从refreshToken拿userId
    Long getUserIdFromRefreshToken(String token);
    // 获取用户名
    String getUsername(String token);
    // 校验token
    boolean validateToken(String token);
    //把refreshToken写入HttpOnly Cookie
    void setAdminRefreshCookie(String refreshToken, HttpServletResponse response);
    //把refreshToken清空
    void clearAdminRefreshCookie(HttpServletResponse response);
    //把refreshToken写入HttpOnly Cookie
    void setUserRefreshCookie(String refreshToken, HttpServletResponse response);
    //把refreshToken清空
    void clearUserRefreshCookie(HttpServletResponse response);
}
