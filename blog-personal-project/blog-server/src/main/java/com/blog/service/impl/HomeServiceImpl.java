package com.blog.service.impl;

import com.blog.mapper.HomeMapper;
import com.blog.exception.CustomException;
import com.blog.pojo.vo.HomeStatisticsVO;
import com.blog.pojo.vo.HomeTrendItemVO;
import com.blog.pojo.vo.HomeTrendVO;
import com.blog.service.HomeService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 首页统计 Service 实现
 */
@Slf4j
@Service
public class HomeServiceImpl implements HomeService {

    @Autowired
    private HomeMapper homeMapper;

    /**
     * 获取首页网站统计数据
     *
     * @return
     */
    @Override
    public HomeStatisticsVO getStatistics() {
        return homeMapper.getStatistics();
    }

    /**
     * 获取首页趋势数据
     *
     * @param rangeType 时间范围：today=今天 yesterday=昨天 week=近7天 month=近30天
     * @return
     */
    @Override
    public List<HomeTrendVO> getTrend(String rangeType) {
        LocalDate today = LocalDate.now();
        LocalDateTime begin;
        LocalDateTime end;
        boolean hourly;

        // 1. 解析时间范围与分组粒度
        switch (rangeType) {
            case "today":
                begin = today.atStartOfDay();
                end = today.plusDays(1).atStartOfDay();
                hourly = true;
                break;
            case "yesterday":
                begin = today.minusDays(1).atStartOfDay();
                end = today.atStartOfDay();
                hourly = true;
                break;
            case "week":
                begin = today.minusDays(6).atStartOfDay();
                end = today.plusDays(1).atStartOfDay();
                hourly = false;
                break;
            case "month":
                begin = today.minusDays(29).atStartOfDay();
                end = today.plusDays(1).atStartOfDay();
                hourly = false;
                break;
            default:
                throw new CustomException("不支持的趋势时间范围:" + rangeType);
        }
        String format = hourly ? "%Y-%m-%d %H:00" : "%Y-%m-%d";

        // 2. 构建完整时间点列表
        List<String> periods = new ArrayList<>();
        if (hourly) {
            for (int hour = 0; hour < 24; hour++) {
                periods.add(String.format("%tF %02d:00", begin, hour));
            }
        } else {
            for (LocalDate date = begin.toLocalDate(); date.isBefore(end.toLocalDate()); date = date.plusDays(1)) {
                periods.add(date.toString());
            }
        }

        // 3. 查询各实体增量
        Map<String, Long> articleMap = toMap(homeMapper.selectArticleTrend(begin, end, format));
        Map<String, Long> dailyMap = toMap(homeMapper.selectDailyTrend(begin, end, format));
        Map<String, Long> userMap = toMap(homeMapper.selectUserTrend(begin, end, format));
        Map<String, Long> commentMap = toMap(homeMapper.selectCommentTrend(begin, end, format));
        Map<String, Long> linkMap = toMap(homeMapper.selectLinkTrend(begin, end, format));
        Map<String, Long> likeMap = toMap(homeMapper.selectLikeTrend(begin, end, format));
        Map<String, Long> rejectedLinkMap = toMap(homeMapper.selectRejectedLinkTrend(begin, end, format));

        // 4. 组装完整趋势列表
        return periods.stream().map(period -> HomeTrendVO.builder()
                .period(period)
                .articleCount(articleMap.getOrDefault(period, 0L))
                .dailyCount(dailyMap.getOrDefault(period, 0L))
                .userCount(userMap.getOrDefault(period, 0L))
                .commentCount(commentMap.getOrDefault(period, 0L))
                .linkCount(linkMap.getOrDefault(period, 0L))
                .likeCount(likeMap.getOrDefault(period, 0L))
                .rejectedLinkCount(rejectedLinkMap.getOrDefault(period, 0L))
                .build()).collect(Collectors.toList());
    }

    /**
     * 趋势查询结果转 Map
     *
     * @param list
     * @return
     */
    private Map<String, Long> toMap(List<HomeTrendItemVO> list) {
        return list.stream().collect(Collectors.toMap(HomeTrendItemVO::getPeriod, HomeTrendItemVO::getCount, (a, b) -> a));
    }

    /**
     * 导出首页数据趋势报表
     */
    @Override
    public void exportTrend(HttpServletResponse response, String rangeType) {
        List<HomeTrendVO> trendList = getTrend(rangeType);
        HomeStatisticsVO statistics = getStatistics();
        String rangeLabel = getRangeLabel(rangeType);

        try (InputStream in = getClass().getClassLoader().getResourceAsStream("template/数据趋势报表模板.xlsx");
             XSSFWorkbook workbook = new XSSFWorkbook(in)) {
            XSSFSheet sheet = workbook.getSheetAt(0);

            // 表头信息：时间范围、导出时间
            XSSFRow row1 = getOrCreateRow(sheet, 1);
            getOrCreateCell(row1,2).setCellValue(rangeLabel);

            XSSFRow row2 = getOrCreateRow(sheet, 2);
            getOrCreateCell(row2,2).setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // 概览数据：第6行 文章/日常/评论/用户，第7行 友链/点赞（Excel 行号 → 索引减1）
            setNumericCell(getOrCreateRow(sheet,5), 2, statistics.getArticleTotal());
            setNumericCell(getOrCreateRow(sheet,5), 4, statistics.getDailyTotal());
            setNumericCell(getOrCreateRow(sheet,5), 6, statistics.getCommentTotal());
            setNumericCell(getOrCreateRow(sheet,5), 8, statistics.getUserTotal());
            setNumericCell(getOrCreateRow(sheet,6), 2, statistics.getLinkTotal());
            setNumericCell(getOrCreateRow(sheet,6), 4, statistics.getLikeTotal());

            // 明细数据
            int dataStart = 10;
            for (int i = 0; i < trendList.size() && i < 30; i++) {
                HomeTrendVO vo = trendList.get(i);
                XSSFRow row = getOrCreateRow(sheet, dataStart + i);
                setNumericCell(row, 1, vo.getPeriod());
                setNumericCell(row, 2, vo.getArticleCount());
                setNumericCell(row, 3, vo.getDailyCount());
                setNumericCell(row, 4, vo.getUserCount());
                setNumericCell(row, 5, vo.getCommentCount());
                setNumericCell(row, 6, vo.getLikeCount());
                setNumericCell(row, 7, vo.getLinkCount());
                setNumericCell(row, 8, vo.getRejectedLinkCount());
            }

            // 合计行（第41行，索引40）
            XSSFRow totalRow = getOrCreateRow(sheet, 40);
            setNumericCell(totalRow, 2, sumTrend(trendList, HomeTrendVO::getArticleCount));
            setNumericCell(totalRow, 3, sumTrend(trendList, HomeTrendVO::getDailyCount));
            setNumericCell(totalRow, 4, sumTrend(trendList, HomeTrendVO::getUserCount));
            setNumericCell(totalRow, 5, sumTrend(trendList, HomeTrendVO::getCommentCount));
            setNumericCell(totalRow, 6, sumTrend(trendList, HomeTrendVO::getLikeCount));
            setNumericCell(totalRow, 7, sumTrend(trendList, HomeTrendVO::getLinkCount));
            setNumericCell(totalRow, 8, sumTrend(trendList, HomeTrendVO::getRejectedLinkCount));

            // 写入响应
            String fileName = "数据趋势报表-" + rangeLabel + "-" + LocalDate.now() + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename=\"" + encoded + "\";filename*=UTF-8''" + encoded);
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        } catch (IOException e) {
            log.error("导出首页趋势报表异常", e);
            if (!response.isCommitted()) {
                response.reset();
            }
            throw new CustomException("报表导出失败:" + e.getMessage());
        }
    }

    /**
     * 时间范围转中文标签
     */
    private String getRangeLabel(String rangeType) {
        switch (rangeType) {
            case "today":
                return "今天";
            case "yesterday":
                return "昨天";
            case "week":
                return "近7天";
            case "month":
                return "近30天";
            default:
                throw new CustomException("不支持的趋势时间范围:" + rangeType);
        }
    }

    /**
     * 获取或创建单元格
     */
    private XSSFCell getOrCreateCell(XSSFRow row, int column) {
        XSSFCell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
            XSSFCell neighbor = row.getCell(column - 1) != null ? row.getCell(column - 1) : row.getCell(column + 1);
            if (neighbor != null) {
                cell.setCellStyle(neighbor.getCellStyle());
            }
        }
        return cell;
    }

    /**
     * 获取或创建单元格行
     */
    private XSSFRow getOrCreateRow(XSSFSheet sheet, int rowIdx) {
        XSSFRow row = sheet.getRow(rowIdx);
        if (row == null) {
            row = sheet.createRow(rowIdx);
        }
        return row;
    }

    /**
     * 写入单元格值
     */
    private void setNumericCell(XSSFRow row, int column, Object value) {
        XSSFCell cell = getOrCreateCell(row, column);
        if (value == null) {
            cell.setCellValue(0);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }

    /**
     * 趋势数据求和
     */
    private long sumTrend(List<HomeTrendVO> list, Function<HomeTrendVO, Long> getter) {
        return list.stream().mapToLong(vo -> {
            Long value = getter.apply(vo);
            return value == null ? 0L : value;
        }).sum();
    }
}
