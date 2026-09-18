package com.blog.service;

import com.blog.pojo.vo.ArticleFrontVO;
import com.blog.pojo.vo.HomeStatisticsVO;
import com.blog.pojo.vo.HomeTrendVO;
import com.blog.pojo.vo.SiteStatisticsVO;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 首页 Service
 */
public interface HomeService {

    /**
     * 获取用户端首页热门文章
     *
     * @return 最多20条热门文章
     */
    List<ArticleFrontVO> getPopularArticles();

    /**
     * 获取首页网站统计数据
     *
     * @return
     */
    HomeStatisticsVO getStatistics();

    /**
     * 获取用户端站点实时统计
     *
     * @return 站点实时统计
     */
    SiteStatisticsVO getSiteStatistics();

    /**
     * 获取首页趋势数据
     *
     * @param rangeType 时间范围：today=今天 yesterday=昨天 week=近7天 month=近30天
     * @return
     */
    List<HomeTrendVO> getTrend(String rangeType);

    /**
     * 导出首页数据趋势报表
     *
     * @param response  HTTP 响应
     * @param rangeType 时间范围：today=今天 yesterday=昨天 week=近7天 month=近30天
     */
    void exportTrend(HttpServletResponse response, String rangeType);
}
