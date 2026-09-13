package com.blog.controller.user;

import com.blog.constant.ArticleStatusConstant;
import com.blog.constant.DelStatusConstant;
import com.blog.context.BaseContext;
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
        // 用户端走独立查询：只返回已发布、已归档且未删除的文章，不会漏出草稿/私密内容
        PageResult pageResult=articleService.pageQueryUser(param);
        return Result.success(pageResult);
    }

    /**
     * 根据文章id获取文章详情等数据
     * @param id
     * @return
     */
    @GetMapping("/getArticleDetail/{id}")
    public Result<ArticleDetailVO> getArticleDetail(@PathVariable("id") Long id){
        log.info("文章id:{}",id);
        ArticleDetailVO articleDetailVO = articleService.getArticleById(id);
        if (articleDetailVO == null || articleDetailVO.getArticleVo() == null) {
            return Result.error("文章不存在");
        }
        // 未登录游客只能查看已发布且未被删除的文章，登录用户保持原有行为（可预览自己的文章）
        if (BaseContext.getCurrentId() == null && !isPublicArticle(articleDetailVO.getArticleVo())) {
            return Result.error("文章不存在或暂未公开");
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
