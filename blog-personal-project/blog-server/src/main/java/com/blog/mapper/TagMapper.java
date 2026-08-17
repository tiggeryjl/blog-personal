package com.blog.mapper;

import com.blog.pojo.entity.Category;
import com.blog.pojo.entity.Tag;
import com.blog.pojo.dto.RecyclePageQueryDTO;
import com.blog.pojo.vo.CategoryVo;
import com.blog.pojo.vo.OptionVO;
import com.blog.pojo.vo.TagVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TagMapper {
    /**
     * 新增标签
     * @param tag
     */
    void add(Tag tag);

    /**
     * 查询所有标签信息
     * @return
     */
    @Select("select * from tag where delete_flag = 0 order by create_time desc")
    List<TagVo> findAll();

    /**
     * 根据标签id查询
     * @param id
     * @return
     */
    @Select("select * from tag where id =#{id}")
    TagVo getById(Long id);

    /**
     * 修改标签
     * @param tag
     */
    void update(Tag tag);

    /**
     * 逻辑删除标签（移入回收站）
     * @param ids
     */
    void logicDelete(@Param("ids") List<Long> ids);

    /**
     * 分页查询逻辑删除的标签（回收站）
     * @param params 查询参数
     * @return 标签列表
     */
    List<TagVo> recyclePageQuery(RecyclePageQueryDTO params);

    /**
     * 批量恢复（逻辑删除 -> 正常）
     * @param ids 标签ID集合
     */
    void recover(@Param("ids") List<Long> ids);

    /**
     * 批量彻底删除
     * @param ids 标签ID集合
     */
    void deleteBatch(@Param("ids") List<Long> ids);

    /**
     * 获取标签下拉框数据
     * @return
     */
    List<OptionVO> getTagOptions();
}
