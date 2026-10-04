package com.blog.service;

import com.blog.pojo.dto.RssSubscribeDTO;

/**
 * RSS 订阅与邮箱订阅服务
 */
public interface RssService {

    /**
     * 邮箱订阅
     * @param dto
     */
    void subscribe(RssSubscribeDTO dto);

    /**
     * 通过退订令牌取消订阅
     *
     * @param token 退订令牌
     * @return 是否退订成功
     */
    boolean unsubscribe(String token);

    /**
     * 当前订阅中的人数
     */
    Long countActive();

    /**
     * 扫描上次通知之后新发布的文章，给订阅者发送邮件通知
     */
    int notifyNewArticles();
}
