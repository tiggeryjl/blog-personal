package com.blog.mapper;

import com.blog.pojo.entity.Category;
import com.blog.pojo.dto.RecyclePageQueryDTO;
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
    @Select("select * from category where delete_flag = 0 order by create_time desc")
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
     * 逻辑删除分类（移入回收站）
     * @param ids
     */
    void logicDelete(@Param("ids") List<Long> ids);

    /**
     * 分页查询逻辑删除的分类（回收站）
     * @param params 查询参数
     * @return 分类列表
     */
    List<CategoryVo> recyclePageQuery(RecyclePageQueryDTO params);

    /**
     * 批量恢复（逻辑删除 -> 正常）
     * @param ids 分类ID集合
     */
    void recover(@Param("ids") List<Long> ids);

    /**
     * 批量彻底删除
     * @param ids 分类ID集合
     */
    void deleteBatch(@Param("ids") List<Long> ids);

    /**
     * 获取分类下拉框数据
     * @return
     */
    List<OptionVO> getCategoryOptions();
}
