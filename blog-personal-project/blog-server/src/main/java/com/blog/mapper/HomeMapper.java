package com.blog.mapper;

import com.blog.pojo.vo.HomeStatisticsVO;
import com.blog.pojo.vo.HomeTrendItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 首页统计 Mapper
 */
@Mapper
public interface HomeMapper {

    /**
     * 统计首页网站数据
     *
     * @return
     */
    HomeStatisticsVO getStatistics();

    /**
     * 统计文章增量趋势
     *
     * @param begin  开始时间
     * @param end    结束时间
     * @param format 时间分组格式
     * @return
     */
    List<HomeTrendItemVO> selectArticleTrend(@Param("begin") LocalDateTime begin,
                                             @Param("end") LocalDateTime end,
                                             @Param("format") String format);

    /**
     * 统计日常增量趋势
     *
     * @param begin  开始时间
     * @param end    结束时间
     * @param format 时间分组格式
     * @return
     */
    List<HomeTrendItemVO> selectDailyTrend(@Param("begin") LocalDateTime begin,
                                           @Param("end") LocalDateTime end,
                                           @Param("format") String format);

    /**
     * 统计用户增量趋势
     *
     * @param begin  开始时间
     * @param end    结束时间
     * @param format 时间分组格式
     * @return
     */
    List<HomeTrendItemVO> selectUserTrend(@Param("begin") LocalDateTime begin,
                                          @Param("end") LocalDateTime end,
                                          @Param("format") String format);

    /**
     * 统计评论增量趋势
     *
     * @param begin  开始时间
     * @param end    结束时间
     * @param format 时间分组格式
     * @return
     */
    List<HomeTrendItemVO> selectCommentTrend(@Param("begin") LocalDateTime begin,
                                             @Param("end") LocalDateTime end,
                                             @Param("format") String format);

    /**
     * 统计友链增量趋势
     *
     * @param begin  开始时间
     * @param end    结束时间
     * @param format 时间分组格式
     * @return
     */
    List<HomeTrendItemVO> selectLinkTrend(@Param("begin") LocalDateTime begin,
                                          @Param("end") LocalDateTime end,
                                          @Param("format") String format);

    /**
     * 统计点赞增量趋势
     *
     * @param begin  开始时间
     * @param end    结束时间
     * @param format 时间分组格式
     * @return
     */
    List<HomeTrendItemVO> selectLikeTrend(@Param("begin") LocalDateTime begin,
                                          @Param("end") LocalDateTime end,
                                          @Param("format") String format);

    /**
     * 统计已拒绝友链增量趋势
     *
     * @param begin  开始时间
     * @param end    结束时间
     * @param format 时间分组格式
     * @return
     */
    List<HomeTrendItemVO> selectRejectedLinkTrend(@Param("begin") LocalDateTime begin,
                                                  @Param("end") LocalDateTime end,
                                                  @Param("format") String format);
}
