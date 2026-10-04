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
}
