package com.blog.config;

import com.blog.properties.AliOssProperties;
import com.blog.utils.AliyunAcsClient;
import com.blog.utils.AliyunOSSOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类,用于创建AliyunAcsClient对象
 */
@Configuration
@Slf4j
public class AcsConfiguration {

    @Bean
    @ConditionalOnMissingBean//保证容器内只有员工改对象
    public AliyunAcsClient aliyunAcsClient(AliOssProperties aliOssProperties){
        log.info("开始创建阿里云滑块验证工具类对象:{}",aliOssProperties);
        return new AliyunAcsClient(aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getRegion(),
                aliOssProperties.getSceneId());
    }
}
