package com.blog.utils;

import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private static final long REFRESH_EXPIRE = 7 * 24 * 60 * 60;

    /**
     * 生成JWT token（临时）
     *
     * @param userId 存入用户唯一标识
     * @return token
     */
    public static String generateToken(String secretKey, long expireTime, Long userId, String username) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expireTime);

        return Jwts.builder()
                // 存放自定义载荷：用户ID
                .setSubject(userId.toString())
                .claim("username", username)
                // 签发时间
                .setIssuedAt(now)
                // 过期时间
                .setExpiration(expireDate)
                // 签名秘钥
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    /**
     * 生成刷新token
     *
     * @param userId
     * @return
     */
    public static String createRefreshToken(String secretKey,long RefreshTime,Long userId) {
        String rtId = UUID.randomUUID().toString();

        Date now = new Date();
        Date expireDate = new Date(now.getTime() + RefreshTime);

        return Jwts.builder()
                .setSubject(rtId)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    /**
     * 从Token中获取用户ID
     */
    public static Long getUserIdByToken(String secretKey, String token) {
        Claims claims = parseToken(secretKey, token);
        return Long.valueOf(claims.getSubject());
    }

    /**
     * 从refreshToken拿userId
     */
    public static Long getUserIdFromRefreshToken(String secretKey, String token) {
        Claims claims = parseToken(secretKey, token);
        return Long.valueOf(claims.get("userId").toString());
    }

    /**
     * 从Token中获取用户名
     */
    public static String getUsernameByToken(String secretKey, String token) {
        Claims claims = parseToken(secretKey, token);
        return claims.get("username", String.class);
    }

    /**
     * 解析Token
     */
    private static Claims parseToken(String secretKey, String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 校验Token是否有效
     * true=合法  false=过期/非法
     */
    public static boolean validateToken(String secretKey, String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            // token过期
//            throw new UserNotLoginException(MessageConstant.TOKEN_EXPIRED);
        } catch (UnsupportedJwtException e) {
            // 不支持的token
//            throw new UserNotLoginException(MessageConstant.TOKEN_NOT_SUPPORTED);
        } catch (MalformedJwtException e) {
            // token格式错误
//            throw new UserNotLoginException(MessageConstant.TOKEN_FORMAT_ERROR);
        } catch (SignatureException e) {
            // 签名错误，被篡改
//            throw new UserNotLoginException(MessageConstant.TOKEN_SIGNATURE_ERROR);
        } catch (IllegalArgumentException e) {
            // token为空
//            throw new UserNotLoginException(MessageConstant.TOKEN_EMPTY);
        }
        return false;
    }

    // 写入管理端RefreshToken到HttpOnly Cookie（同域无需Secure，生产HTTPS自行加Secure）
    public static void setAdminRefreshCookie(String refreshToken, HttpServletResponse response) {
        String cookieStr = String.format(
                "admin_refreshToken=%s; Path=/; HttpOnly; Max-Age=%d; SameSite=Lax",
                refreshToken, REFRESH_EXPIRE);
        response.setHeader("Set-Cookie", cookieStr);
    }

    // 清除管理端Cookie（退出登录）
    public static void clearAdminRefreshCookie(HttpServletResponse response) {
        String clearCookie = "admin_refreshToken=; Path=/; HttpOnly; Max-Age=0; SameSite=Lax";
        response.setHeader("Set-Cookie", clearCookie);
    }

    // 设置用户端刷新Cookie
    public static void setUserRefreshCookie(String refreshToken, HttpServletResponse response) {
        String cookieStr = String.format(
                "user_refreshToken=%s; Path=/; HttpOnly; Max-Age=%d; SameSite=Lax",
                refreshToken, REFRESH_EXPIRE);
        response.setHeader("Set-Cookie", cookieStr);
    }
    // 清除用户端Cookie
    public static void clearUserRefreshCookie(HttpServletResponse response) {
        String clearCookie = "user_refreshToken=; Path=/; HttpOnly; Max-Age=0; SameSite=Lax";
        response.setHeader("Set-Cookie", clearCookie);
    }
}
