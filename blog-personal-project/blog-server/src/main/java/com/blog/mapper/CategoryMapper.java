package com.blog.mapper;

import com.blog.pojo.entity.Category;
import com.blog.pojo.vo.CategoryVo;
import com.blog.pojo.vo.OptionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {
    /**
     * 新增分类
     * @param category
     */
    void add(Category category);

    /**
     * 查询所有分类信息
     * @return
     */
    @Select("select * from category order by create_time desc")
    List<CategoryVo> findAll();

    /**
     * 根据分类id查询
     * @param id
     * @return
     */
    @Select("select * from category where id =#{id}")
    CategoryVo getById(Long id);

    /**
     * 修改分类
     * @param category
     */
    void update(Category category);

    /**
     * 删除分类
     * @param ids
     */
    void delete(@Param("ids") List<Long> ids);

    /**
     * 获取分类下拉框数据
     * @return
     */
    List<OptionVO> getCategoryOptions();
}
