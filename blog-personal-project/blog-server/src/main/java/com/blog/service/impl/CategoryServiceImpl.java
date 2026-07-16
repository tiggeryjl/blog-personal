package com.blog.service.impl;

import com.blog.constant.DelStatusConstant;
import com.blog.constant.MessageConstant;
import com.blog.constant.StatusConstant;
import com.blog.exception.CategoryException;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.pojo.dto.CategoryDTO;
import com.blog.pojo.entity.Category;
import com.blog.pojo.vo.CategoryVo;
import com.blog.pojo.vo.OptionVO;
import com.blog.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 新增分类
     * @param categoryDTO
     */
    @Override
    public void add(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);
        category.setDeleteFlag(DelStatusConstant.ENABLE);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.add(category);
    }

    /**
     * 查询所有分类信息
     * @return
     */
    @Override
    public List<CategoryVo> findAll() {
        return categoryMapper.findAll();

    }

    /**
     * 根据分类id查询
     * @param id
     * @return
     */
    @Override
    public CategoryVo getById(Long id) {
        return categoryMapper.getById(id);
    }

    /**
     * 修改分类
     * @param categoryDTO
     */
    @Override
    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.update(category);
    }

    /**
     * 启用禁用分类
     * @param id
     * @param status
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        Category category = Category.builder()
                .id(id).status(status).updateTime(LocalDateTime.now()).build();
        categoryMapper.update(category);
    }

    /**
     * 删除分类
     * @param ids
     */
    @Override
    public void delete(List<Long> ids) {
        List<Long> linkedCategoryIds=articleMapper.selectRelationCategoryIds(ids);
        if (linkedCategoryIds.isEmpty()) {
            categoryMapper.delete(ids);
        } else {
            throw new CategoryException(MessageConstant.ASSOCIATED_ARTICLES);
        }
    }

    /**
     * 获取分类下拉框数据
     * @return
     */
    @Override
    public List<OptionVO> getCategoryOptions() {
        return categoryMapper.getCategoryOptions();
    }
}
