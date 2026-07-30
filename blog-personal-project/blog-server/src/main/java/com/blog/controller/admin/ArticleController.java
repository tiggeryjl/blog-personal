package com.blog.controller.admin;

import com.blog.pojo.dto.ArticleDTO;
import com.blog.pojo.dto.ArticlePageQueryDTO;
import com.blog.pojo.vo.ArticleVo;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 文章管理
 */
@Slf4j
@RestController
@RequestMapping("/admin/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    /**
     * 分页查询文章列表
     * @param param
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:article:list')")
    @GetMapping("/getArticleList")
    public Result<PageResult> getArticleList(ArticlePageQueryDTO param){
        log.info("分页查询文章列表:{}",param);
        PageResult pageResult=articleService.pageQurey(param);
        return Result.success(pageResult);
    }

    /**
     * 新增文章
     * @param articleDTO
     * @return
     */
    @PostMapping("/add")
    public Result add(@RequestBody ArticleDTO articleDTO){
        log.info("新增文章:{}",articleDTO);
        articleService.add(articleDTO);
        return Result.success();
    }

    /**
     * 根据ID查询文章
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<ArticleVo> getArticleById(@PathVariable Long id){
        log.info("查询id为{}的文章",id);
        ArticleVo articleVo=articleService.getArticleById(id);
        return Result.success(articleVo);
    }

    /**
     * 修改文章
     * @param articleDTO
     * @return
     */
    @PutMapping("/update")
    public Result update(@RequestBody ArticleDTO articleDTO){
        log.info("修改文章:{}",articleDTO);
        articleService.update(articleDTO);
        return Result.success();
    }

    /**
     * 设置定时发布
     */
    @PostMapping("/setTimed")
    public Result setTimed(@RequestBody ArticleDTO articleDTO) {
        log.info("设置定时发布:{}",articleDTO);
        articleService.setTimedPublish(articleDTO);
        return Result.success();
    }

    /**
     * 取消定时发布
     */
    @PostMapping("/cancelTimed/{id}")
    public Result cancelTimed(@PathVariable Long id) {
        log.info("取消定时发布id为:{}",id);
        articleService.cancelTimedPublish(id);
        return Result.success();
    }

}
