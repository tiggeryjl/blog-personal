package com.blog.mapper;

import com.blog.pojo.vo.HomeTrendItemVO;
import com.blog.pojo.vo.ReportDistributionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 数据分析报表 Mapper
 */
@Mapper
public interface ReportMapper {

    // ==================== 作品统计 ====================

    /**
     * 作品模块汇总指标
     */
    Map<String, Object> selectWorksSummary(@Param("begin") LocalDateTime begin,
                                           @Param("end") LocalDateTime end);

    /**
     * 文章新增趋势
     */
    List<HomeTrendItemVO> selectArticleTrend(@Param("begin") LocalDateTime begin,
                                             @Param("end") LocalDateTime end,
                                             @Param("format") String format);

    /**
     * 日常新增趋势
     */
    List<HomeTrendItemVO> selectDailyTrend(@Param("begin") LocalDateTime begin,
                                           @Param("end") LocalDateTime end,
                                           @Param("format") String format);

    /**
     * 文章阅读趋势（基于阅读记录表）
     */
    List<HomeTrendItemVO> selectViewTrend(@Param("begin") LocalDateTime begin,
                                          @Param("end") LocalDateTime end,
                                          @Param("format") String format);

    /**
     * 文章分类分布
     */
    List<ReportDistributionVO> selectArticleCategoryDistribution();

    /**
     * 文章状态分布
     */
    List<ReportDistributionVO> selectArticleStatusDistribution();

    /**
     * 阅读量 TOP 文章
     */
    List<Map<String, Object>> selectTopArticleByViews(@Param("limit") int limit);

    /**
     * 点赞 TOP 日常
     */
    List<Map<String, Object>> selectTopDailyByLikes(@Param("limit") int limit);

    // ==================== 评论统计 ====================

    /**
     * 评论模块汇总指标
     */
    Map<String, Object> selectCommentSummary(@Param("begin") LocalDateTime begin,
                                             @Param("end") LocalDateTime end);

    /**
     * 评论新增趋势
     */
    List<HomeTrendItemVO> selectCommentTrend(@Param("begin") LocalDateTime begin,
                                             @Param("end") LocalDateTime end,
                                             @Param("format") String format);

    /**
     * 评论类型分布
     */
    List<ReportDistributionVO> selectCommentTypeDistribution(@Param("begin") LocalDateTime begin,
                                                             @Param("end") LocalDateTime end);

    /**
     * 评论状态分布
     */
    List<ReportDistributionVO> selectCommentStatusDistribution(@Param("begin") LocalDateTime begin,
                                                               @Param("end") LocalDateTime end);

    /**
     * 评论最多文章 TOP
     */
    List<Map<String, Object>> selectTopCommentArticle(@Param("begin") LocalDateTime begin,
                                                      @Param("end") LocalDateTime end,
                                                      @Param("limit") int limit);

    // ==================== 用户统计 ====================

    /**
     * 用户模块汇总指标
     */
    Map<String, Object> selectUserSummary(@Param("begin") LocalDateTime begin,
                                          @Param("end") LocalDateTime end);

    /**
     * 用户注册趋势
     */
    List<HomeTrendItemVO> selectUserTrend(@Param("begin") LocalDateTime begin,
                                          @Param("end") LocalDateTime end,
                                          @Param("format") String format);

    /**
     * 用户活跃度分布
     */
    List<ReportDistributionVO> selectUserActivityDistribution();

    /**
     * 活跃用户 TOP
     */
    List<Map<String, Object>> selectTopActiveUsers(@Param("begin") LocalDateTime begin,
                                                   @Param("end") LocalDateTime end,
                                                   @Param("limit") int limit);

    // ==================== 友链统计 ====================

    /**
     * 友链模块汇总指标
     */
    Map<String, Object> selectLinkSummary(@Param("begin") LocalDateTime begin,
                                          @Param("end") LocalDateTime end);

    /**
     * 友链申请趋势
     */
    List<HomeTrendItemVO> selectLinkTrend(@Param("begin") LocalDateTime begin,
                                          @Param("end") LocalDateTime end,
                                          @Param("format") String format);

    /**
     * 友链审核状态分布
     */
    List<ReportDistributionVO> selectLinkAuditDistribution();

    /**
     * 待审核友链 TOP
     */
    List<Map<String, Object>> selectTopPendingLinks(@Param("limit") int limit);

    // ==================== AI 周报 ====================

    /**
     * 周报基础数据（阅读量/评论/新增文章/新增用户）
     */
    Map<String, Object> selectWeeklyReportData(@Param("begin") LocalDateTime begin,
                                               @Param("end") LocalDateTime end);

    /**
     * 本周阅读量 TOP 文章
     */
    List<Map<String, Object>> selectTopArticlesByWeeklyViews(@Param("begin") LocalDateTime begin,
                                                             @Param("end") LocalDateTime end,
                                                             @Param("limit") int limit);
}
