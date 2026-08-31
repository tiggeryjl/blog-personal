package com.blog.service;


public interface RedisService {


    /**
     * 设置缓存
     * @param key 键
     * @param value 值
     */
    void set(String key,Object value);

    /**
     * 设置缓存+过期时间
     * @param key 键
     * @param value 值
     * @param timeout 过期时长
     */
    void set(String key,Object value,long timeout);

    /**
     * 获取缓存
     */
    <T> T get(String key,Class<T> clazz);

    /**
     * 删除缓存
     */
    Boolean delete(String key);

    /**
     * 判断key是否存在
     */
    Boolean hasKey(String key);

    /**
     * 更新缓存：直接set覆盖旧值即可
     */
    void update(String key,Object newValue);
}
