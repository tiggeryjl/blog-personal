package com.blog.service.impl;

import com.blog.constant.DelStatusConstant;
import com.blog.constant.MessageConstant;
import com.blog.constant.StatusConstant;
import com.blog.exception.CategoryException;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.TagMapper;
import com.blog.pojo.dto.CategoryDTO;
import com.blog.pojo.dto.TagDTO;
import com.blog.pojo.entity.Category;
import com.blog.pojo.entity.Tag;
import com.blog.pojo.vo.CategoryVo;
import com.blog.pojo.vo.OptionVO;
import com.blog.pojo.vo.TagVo;
import com.blog.service.TagService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private ArticleTagMapper articleTagMapper;

    /**
     * 新增分类
     * @param tagDTO
     */
    @Override
    public void add(TagDTO tagDTO) {
        Tag tag = new Tag();
        BeanUtils.copyProperties(tagDTO,tag);
        tag.setDeleteFlag(DelStatusConstant.ENABLE);
        tag.setCreateTime(LocalDateTime.now());
        tag.setUpdateTime(LocalDateTime.now());
        tagMapper.add(tag);
    }

    /**
     * 查询所有分类信息
     * @return
     */
    @Override
    public List<TagVo> findAll() {
        return tagMapper.findAll();

    }

    /**
     * 根据分类id查询
     * @param id
     * @return
     */
    @Override
    public TagVo getById(Long id) {
        return tagMapper.getById(id);
    }

    /**
     * 修改分类
     * @param tagDTO
     */
    @Override
    public void update(TagDTO tagDTO) {
        Tag tag = new Tag();
        BeanUtils.copyProperties(tagDTO,tag);
        tag.setUpdateTime(LocalDateTime.now());
        tagMapper.update(tag);
    }

    /**
     * 启用禁用分类
     * @param id
     * @param status
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        Tag tag = Tag.builder()
                .id(id).status(status).updateTime(LocalDateTime.now()).build();
        tagMapper.update(tag);
    }

    /**
     * 删除分类
     * @param ids
     */
    @Override
    public void delete(List<Long> ids) {
        List<Long> linkedTagIds=articleTagMapper.selectRelationTagIds(ids);
        if (linkedTagIds.isEmpty()) {
            tagMapper.delete(ids);
        } else {
            throw new CategoryException(MessageConstant.ASSOCIATED_ARTICLES);
        }
    }

    /**
     * 获取标签下拉框数据
     * @return
     */
    @Override
    public List<OptionVO> getTagOptions() {
        return tagMapper.getTagOptions();
    }
}
