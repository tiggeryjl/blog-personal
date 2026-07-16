package com.blog.utils;

import java.util.Random;

public class UserUtil {
    // 手机号
    public static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    // 邮箱
    public static final String EMAIL_REGEX = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
    // 用户名 3-16位 字母数字下划线
    public static final String USERNAME_REG = "^[a-zA-Z0-9_]{3,16}$";

    // 11位纯数字
    public static boolean is11Digit(String str) {
        return str.matches("^\\d{11}$");
    }

    // 是否手机号
    public static boolean isPhone(String str) {
        return str.matches(PHONE_REGEX);
    }

    // 是否邮箱
    public static boolean isEmail(String str) {
        return str.matches(EMAIL_REGEX);
    }

    // 是否合法用户名格式（登录放行的合法格式）
    public static boolean isLegalUsername(String username) {
        // 登录允许的用户名：不能是11位数字、不能是邮箱
        return !is11Digit(username) && !isEmail(username);
    }

    //=======================================================================

    // 从QQ邮箱提取QQ号
    public static String getQQFromEmail(String email) {
        if (email == null || !email.endsWith("@qq.com")) {
            return null;
        }
        String prefix = email.split("@")[0];
        // 判断是不是纯数字QQ号
        if (!prefix.matches("\\d+")) {
            return null;
        }
        return prefix;
    }

    // 拼接头像地址
    public static String getQQAvatarUrl(String email) {
        String qq = getQQFromEmail(email);
        if (qq == null) {
            Random random = new Random();
            // 生成随机数50-100
            int one = random.nextInt(50) + 50;
            int two = random.nextInt(50) + 50;

            return "https://picsum.photos/"+one+"/"+two;
        }
        return "https://q1.qlogo.cn/headimg_dl?dst_uin=" + qq + "&spec=640";
    }

    // =============== 生成指定位数的随机数字字符串 ================
    public static String generateRandomNum() {
        Random random = new Random();
        // 生成第一个数字：1-9（保证不会以0开头）
        StringBuilder sb = new StringBuilder();
        sb.append(random.nextInt(9) + 1);

        // 生成剩下的数字：8
        for (int i = 1; i < 8; i++) {
            sb.append(random.nextInt(10));
        }

        return sb.toString();
    }
}
