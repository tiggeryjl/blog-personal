package com.blog.service.impl;

import com.blog.properties.JwtProperties;
import com.blog.service.JwtService;
import com.blog.utils.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JwtServiceImpl implements JwtService {

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
    public String createRefreshToken(Long userId) {
        return JwtUtil.createRefreshToken(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserRefreshTtl(),
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
    public void setAdminRefreshCookie(String refreshToken, HttpServletResponse response) {
        JwtUtil.setAdminRefreshCookie(refreshToken,response);
    }

    @Override
    public void clearAdminRefreshCookie(HttpServletResponse response) {
        JwtUtil.clearAdminRefreshCookie(response);
    }

    @Override
    public void setUserRefreshCookie(String refreshToken, HttpServletResponse response) {
        JwtUtil.setUserRefreshCookie(refreshToken,response);
    }

    @Override
    public void clearUserRefreshCookie(HttpServletResponse response) {
        JwtUtil.clearUserRefreshCookie(response);
    }
}
