package com.blog.mapper;

import com.blog.pojo.dto.CommentPageQueryDTO;
import com.blog.pojo.entity.Comment;
import com.blog.pojo.vo.ArticleCountVO;
import com.blog.pojo.vo.CommentVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 公共评论 Mapper
 */
@Mapper
public interface CommentMapper {

    /**
     * 分页查询评论列表
     *
     * @param params 查询参数
     * @return 评论列表
     */
    List<CommentVo> pageQuery(CommentPageQueryDTO params);

    /**
     * 分页查询逻辑删除的评论（回收站）
     *
     * @param params 查询参数
     * @return 评论列表
     */
    List<CommentVo> recyclePageQuery(CommentPageQueryDTO params);

    /**
     * 分页查询主楼评论ID(仅 parent_id = 0)
     *
     * @param params 查询参数
     * @return 主楼评论ID集合
     */
    List<Long> pageMainIds(CommentPageQueryDTO params);

    /**
     * 根据ID集合查询评论(完整VO)
     *
     * @param ids 评论ID集合
     * @return 评论列表
     */
    List<CommentVo> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 查询指定主楼评论下的全部回复(递归所有层级)
     *
     * @param ids    主楼评论ID集合
     * @param status 状态过滤(可为空)
     * @return 回复列表
     */
    List<CommentVo> selectRepliesByParentIds(List<Long> ids, Integer status);

    /**
     * 根据ID查询评论
     *
     * @param id 评论ID
     * @return 评论
     */
    Comment getById(@Param("id") Long id);

    /**
     * 更新评论(状态/置顶等)
     *
     * @param comment 评论
     */
    void update(Comment comment);

    /**
     * 新增评论(后台回复)
     *
     * @param comment 评论
     */
    void add(Comment comment);

    /**
     * 批量统计多篇文章的可见评论数
     *
     * @param articleIds 文章ID集合
     * @return 各文章ID对应的评论数
     */
    List<ArticleCountVO> countByArticleIds(@Param("articleIds") Collection<Long> articleIds);

    /**
     * 批量逻辑删除(含其下二级回复)
     *
     * @param ids 评论ID集合
     */
    void logicDelete(@Param("ids") List<Long> ids);

    /**
     * 批量物理删除(含其下二级回复)
     *
     * @param ids 评论ID集合
     */
    void delete(@Param("ids") List<Long> ids);

    /**
     * 批量恢复（逻辑删除 -> 正常）
     *
     * @param ids 评论ID集合
     */
    void recover(@Param("ids") List<Long> ids);

    /**
     * 根据文章id查询文章评论
     * @param comment
     * @return
     */
    List<Comment> getArticle(Comment comment);

    /**
     * 变更评论点赞数
     *
     * @param id    评论ID
     * @param delta 变化量(+1点赞)
     * @return 受影响行数
     */
    int changeLikeNum(@Param("id") Long id, @Param("delta") int delta);
}
