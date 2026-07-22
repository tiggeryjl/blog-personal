package com.blog.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeeklyStatsDTO implements Serializable {

    /**
     * 阅读量
     */
    private Long views;

    /**
     * 评论数
     */
    private Long comments;

    /**
     * 新增文章
     */
    private Integer newArticles;

    /**
     * 新增订阅
     */
    private Integer newSubscribers;

    /**
     * 热门文章TOP5
     */
    private String top5Articles;
}
