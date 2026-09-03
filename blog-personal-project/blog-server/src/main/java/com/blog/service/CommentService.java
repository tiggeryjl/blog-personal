package com.blog.service;

import com.blog.pojo.dto.CommentPageQueryDTO;
import com.blog.pojo.dto.CommentReplyDTO;
import com.blog.pojo.dto.CommentStatusDTO;
import com.blog.pojo.vo.CommentVo;
import com.blog.result.PageResult;

import java.util.List;

/**
 * 公共评论服务
 */
public interface CommentService {

    /**
     * 分页查询评论列表
     *
     * @param commentPageQueryDTO 查询参数
     * @return 分页结果
     */
    PageResult pageQuery(CommentPageQueryDTO commentPageQueryDTO);

    /**
     * 分页查询逻辑删除的评论（回收站）
     *
     * @param commentPageQueryDTO 查询参数
     * @return 分页结果
     */
    PageResult recyclePageQuery(CommentPageQueryDTO commentPageQueryDTO);

    /**
     * 审核/隐藏评论
     *
     * @param commentStatusDTO 评论ID与目标状态
     */
    void updateStatus(CommentStatusDTO commentStatusDTO);

    /**
     * 后台回复评论
     *
     * @param commentReplyDTO 被回复评论ID与回复内容
     */
    void addReply(CommentReplyDTO commentReplyDTO);

    /**
     * 置顶/取消置顶评论
     *
     * @param id 评论ID
     */
    void updateTop(Long id);

    /**
     * 批量逻辑删除评论
     *
     * @param ids 评论ID集合
     */
    void logicDelete(List<Long> ids);

    /**
     * 批量物理删除评论
     *
     * @param ids 评论ID集合
     */
    void delete(List<Long> ids);

    /**
     * 批量恢复（回收站 -> 正常列表）
     *
     * @param ids 评论ID集合
     */
    void recover(List<Long> ids);

    /**
     * 根据文章id查询文章评论
     * @param id
     * @return
     */
    List<CommentVo> getArticleById(Long id);

    /**
     * 发表文章顶级评论
     *
     * @param articleId 文章ID
     * @param content   评论内容
     * @return 新评论ID
     */
    void addArticleComment(Long articleId, String content);
}
