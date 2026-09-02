package com.blog.controller.user;

import com.blog.pojo.dto.ArticleDTO;
import com.blog.pojo.dto.ArticlePageQueryDTO;
import com.blog.pojo.vo.ArticleDetailVO;
import com.blog.pojo.vo.ArticleVo;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.service.ArticleService;
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
        PageResult pageResult=articleService.pageQurey(param);
        return Result.success(pageResult);
    }

    /**
     * 根据文章id获取文章详情等数据
     * @param id
     * @return
     */
    @GetMapping("/getArticleDetail/{id}")
    public Result<ArticleDetailVO> getArticleDetail(@PathVariable Long id){
        log.info("文章id:{}",id);
        ArticleDetailVO articleDetailVO = articleService.getArticleById(id);
        return Result.success(articleDetailVO);
    }


}
