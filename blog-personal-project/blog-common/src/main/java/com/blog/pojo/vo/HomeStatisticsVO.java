package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 首页网站数据统计
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HomeStatisticsVO implements Serializable {

    //文章总数
    private Long articleTotal;

    //日常总数
    private Long dailyTotal;

    //用户总数
    private Long userTotal;

    //评论总数
    private Long commentTotal;

    //友链总数
    private Long linkTotal;

    //点赞总数
    private Long likeTotal;

    //今日新增用户数
    private Long todayNewUser;

    //待审核友链
    private Long linkPending;

    //已拒绝友链
    private Long linkRejected;

    //已通过友链
    private Long linkApproved;

    //分类总数
    private Long categoryTotal;

    //标签总数
    private Long tagTotal;

    //待审核评论
    private Long commentPending;

    //总阅读量
    private Long viewTotal;
}
