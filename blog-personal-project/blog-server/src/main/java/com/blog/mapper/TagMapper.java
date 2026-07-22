package com.blog.mapper;

import com.blog.pojo.entity.Category;
import com.blog.pojo.entity.Tag;
import com.blog.pojo.vo.CategoryVo;
import com.blog.pojo.vo.OptionVO;
import com.blog.pojo.vo.TagVo;
import org.apache.ibatis.annotations.Mapper;
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
    @Select("select * from tag order by create_time desc")
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
     * 删除标签
     * @param ids
     */
    void delete(List<Long> ids);

    /**
     * 获取标签下拉框数据
     * @return
     */
    List<OptionVO> getTagOptions();
}
