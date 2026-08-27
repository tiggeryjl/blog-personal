package com.blog.controller.admin;

import com.blog.pojo.vo.ReportDistributionVO;
import com.blog.result.Result;
import com.blog.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 数据分析报表接口
 */
@Slf4j
@RestController
@RequestMapping("/admin/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 模块汇总指标
     *
     * @param module works/comment/user/link
     */
    @PreAuthorize("@reportPermission.check(#module)")
    @GetMapping("/{module}/summary")
    public Result<Map<String, Object>> summary(
            @PathVariable String module,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("报表汇总:{}, begin={}, end={}", module, begin, end);
        return Result.success(reportService.summary(module, begin, end));
    }

    /**
     * 按天趋势
     *
     * @param module works/comment/user/link
     */
    @PreAuthorize("@reportPermission.check(#module)")
    @GetMapping("/{module}/trend")
    public Result<List<Map<String, Object>>> trend(
            @PathVariable String module,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("报表趋势:{}, begin={}, end={}", module, begin, end);
        return Result.success(reportService.trend(module, begin, end));
    }

    /**
     * 分布数据
     *
     * @param module    works/comment/user/link
     * @param dimension works=category/status，comment=type/status
     */
    @PreAuthorize("@reportPermission.check(#module)")
    @GetMapping("/{module}/distribution")
    public Result<List<ReportDistributionVO>> distribution(
            @PathVariable String module,
            @RequestParam(required = false) String dimension,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("报表分布:{}, dimension={}, begin={}, end={}", module, dimension, begin, end);
        return Result.success(reportService.distribution(module, dimension, begin, end));
    }

    /**
     * 榜单数据
     *
     * @param module works/comment/user/link
     * @param metric  works=articleViews/dailyLikes
     */
    @PreAuthorize("@reportPermission.check(#module)")
    @GetMapping("/{module}/top")
    public Result<List<Map<String, Object>>> top(
            @PathVariable String module,
            @RequestParam(required = false) String metric,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("报表榜单:{}, metric={}, limit={}, begin={}, end={}", module, metric, limit, begin, end);
        return Result.success(reportService.top(module, metric, limit, begin, end));
    }

    /**
     * AI 周报基础数据（近7天）
     */
    @PreAuthorize("@reportPermission.check('works')")
    @GetMapping("/weeklyData")
    public Result<Map<String, Object>> weeklyData() {
        log.info("获取AI周报基础数据");
        return Result.success(reportService.weeklyReportData());
    }
}
