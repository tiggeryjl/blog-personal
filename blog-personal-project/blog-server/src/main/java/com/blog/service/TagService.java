package com.blog.service;

import com.blog.pojo.dto.CategoryDTO;
import com.blog.pojo.dto.RecyclePageQueryDTO;
import com.blog.pojo.dto.TagDTO;
import com.blog.pojo.vo.CategoryVo;
import com.blog.pojo.vo.OptionVO;
import com.blog.pojo.vo.TagVo;
import com.blog.result.PageResult;

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
     * 逻辑删除标签（移入回收站）
     * @param ids
     */
    void delete(List<Long> ids);

    /**
     * 分页查询逻辑删除的标签（回收站）
     * @param params 查询参数
     * @return 分页结果
     */
    PageResult recyclePageQuery(RecyclePageQueryDTO params);

    /**
     * 批量恢复（回收站 -> 正常列表）
     * @param ids 标签ID集合
     */
    void recover(List<Long> ids);

    /**
     * 回收站彻底删除标签
     * @param ids 标签ID集合
     */
    void recycleDelete(List<Long> ids);

    /**
     * 获取标签下拉框数据
     * @return
     */
    List<OptionVO> getTagOptions();
}
