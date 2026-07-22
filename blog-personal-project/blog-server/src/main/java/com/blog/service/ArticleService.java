package com.blog.service;

import com.blog.pojo.dto.ArticleDTO;
import com.blog.pojo.dto.ArticlePageQueryDTO;
import com.blog.pojo.vo.ArticleVo;
import com.blog.result.PageResult;

import java.time.LocalDateTime;

public interface ArticleService {

    /**
     * 新增文章
     * @param articleDTO
     */
    void add(ArticleDTO articleDTO);

    /**
     * 分页查询文章列表
     * @param articlePageQueryDTO
     */
    PageResult pageQurey(ArticlePageQueryDTO articlePageQueryDTO);

    /**
     * 根据ID查询文章
     * @param id
     * @return
     */
    ArticleVo getArticleById(Long id);

    /**
     * 修改文章
     * @param articleDTO
     */
    void update(ArticleDTO articleDTO);

    /**
     * 设置定时发布
     */
    void setTimedPublish(ArticleDTO articleDTO);

    /**
     * 取消定时发布
     * @param id
     */
    void cancelTimedPublish(Long id);
}
