package com.blog.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * 系统配置
 */
@Slf4j
@Service
public class SystemConfigServiceImpl implements SystemConfigService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Override
    public void saveValue(String configKey, String configValue, String description) {
        if (!StringUtils.hasText(configKey)) {
            return;
        }
        systemConfigMapper.upsertValue(configKey, configValue, description);
    }

    @Override
    public <T> T getJsonValue(String configKey, Class<T> clazz) {
        String value = getValue(configKey);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return objectMapper.readValue(value, clazz);
        } catch (JsonProcessingException e) {
            log.error("系统配置JSON解析失败，配置键：{}", configKey, e);
            return null;
        }
    }

    @Override
    public void saveJsonValue(String configKey, Object value, String description) {
        if (!StringUtils.hasText(configKey) || value == null) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(value);
            systemConfigMapper.upsertValueWithType(configKey, json, "json", description);
        } catch (JsonProcessingException e) {
            log.error("系统配置JSON序列化失败，配置键：{}", configKey, e);
            throw new RuntimeException("系统配置保存失败", e);
        }
    }
}
