package com.blog.service;

import com.blog.pojo.vo.LikeVo;

/**
 * 点赞服务
 */
public interface LikeService {

    /**
     * 点赞文章/评论
     *
     * @param targetType 点赞目标类型 0=文章 2=评论
     * @param targetId   目标ID
     * @return 点赞结果
     */
    LikeVo like(Integer targetType, Long targetId);
}
