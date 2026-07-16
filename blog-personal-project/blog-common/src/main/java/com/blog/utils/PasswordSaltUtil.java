package com.blog.utils;

import org.springframework.util.DigestUtils;
import java.util.UUID;

public class PasswordSaltUtil {

    // 生成随机盐 32位
    public static String generateSalt() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // 密码加密：原密码 + salt 再MD5
    public static String encryptPwd(String rawPwd, String salt) {
        return DigestUtils.md5DigestAsHex((rawPwd + salt).getBytes());
    }
}
