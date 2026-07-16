package com.blog.constant;

/**
 * 逻辑删除状态常量，删除与正常
 * 账号锁定标识， 未锁定与已锁定
 */
public class DelStatusConstant {

    //正常 未锁定
    public static final Integer ENABLE = 0;

    //删除 已锁定
    public static final Integer DISABLE = 1;
}
