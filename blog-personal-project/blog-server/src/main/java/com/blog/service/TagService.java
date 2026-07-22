package com.blog.service;

import com.blog.pojo.dto.CategoryDTO;
import com.blog.pojo.dto.TagDTO;
import com.blog.pojo.vo.CategoryVo;
import com.blog.pojo.vo.OptionVO;
import com.blog.pojo.vo.TagVo;

import java.util.List;

public interface TagService {
    /**
     * 新增标签
     * @param tagDTO
     */
    void add(TagDTO tagDTO);

    /**
     * 查询所有标签信息
     * @return
     */
    List<TagVo> findAll();

    /**
     * 根据标签id查询
     * @param id
     * @return
     */
    TagVo getById(Long id);

    /**
     * 修改标签
     * @param tagDTO
     */
    void update(TagDTO tagDTO);

    /**
     * 启用禁用标签
     * @param id
     * @param status
     */
    void updateStatus(Long id, Integer status);

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
