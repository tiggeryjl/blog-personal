package com.blog.service;

import java.time.LocalDateTime;

/**
 * 系统配置 Service
 */
public interface SystemConfigService {

    /**
     * 根据配置键获取字符串配置值
     *
     * @param configKey 配置键
     * @return 配置值，不存在时返回 null
     */
    String getValue(String configKey);

    /**
     * 根据配置键获取日期时间配置值
     *
     * @param configKey 配置键
     * @return 日期时间，配置缺失或格式错误时返回 null
     */
    LocalDateTime getDateTimeValue(String configKey);

    /**
     * 写入或覆盖一项字符串配置，用于保存运行过程中的状态值
     * @param configKey   配置键
     * @param configValue 配置值
     * @param description 配置描述
     */
    void saveValue(String configKey, String configValue, String description);

    /**
     * 读取 JSON 配置值
     *
     * @param configKey 配置键
     * @param clazz     目标类型
     * @param <T>       目标类型
     * @return 反序列化后的对象，配置缺失或解析失败时返回 null
     */
    <T> T getJsonValue(String configKey, Class<T> clazz);

    /**
     * 写入 JSON 配置值
     *
     * @param configKey   配置键
     * @param value       配置对象
     * @param description 配置描述
     */
    void saveJsonValue(String configKey, Object value, String description);
}
