package com.blog.constant;

/**
 * 登录风险验证相关常量
 */
public final class LoginVerificationConstant {

    /**
     * 登录验证码缓存前缀
     */
    public static final String CODE_KEY_PREFIX = "auth:code:login:";

    /**
     * 登录风险凭证前缀
     */
    public static final String RISK_KEY_PREFIX = "auth:risk:login:";

    /**
     * 同一用户发送验证码间隔前缀
     */
    public static final String LIMIT_KEY_PREFIX = "auth:limit:login:";

    /**
     * 验证码错误次数前缀
     */
    public static final String ATTEMPT_KEY_PREFIX = "auth:attempt:login:";

    /**
     * 登录密码错误次数前缀
     */
    public static final String FAILURE_KEY_PREFIX = "auth:failure:login:";

    /**
     * 邮箱每日发送次数前缀
     */
    public static final String EMAIL_DAILY_KEY_PREFIX = "auth:limit:email:login:";

    /**
     * IP 每日发送次数前缀
     */
    public static final String IP_DAILY_KEY_PREFIX = "auth:limit:ip:login:";

    /**
     * 验证码有效期：5 分钟
     */
    public static final long CODE_TTL_SECONDS = 300L;

    /**
     * 风险凭证有效期：5 分钟
     */
    public static final long RISK_TTL_SECONDS = 300L;

    /**
     * 同一用户重发验证码间隔：60 秒
     */
    public static final long SEND_INTERVAL_SECONDS = 60L;

    /**
     * 验证码最大错误次数
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

    /**
     * 密码错误统计窗口：10 分钟
     */
    public static final long FAILURE_WINDOW_SECONDS = 600L;

    /**
     * 达到该错误次数后要求邮箱验证
     */
    public static final long FAILURE_THRESHOLD = 3L;
}
