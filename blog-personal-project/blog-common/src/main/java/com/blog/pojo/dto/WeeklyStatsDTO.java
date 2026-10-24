package com.blog.pojo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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
    @NotNull(message = "阅读量不能为空")
    @PositiveOrZero(message = "阅读量不能小于0")
    private Long views;

    /**
     * 评论数
     */
    @NotNull(message = "评论数不能为空")
    @PositiveOrZero(message = "评论数不能小于0")
    private Long comments;

    /**
     * 新增文章
     */
    @NotNull(message = "新增文章数不能为空")
    @PositiveOrZero(message = "新增文章数不能小于0")
    private Integer newArticles;

    /**
     * 新增用户（原字段名保留，语义为新增用户）
     */
    @NotNull(message = "新增用户数不能为空")
    @PositiveOrZero(message = "新增用户数不能小于0")
    private Integer newSubscribers;

    /**
     * 热门文章TOP5
     */
    private String top5Articles;
}
