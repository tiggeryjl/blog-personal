package com.blog.mapper;

import com.blog.pojo.entity.RssSubscription;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * RSS 邮箱订阅 Mapper
 */
@Mapper
public interface RssSubscriptionMapper {

    /**
     * 新增订阅记录
     */
    int insert(RssSubscription subscription);

    /**
     * 按邮箱查询订阅记录（含已退订的）
     */
    RssSubscription getByEmail(@Param("email") String email);

    /**
     * 按退订令牌查询订阅记录
     */
    RssSubscription getByToken(@Param("token") String token);

    /**
     * 重新激活订阅：刷新令牌和订阅时间，清空退订时间
     */
    int reactivate(RssSubscription subscription);

    /**
     * 退订
     */
    int unsubscribeByToken(@Param("token") String token);

    /**
     * 查询所有订阅中的邮箱，用于群发新文章通知
     */
    List<RssSubscription> listActive();

    /**
     * 统计订阅中的人数
     */
    Long countActive();
}
