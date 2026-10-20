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

    /**
     * 写入配置值，配置键已存在时覆盖（用于保存 RSS 通知时间水位线等运行状态）
     *
     * @param configKey   配置键
     * @param configValue 配置值
     * @param description 配置描述，仅新增时写入
     * @return 影响行数
     */
    int upsertValue(@Param("configKey") String configKey,
                    @Param("configValue") String configValue,
                    @Param("description") String description);

    /**
     * 写入带类型的配置值，配置键已存在时覆盖（用于 JSON 等结构化配置）
     *
     * @param configKey   配置键
     * @param configValue 配置值
     * @param configType  配置类型
     * @param description 配置描述
     * @return 影响行数
     */
    int upsertValueWithType(@Param("configKey") String configKey,
                            @Param("configValue") String configValue,
                            @Param("configType") String configType,
                            @Param("description") String description);
}
