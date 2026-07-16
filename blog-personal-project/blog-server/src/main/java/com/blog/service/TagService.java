package com.blog.service;

import com.blog.pojo.dto.CategoryDTO;
import com.blog.pojo.dto.TagDTO;
import com.blog.pojo.vo.CategoryVo;
import com.blog.pojo.vo.OptionVO;
import com.blog.pojo.vo.TagVo;

import java.util.List;

public interface TagService {
    /**
     * 新增分类
     * @param tagDTO
     */
    void add(TagDTO tagDTO);

    /**
     * 查询所有分类信息
     * @return
     */
    List<TagVo> findAll();

    /**
     * 根据分类id查询
     * @param id
     * @return
     */
    TagVo getById(Long id);

    /**
     * 修改分类
     * @param tagDTO
     */
    void update(TagDTO tagDTO);

    /**
     * 启用禁用分类
     * @param id
     * @param status
     */
    void updateStatus(Long id, Integer status);

    /**
     * 删除分类
     * @param ids
     */
    void delete(List<Long> ids);

    /**
     * 获取标签下拉框数据
     * @return
     */
    List<OptionVO> getTagOptions();
}
