package com.blog.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 系统配置 Mapper
 */
@Mapper
public interface SystemConfigMapper {

    /**
     * 根据配置键查询配置值
     * @param configKey 配置键
     * @return
     */
    String getValueByKey(@Param("configKey") String configKey);
}
