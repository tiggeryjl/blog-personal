package com.blog.service.impl;

import com.blog.exception.CustomException;
import com.blog.mapper.ReportMapper;
import com.blog.pojo.vo.HomeTrendItemVO;
import com.blog.pojo.vo.ReportDistributionVO;
import com.blog.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据分析报表 Service 实现
 */
@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    private static final String DAY_FORMAT = "%Y-%m-%d";

    @Autowired
    private ReportMapper reportMapper;

    /**
     * 模块汇总指标
     * @param module 模块works/comment/user/link
     * @param begin
     * @param end
     * @return
     */
    @Override
    public Map<String, Object> summary(String module, LocalDate begin, LocalDate end) {
        LocalDateTime[] range = resolveRange(begin, end);
        switch (module) {
            case "works":
                return reportMapper.selectWorksSummary(range[0], range[1]);
            case "comment":
                return reportMapper.selectCommentSummary(range[0], range[1]);
            case "user":
                return reportMapper.selectUserSummary(range[0], range[1]);
            case "link":
                return reportMapper.selectLinkSummary(range[0], range[1]);
            default:
                throw new CustomException("未知的报表模块:" + module);
        }
    }

    /**
     * 按天趋势
     * @param module 模块works/comment/user/link
     * @param begin
     * @param end
     * @return
     */
    @Override
    public List<Map<String, Object>> trend(String module, LocalDate begin, LocalDate end) {
        LocalDateTime[] range = resolveRange(begin, end);
        List<String> periods = buildPeriods(range[0].toLocalDate(), range[1].toLocalDate());

        switch (module) {
            case "works": {
                Map<String, Long> articleMap = toMap(reportMapper.selectArticleTrend(range[0], range[1], DAY_FORMAT));
                Map<String, Long> dailyMap = toMap(reportMapper.selectDailyTrend(range[0], range[1], DAY_FORMAT));
                Map<String, Long> viewMap = toMap(reportMapper.selectViewTrend(range[0], range[1], DAY_FORMAT));
                return periods.stream().map(period -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("period", period);
                    row.put("articleCount", articleMap.getOrDefault(period, 0L));
                    row.put("dailyCount", dailyMap.getOrDefault(period, 0L));
                    row.put("viewCount", viewMap.getOrDefault(period, 0L));
                    return row;
                }).collect(Collectors.toList());
            }
            case "comment":
                return singleTrend(periods, reportMapper.selectCommentTrend(range[0], range[1], DAY_FORMAT));
            case "user":
                return singleTrend(periods, reportMapper.selectUserTrend(range[0], range[1], DAY_FORMAT));
            case "link":
                return singleTrend(periods, reportMapper.selectLinkTrend(range[0], range[1], DAY_FORMAT));
            default:
                throw new CustomException("未知的报表模块:" + module);
        }
    }

    /**
     * 分布数据
     * @param module    模块：works/comment/user/link
     * @param dimension 维度：works=category/status，comment=type/status，user/link 忽略
     * @param begin
     * @param end
     * @return
     */
    @Override
    public List<ReportDistributionVO> distribution(String module, String dimension, LocalDate begin, LocalDate end) {
        LocalDateTime[] range = resolveRange(begin, end);
        switch (module) {
            case "works":
                if ("status".equals(dimension)) {
                    return reportMapper.selectArticleStatusDistribution();
                }
                return reportMapper.selectArticleCategoryDistribution();
            case "comment":
                if ("status".equals(dimension)) {
                    return reportMapper.selectCommentStatusDistribution(range[0], range[1]);
                }
                return reportMapper.selectCommentTypeDistribution(range[0], range[1]);
            case "user":
                return reportMapper.selectUserActivityDistribution();
            case "link":
                return reportMapper.selectLinkAuditDistribution();
            default:
                throw new CustomException("未知的报表模块:" + module);
        }
    }

    /**
     * 榜单数据
     * @param module 模块：works/comment/user/link
     * @param metric 指标：works=articleViews/dailyLikes，其余模块忽略
     * @param limit  返回条数，默认10，最大50
     * @param begin
     * @param end
     * @return
     */
    @Override
    public List<Map<String, Object>> top(String module, String metric, Integer limit, LocalDate begin, LocalDate end) {
        LocalDateTime[] range = resolveRange(begin, end);
        int size = (limit == null || limit <= 0) ? 10 : Math.min(limit, 50);
        switch (module) {
            case "works":
                if ("dailyLikes".equals(metric)) {
                    return reportMapper.selectTopDailyByLikes(size);
                }
                return reportMapper.selectTopArticleByViews(size);
            case "comment":
                return reportMapper.selectTopCommentArticle(range[0], range[1], size);
            case "user":
                return reportMapper.selectTopActiveUsers(range[0], range[1], size);
            case "link":
                return reportMapper.selectTopPendingLinks(size);
            default:
                throw new CustomException("未知的报表模块:" + module);
        }
    }

    /**
     * 近7天AI 周报基础数据
     * @return
     */
    @Override
    public Map<String, Object> weeklyReportData() {
        LocalDate today = LocalDate.now();
        LocalDateTime begin = today.minusDays(6).atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        Map<String, Object> data = reportMapper.selectWeeklyReportData(begin, end);
        List<Map<String, Object>> topList = reportMapper.selectTopArticlesByWeeklyViews(begin, end, 5);
        StringBuilder top5 = new StringBuilder();
        for (int i = 0; i < topList.size(); i++) {
            Map<String, Object> item = topList.get(i);
            if (i > 0) {
                top5.append("\n");
            }
            top5.append(i + 1).append(". 《").append(item.get("title")).append("》 阅读量 ").append(item.get("viewCount"));
        }
        data.put("newSubscribers", data.getOrDefault("newUsers", 0));
        data.put("top5Articles", top5.toString());
        return data;
    }

    /**
     * 单一序列趋势组装
     */
    private List<Map<String, Object>> singleTrend(List<String> periods, List<HomeTrendItemVO> list) {
        Map<String, Long> dataMap = toMap(list);
        return periods.stream().map(period -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("period", period);
            row.put("count", dataMap.getOrDefault(period, 0L));
            return row;
        }).collect(Collectors.toList());
    }

    /**
     * 趋势查询结果转 Map
     */
    private Map<String, Long> toMap(List<HomeTrendItemVO> list) {
        return list.stream().collect(Collectors.toMap(HomeTrendItemVO::getPeriod, HomeTrendItemVO::getCount, (a, b) -> a));
    }

    /**
     * 解析时间范围：未传 begin 默认近7天，未传 end 默认今天
     */
    private LocalDateTime[] resolveRange(LocalDate begin, LocalDate end) {
        LocalDate today = LocalDate.now();
        if (begin == null) {
            begin = today.minusDays(6);
        }
        if (end == null) {
            end = today;
        }
        if (begin.isAfter(end)) {
            throw new CustomException("开始日期不能晚于结束日期");
        }
        return new LocalDateTime[]{begin.atStartOfDay(), end.plusDays(1).atStartOfDay()};
    }

    /**
     * 构建完整日期列表（含结束日期）
     */
    private List<String> buildPeriods(LocalDate begin, LocalDate end) {
        List<String> periods = new ArrayList<>();
        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            periods.add(date.toString());
        }
        return periods;
    }
}
