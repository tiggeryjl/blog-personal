package com.blog.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
     * @param category
     */
//    void add(Category category);

}
