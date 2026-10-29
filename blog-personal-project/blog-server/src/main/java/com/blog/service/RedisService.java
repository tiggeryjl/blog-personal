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
     * 仅当 key 不存在时设置缓存，适合发送频率限流
     *
     * @param key 键
     * @param value 值
     * @param timeout 过期时长（秒）
     * @return 是否设置成功
     */
    boolean setIfAbsent(String key, Object value, long timeout);

    /**
     * 原子递增计数器，并在首次创建时设置过期时间
     *
     * @param key 键
     * @param delta 增量
     * @param timeout 过期时长（秒）
     * @return 递增后的值
     */
    long incrementAndExpire(String key, long delta, long timeout);

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
     * 根据前缀批量删除缓存
     */
    Long deleteByPrefix(String prefix);

    /**
     * 更新缓存：直接set覆盖旧值即可
     */
    void update(String key,Object newValue);
}
