package com.blog.service;

import com.blog.pojo.dto.CategoryDTO;
import com.blog.pojo.dto.RecyclePageQueryDTO;
import com.blog.pojo.vo.CategoryVo;
import com.blog.pojo.vo.OptionVO;
import com.blog.result.PageResult;

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
     * 逻辑删除分类（移入回收站）
     * @param ids
     */
    void delete(List<Long> ids);

    /**
     * 分页查询逻辑删除的分类（回收站）
     * @param params 查询参数
     * @return 分页结果
     */
    PageResult recyclePageQuery(RecyclePageQueryDTO params);

    /**
     * 批量恢复（回收站 -> 正常列表）
     * @param ids 分类ID集合
     */
    void recover(List<Long> ids);

    /**
     * 回收站彻底删除分类
     * @param ids 分类ID集合
     */
    void recycleDelete(List<Long> ids);

    /**
     * 获取分类下拉框数据
     * @return
     */
    List<OptionVO> getCategoryOptions();
}
