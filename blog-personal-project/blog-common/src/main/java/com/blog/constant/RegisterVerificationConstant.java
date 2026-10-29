package com.blog.constant;

/**
 * 注册邮箱验证码相关常量
 */
public final class RegisterVerificationConstant {

    /**
     * 注册验证码缓存前缀
     */
    public static final String CODE_KEY_PREFIX = "auth:code:register:";

    /**
     * 同一邮箱发送间隔前缀
     */
    public static final String LIMIT_KEY_PREFIX = "auth:limit:register:";

    /**
     * 验证码错误次数前缀
     */
    public static final String ATTEMPT_KEY_PREFIX = "auth:attempt:register:";

    /**
     * 邮箱每日发送次数前缀
     */
    public static final String EMAIL_DAILY_KEY_PREFIX = "auth:limit:email:register:";

    /**
     * IP 每日发送次数前缀
     */
    public static final String IP_DAILY_KEY_PREFIX = "auth:limit:ip:register:";

    /**
     * 验证码有效期：5 分钟
     */
    public static final long CODE_TTL_SECONDS = 300L;

    /**
     * 同一邮箱重发间隔：60 秒
     */
    public static final long SEND_INTERVAL_SECONDS = 60L;

    /**
     * 验证码最多错误次数
     */
    public static final long MAX_ATTEMPTS = 5L;

    /**
     * 同一邮箱每日发送上限
     */
    public static final long EMAIL_DAILY_LIMIT = 10L;

    /**
     * 同一 IP 每日发送上限
     */
    public static final long IP_DAILY_LIMIT = 30L;

    /**
     * 每日计数有效期：24 小时
     */
    public static final long DAILY_COUNT_TTL_SECONDS = 86400L;

}
