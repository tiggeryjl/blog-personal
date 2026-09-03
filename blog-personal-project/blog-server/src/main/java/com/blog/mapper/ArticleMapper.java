package com.blog.mapper;

import com.blog.pojo.dto.ArticlePageQueryDTO;
import com.blog.pojo.entity.Article;
import com.blog.pojo.vo.ArticleVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ArticleMapper {

    /**
     * 搜索关联的分类id
     * @param ids
     */
    List<Long> selectRelationCategoryIds(@Param("ids") List<Long> ids);


    /**
     * 新增文章
     * @param article
     */
    void add(Article article);

    /**
     * 分页查询文章列表
     * @param params
     * @return
     */
    List<ArticleVo> pageQurey(ArticlePageQueryDTO params);

    /**
     * 分页查询逻辑删除的文章（回收站）
     * @param params
     * @return
     */
    List<ArticleVo> recyclePageQuery(ArticlePageQueryDTO params);

    /**
     * 统计总数
     * @param params
     * @return
     */
    Long countPageQuery(ArticlePageQueryDTO params);

    /**
     * 根据ID查询文章
     * @param id
     * @return 1
     */
    @Select("select * from article where id =#{id}")
    Article getArticleById(Long id);

    /**
     * 根据ID查询文章完整信息（含分类、作者、标签）
     * @param id
     * @re turn
     */
    ArticleVo getArticleVoById(Long id);

    /**
     * 修改文章
     * @param article
     */
    void update(Article article);

    /**
     * 单独设置定时发布状态和时间
     */
    int updateTimedPublish(Article article);

    /**
     * 取消定时,改回草稿,清空定时时间
     */
    int cancelTimedPublish(Article article);

    /**
     * 批量发布所有到期的定时文章,定时任务专用
     * @return 影响行数
     */
    int batchPublishExpiredTimed(@Param("now") LocalDateTime now);

    /**
     * 批量逻辑删除
     * @param ids
     */
    void deleteBatchLogic(List<Long> ids, Integer deleteFlag);

    /**
     * 批量恢复
     * @param ids
     */
    void recoverBatch(@Param("ids") List<Long> ids);

    /**
     * 批量永久删除
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 查询上一页：文章列表页中当前文章的上一条
     * @param id
     * @return
     */
    Article selectPrevArticle(Long id);

    /**
     * 查询下一页：文章列表页中当前文章的下一条
     * @param id
     * @return
     */
    Article selectNextArticle(Long id);
}
