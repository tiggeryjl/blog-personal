package com.blog.service.impl;

import com.blog.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class RedisServiceImpl implements RedisService {

    @Autowired
    RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 设置缓存
     * @param key 键
     * @param value 值
     */
    public void set(String key,Object value){
        redisTemplate.opsForValue().set(key,value);
    }

    /**
     * 设置缓存+过期时间
     * @param key 键
     * @param value 值
     * @param timeout 过期时长
     */
    public void set(String key,Object value,long timeout){
        redisTemplate.opsForValue().set(key,value,timeout,TimeUnit.SECONDS);
    }

    /**
     * 获取缓存
     */
    public <T> T get(String key,Class<T> clazz){
        Object o = redisTemplate.opsForValue().get(key);
        if(o == null){
            return null;
        }
        // 如果是LinkedHashMap，手动用ObjectMapper转成目标对象
        if(o instanceof LinkedHashMap){
            return objectMapper.convertValue(o, clazz);
        }
        return clazz.cast(o);
    }

    /**
     * 删除缓存
     */
    public Boolean delete(String key){
        if(key == null) return false;
        return redisTemplate.delete(key);
    }

    /**
     * 判断key是否存在
     */
    public Boolean hasKey(String key){
        return redisTemplate.hasKey(key);
    }

    /**
     * 更新缓存：直接set覆盖旧值即可
     */
    public void update(String key,Object newValue){
        redisTemplate.opsForValue().set(key,newValue);
    }
}
