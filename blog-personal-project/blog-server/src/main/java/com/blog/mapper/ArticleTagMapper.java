package com.blog.mapper;

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


}
