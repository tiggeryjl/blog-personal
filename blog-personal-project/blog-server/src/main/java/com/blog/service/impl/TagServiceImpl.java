package com.blog.service.impl;

import com.blog.constant.DelStatusConstant;
import com.blog.constant.MessageConstant;
import com.blog.constant.StatusConstant;
import com.blog.exception.CategoryException;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.TagMapper;
import com.blog.pojo.dto.CategoryDTO;
import com.blog.pojo.dto.RecyclePageQueryDTO;
import com.blog.pojo.dto.TagDTO;
import com.blog.pojo.entity.Category;
import com.blog.pojo.entity.Tag;
import com.blog.pojo.vo.CategoryVo;
import com.blog.pojo.vo.OptionVO;
import com.blog.pojo.vo.TagVo;
import com.blog.result.PageResult;
import com.blog.service.TagService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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
     * 新增标签
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
     * 查询所有标签信息
     * @return
     */
    @Override
    public List<TagVo> findAll() {
        return tagMapper.findAll();

    }

    /**
     * 根据标签id查询
     * @param id
     * @return
     */
    @Override
    public TagVo getById(Long id) {
        return tagMapper.getById(id);
    }

    /**
     * 修改标签
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
     * 启用禁用标签
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
     * 逻辑删除标签（移入回收站）
     * @param ids
     */
    @Override
    public void delete(List<Long> ids) {
        List<Long> linkedArticleIds=articleTagMapper.selectRelationTagIds(ids);
        if (linkedArticleIds.isEmpty()) {
            tagMapper.logicDelete(ids);
        } else {
            throw new CategoryException(MessageConstant.ASSOCIATED_TAG_ARTICLES);
        }
    }

    /**
     * 分页查询逻辑删除的标签（回收站）
     * @param params 查询参数
     */
    @Override
    public PageResult recyclePageQuery(RecyclePageQueryDTO params) {
        PageHelper.startPage(params.getPage(), params.getPageSize());
        List<TagVo> tagList = tagMapper.recyclePageQuery(params);
        PageInfo<TagVo> pageInfo = new PageInfo<>(tagList);
        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 批量恢复（回收站 -> 正常列表）
     * @param ids 标签ID集合
     */
    @Override
    public void recover(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new CategoryException("请选择要恢复的标签");
        }
        tagMapper.recover(ids);
    }

    /**
     * 回收站彻底删除标签
     * @param ids 标签ID集合
     */
    @Override
    public void recycleDelete(List<Long> ids) {
        List<Long> linkedArticleIds = articleTagMapper.selectRelationTagIds(ids);
        if (!linkedArticleIds.isEmpty()) {
            throw new CategoryException(MessageConstant.ASSOCIATED_TAG_ARTICLES);
        }
        tagMapper.deleteBatch(ids);
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
