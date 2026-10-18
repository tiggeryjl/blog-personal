package com.blog.utils;

import java.security.SecureRandom;

/**
 * 密码生成工具
 */
public final class PasswordGenerateUtil {

    /**
     * 可用字符集：去掉了容易看错的 i / l / o 和 0 / 1，方便口头或手抄传达
     */
    private static final char[] CHARS = "abcdefghjkmnpqrstuvwxyz23456789".toCharArray();

    /**
     * 默认密码长度
     */
    private static final int DEFAULT_LENGTH = 8;

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成随机密码（默认 8 位）
     *
     * @return 随机密码
     */
    public static String generate() {
        return generate(DEFAULT_LENGTH);
    }

    /**
     * 生成指定长度的随机密码
     *
     * @param length 长度，小于 1 时按默认长度处理
     * @return 随机密码
     */
    public static String generate(int length) {
        int realLength = length < 1 ? DEFAULT_LENGTH : length;
        StringBuilder password = new StringBuilder(realLength);
        for (int i = 0; i < realLength; i++) {
            password.append(CHARS[RANDOM.nextInt(CHARS.length)]);
        }
        return password.toString();
    }

    private PasswordGenerateUtil() {
    }
}
