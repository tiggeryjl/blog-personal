package com.blog.mapper;

import com.blog.pojo.dto.ArticleTagDTO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleTagMapper {

    /**
     * 搜索关联的标签id
     * @param ids
     * @return
     */
    List<Long> selectRelationTagIds(@Param("ids") List<Long> ids);

    /**
     * 根据文章id删除关联标签
     * @param id
     */
    @Delete("delete from article_tag where article_id = #{id}")
    void deleteByArticleId(Long id);

    /**
     * 插入文章关联标签
     * @param articleTagDTO
     */
    void batchInsert(ArticleTagDTO articleTagDTO);

    /**
     * 根据文章id查询标签
     * @param id
     * @return
     */
    List<Long> selectRelationArticleId(Long id);
}
