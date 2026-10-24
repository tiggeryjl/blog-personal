package com.blog.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 首页文章热度排行配置
 */
@Component
@ConfigurationProperties(prefix = "blog.ranking.article")
@Data
public class ArticleRankingProperties {

    /**
     * 浏览权重
     */
    private double viewWeight = 1.0;

    /**
     * 点赞权重
     */
    private double likeWeight = 4.0;

    /**
     * 评论权重
     */
    private double commentWeight = 6.0;

    /**
     * 热度半衰期（小时）
     */
    private double halfLifeHours = 168.0;

    /**
     * 编辑推荐加成
     */
    private double recommendBoost = 1.2;
}
