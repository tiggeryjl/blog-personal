package com.blog.service;

import com.blog.pojo.dto.CategoryDTO;
import com.blog.pojo.vo.CategoryVo;
import com.blog.pojo.vo.OptionVO;

import java.util.List;

public interface CategoryService {
    /**
     * 新增分类
     * @param categoryDTO
     */
    void add(CategoryDTO categoryDTO);

    /**
     * 查询所有分类信息
     * @return
     */
    List<CategoryVo> findAll();

    /**
     * 根据分类id查询
     * @param id
     * @return
     */
    CategoryVo getById(Long id);

    /**
     * 修改分类
     * @param categoryDTO
     */
    void update(CategoryDTO categoryDTO);

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
     * 获取分类下拉框数据
     * @return
     */
    List<OptionVO> getCategoryOptions();
}
