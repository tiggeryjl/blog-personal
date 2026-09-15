package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户端站点实时统计
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SiteStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    //WebSocket消息类型
    @Builder.Default
    private String messageType = "siteStatistics";

    //在线访客数
    private Long onlineVisitor;

    //今日浏览量
    private Long todayView;

    //总浏览量
    private Long totalView;

    //总访问量
    private Long totalVisitor;

    //公开文章数
    private Long articleCount;

    //服务运行时长
    private Long runningSeconds;
}
