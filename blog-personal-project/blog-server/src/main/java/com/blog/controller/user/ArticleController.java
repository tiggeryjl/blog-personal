package com.blog.controller.user;

import com.blog.constant.ArticleStatusConstant;
import com.blog.constant.DelStatusConstant;
import com.blog.context.BaseContext;
import com.blog.pojo.dto.ArticleDTO;
import com.blog.pojo.dto.ArticlePageQueryDTO;
import com.blog.pojo.vo.ArticleCalendarVO;
import com.blog.pojo.vo.ArticleDetailVO;
import com.blog.pojo.vo.ArticleVo;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.service.ArticleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章管理
 */
@Slf4j
@RestController("userArticleController")
@RequestMapping("/user/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    /**
     * 分页查询文章列表
     * @param param
     * @return
     */
    @GetMapping("/getArticleList")
    public Result<PageResult> getArticleList(ArticlePageQueryDTO param){
        log.info("分页查询文章列表:{}",param);
        PageResult pageResult=articleService.pageQueryUser(param);
        return Result.success(pageResult);
    }

    /**
     * 查询指定年月内每天发布的文章数量
     * @param year  年份
     * @param month 月份
     */
    @GetMapping("/getCalendarCount")
    public Result<List<ArticleCalendarVO>> getCalendarCount(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month){
        log.info("查询日历文章数量 year:{}, month:{}", year, month);
        return Result.success(articleService.getCalendarArticleCounts(year, month));
    }

    /**
     * 根据文章id获取文章详情等数据
     * @param id
     * @return
     */
    @GetMapping("/getArticleDetail/{id}")
    public Result<ArticleDetailVO> getArticleDetail(@PathVariable("id") Long id, HttpServletRequest request){
        log.info("文章id:{}",id);
        ArticleDetailVO articleDetailVO = articleService.getArticleById(id);
        if (articleDetailVO == null || articleDetailVO.getArticleVo() == null) {
            return Result.error("文章不存在");
        }

        boolean publicArticle = isPublicArticle(articleDetailVO.getArticleVo());
        if (publicArticle && articleService.recordArticleView(id, request)) {
            Long viewNum = articleDetailVO.getArticleVo().getViewNum();
            articleDetailVO.getArticleVo().setViewNum((viewNum == null ? 0 : viewNum) + 1);
        }
        return Result.success(articleDetailVO);
    }

    /**
     * 判断文章是否对游客公开
     */
    private boolean isPublicArticle(ArticleVo articleVo) {
        return (ArticleStatusConstant.PUBLISHED.equals(articleVo.getStatus())
                || ArticleStatusConstant.ARCHIVED.equals(articleVo.getStatus()))
                && !DelStatusConstant.DISABLE.equals(articleVo.getDeleteFlag());
    }

}
