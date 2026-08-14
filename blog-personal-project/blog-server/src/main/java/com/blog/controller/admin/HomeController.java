package com.blog.controller.admin;

import com.blog.pojo.vo.HomeStatisticsVO;
import com.blog.pojo.vo.HomeTrendVO;
import com.blog.result.Result;
import com.blog.service.HomeService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * 首页统计接口
 */
@Slf4j
@RestController
@RequestMapping("/admin/home")
public class HomeController {

    @Autowired
    private HomeService homeService;

    /**
     * 获取首页网站统计数据
     *
     * @return
     */
    @PreAuthorize("hasAuthority('sys:index:list')")
    @GetMapping("/getStatistics")
    public Result<HomeStatisticsVO> getStatistics() {
        log.info("获取首页网站统计数据");
        HomeStatisticsVO statistics = homeService.getStatistics();
        return Result.success(statistics);
    }

    /**
     * 获取首页趋势数据
     *
     * @param rangeType 时间范围：today=今天 yesterday=昨天 week=近7天 month=近30天
     * @return
     */
    @PreAuthorize("hasAuthority('sys:index:list')")
    @GetMapping("/getTrend")
    public Result<List<HomeTrendVO>> getTrend(@RequestParam(defaultValue = "week") String rangeType) {
        log.info("获取首页趋势数据:{}", rangeType);
        List<HomeTrendVO> trendList = homeService.getTrend(rangeType);
        return Result.success(trendList);
    }

    /**
     * 导出首页数据趋势报表
     *
     * @param response  响应对象
     * @param rangeType 时间范围：today=今天 yesterday=昨天 week=近7天 month=近30天
     */
    @PreAuthorize("hasAuthority('sys:index:list')")
    @GetMapping("/exportTrend")
    public void exportTrend(HttpServletResponse response,
                            @RequestParam(defaultValue = "week") String rangeType) throws IOException {
        log.info("导出首页趋势报表:{}", rangeType);
        homeService.exportTrend(response, rangeType);
    }
}
