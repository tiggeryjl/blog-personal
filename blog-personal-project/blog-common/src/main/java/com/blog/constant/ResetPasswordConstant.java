package com.blog.constant;

/**
 * 忘记密码（邮箱验证码重置密码）相关常量
 */
public final class ResetPasswordConstant {

    /**
     * 验证码缓存key前缀，完整key为 前缀 + 邮箱
     */
    public static final String CODE_KEY_PREFIX = "reset:pwd:code:";

    /**
     * 发送验证码限流key前缀，完整key为 前缀 + 邮箱
     */
    public static final String LIMIT_KEY_PREFIX = "reset:pwd:limit:";

    /**
     * 验证通过后发放的一次性凭证key前缀，完整key为 前缀 + 凭证
     */
    public static final String TICKET_KEY_PREFIX = "reset:pwd:ticket:";

    /**
     * 验证码有效期（秒）
     */
    public static final long CODE_TTL_SECONDS = 300L;

    /**
     * 同一邮箱再次发送验证码的间隔
     */
    public static final long SEND_INTERVAL_SECONDS = 60L;

    /**
     * 一次性凭证有效期
     */
    public static final long TICKET_TTL_SECONDS = 600L;

    /**
     * 验证码错误次数前缀
     */
    public static final String ATTEMPT_KEY_PREFIX = "reset:pwd:attempt:";

    /**
     * 邮箱每日发送上限前缀
     */
    public static final String EMAIL_DAILY_KEY_PREFIX = "reset:pwd:limit:email:";

    /**
     * IP 每日发送上限前缀
     */
    public static final String IP_DAILY_KEY_PREFIX = "reset:pwd:limit:ip:";

    /**
     * 每日计数有效期
     */
    public static final long DAILY_COUNT_TTL_SECONDS = 86400L;

    /**
     * 验证码最多错误次数
     */
    public static final long MAX_ATTEMPTS = 5L;

    /**
     * 同一邮箱每日发送上限
     */
    public static final long EMAIL_DAILY_LIMIT = 15L;

    /**
     * 同一 IP 每日发送上限
     */
    public static final long IP_DAILY_LIMIT = 30L;

    private ResetPasswordConstant() {
    }
}
