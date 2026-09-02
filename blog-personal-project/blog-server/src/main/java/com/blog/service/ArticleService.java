package com.blog.service;

import com.blog.pojo.dto.ArticleDTO;
import com.blog.pojo.dto.ArticlePageQueryDTO;
import com.blog.pojo.vo.ArticleDetailVO;
import com.blog.pojo.vo.ArticleVo;
import com.blog.result.PageResult;

import java.time.LocalDateTime;
import java.util.List;

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
     * 分页查询逻辑删除的文章（回收站）
     * @param articlePageQueryDTO
     */
    PageResult recyclePageQuery(ArticlePageQueryDTO articlePageQueryDTO);

    /**
     * 根据ID查询文章
     * @param id
     * @return
     */
    ArticleDetailVO getArticleById(Long id);

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

    /**
     * 逻辑删除
     * @param ids
     */
    void logicDelete(List<Long> ids);

    /**
     * 彻底删除
     * @param ids
     */
    void delete(List<Long> ids);

    /**
     * 批量恢复（回收站 -> 正常列表）
     * @param ids
     */
    void recover(List<Long> ids);

    /**
     * 修改文章状态
     * @param articleDTO
     */
    void updateStatus(ArticleDTO articleDTO);

    /**
     * 置顶设置
     * @param id
     */
    void updateTop(Long id);
}
