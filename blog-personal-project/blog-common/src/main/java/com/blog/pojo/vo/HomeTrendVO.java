package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 首页趋势数据
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HomeTrendVO implements Serializable {

    //时间点
    private String period;

    //新增文章数
    private Long articleCount;

    //新增日常数
    private Long dailyCount;

    //新增用户数
    private Long userCount;

    //新增评论数
    private Long commentCount;

    //新增友链数
    private Long linkCount;

    //新增点赞数
    private Long likeCount;

    //已拒绝友链数
    private Long rejectedLinkCount;
}
