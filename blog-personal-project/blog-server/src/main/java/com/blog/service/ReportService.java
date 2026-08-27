package com.blog.service;

import com.blog.pojo.vo.ReportDistributionVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 数据分析报表 Service
 */
public interface ReportService {

    /**
     * 模块汇总指标
     *
     * @param module 模块：works/comment/user/link
     * @param begin  开始日期（含），为空默认近7天
     * @param end    结束日期（含），为空默认今天
     */
    Map<String, Object> summary(String module, LocalDate begin, LocalDate end);

    /**
     * 按天趋势
     *
     * @param module 模块：works/comment/user/link
     * @param begin  开始日期（含）
     * @param end    结束日期（含）
     */
    List<Map<String, Object>> trend(String module, LocalDate begin, LocalDate end);

    /**
     * 分布数据
     *
     * @param module    模块：works/comment/user/link
     * @param dimension 维度：works=category/status，comment=type/status，user/link 忽略
     * @param begin     开始日期（含）
     * @param end       结束日期（含）
     */
    List<ReportDistributionVO> distribution(String module, String dimension, LocalDate begin, LocalDate end);

    /**
     * 榜单数据
     *
     * @param module 模块：works/comment/user/link
     * @param metric 指标：works=articleViews/dailyLikes，其余模块忽略
     * @param limit  返回条数，默认10，最大50
     * @param begin  开始日期（含）
     * @param end    结束日期（含）
     */
    List<Map<String, Object>> top(String module, String metric, Integer limit, LocalDate begin, LocalDate end);

    /**
     * AI 周报基础数据（近7天）
     */
    Map<String, Object> weeklyReportData();
}
