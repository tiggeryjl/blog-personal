package com.blog.service.impl;

import com.blog.mapper.SystemConfigMapper;
import com.blog.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 系统配置 Service 实现
 */
@Slf4j
@Service
public class SystemConfigServiceImpl implements SystemConfigService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Override
    public String getValue(String configKey) {
        if (!StringUtils.hasText(configKey)) {
            return null;
        }
        return systemConfigMapper.getValueByKey(configKey);
    }

    @Override
    public LocalDateTime getDateTimeValue(String configKey) {
        String value = getValue(configKey);
        if (!StringUtils.hasText(value)) {
            log.warn("系统配置不存在或为空，配置键：{}", configKey);
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim(), DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            log.error("系统配置日期格式错误，配置键：{}，期望格式：yyyy-MM-dd HH:mm:ss", configKey);
            return null;
        }
    }
}
